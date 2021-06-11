package me.crz.minecraft.kingdoms.empire.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.perk.EmpirePerk;
import me.crz.minecraft.kingdoms.perk.PerkData;
import me.crz.minecraft.kingdoms.utils.ItemUtils;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.UtilTime;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.resident.Resident;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShopGUI extends KingdomsGUI {
    private static final String[] SETUP = {
            "         ",
            "         ",
            "ggggggggg",
            "ggggggggg",
            "         ",
            "    b    ",
    };
    private final Resident resident;
    private final InventoryGui gui;

    public ShopGUI(Resident resident) {
        super("empire.shop");
        this.resident = resident;

        gui = new InventoryGui(KingdomsPlugin.getInstance(), getText("title"), SETUP);
        gui.setCloseAction(close -> false);
        gui.setFiller(BORDER_ITEM);

        GuiElementGroup group = new GuiElementGroup('g');

        if (resident.hasEmpire()) {
            Empire empire = resident.getVillage().getEmpire();

            for (EmpirePerk perk : EmpireManager.getInstance().getPerks()) {
                if (empire.hasUnlocked(perk) && !empire.checkPerkCount(perk)) {
                    continue;
                }

                group.addElement(new DynamicGuiElement('g', h -> {
                    List<String> description = new ArrayList<>();
                    ItemStack item = ItemUtils.toIcon(perk.getIcon());
                    if (resident.getVillage().getEmpire().hasUnlocked(perk)) {
                        PerkData perkData = resident.getVillage().getEmpire().getPerkData(perk);
                        if (perk.getRepeat() != 0) {
                            description.add(getText("entry.repeat",
                                    "name", perk.getTag(),
                                    "used", String.valueOf(perkData.getTimes())
                            ));
                            ItemUtils.setGlowing(item, true);
                            if (perk.getRepeat() > 0) {
                                description.add(getText("repeatable",
                                        "times", String.valueOf(perk.getRepeat()),
                                        "used", String.valueOf(perkData.getTimes()),
                                        "left", String.valueOf(perk.getRepeat() + 1 - perkData.getTimes())
                                ));
                                item.setAmount(perkData.getTimes());
                            } else {
                                description.add(getText("unlimited"));
                            }
                            if (!resident.getVillage().getEmpire().checkPerkCooldown(perk)) {
                                description.add(getText("cooldown", "cooldown", UtilTime.format(System.currentTimeMillis() - perkData.getLastUsed())));
                            }
                            if (!perk.getRepeatCost().isEmpty()) {
                                description.add(getText("cost.repeat"));
                                for (Map.Entry<String, Long> cost : perk.getRepeatCost().entrySet()) {
                                    description.add(getText("cost.entry",
                                            "type", cost.getKey(),
                                            "icon", Utils.getIcon(resident, cost.getKey()),
                                            "amount", String.valueOf(cost.getValue())
                                    ));
                                }
                                if (!resident.getVillage().getEmpire().has(perk.getRepeatCost())) {
                                    description.add(getText("cost.too-expensive"));
                                }
                            }

                        } else {
                            description.add(getText("entry.purchased", "name", perk.getTag()));
                            ItemUtils.setGlowing(item, true);
                        }
                    } else {
                        description.add(getText("entry." + (perk.matchesLevelRequirement(empire) ? "unlockable" : "locked"),
                                "name", perk.getTag(),
                                "times", String.valueOf(perk.getRepeat()),
                                "cooldown", UtilTime.format(perk.getCooldown())
                        ));
                        if (perk.getRepeat() != 0) {
                            if (perk.getRepeat() > 0) {
                                description.add(getText("repeatable",
                                        "times", String.valueOf(perk.getRepeat()),
                                        "used", String.valueOf(0),
                                        "left", String.valueOf(perk.getRepeat())
                                ));
                            } else {
                                description.add(getText("unlimited"));
                            }
                            if (perk.getCooldown() > 0) {
                                description.add(getText("cooldown", "cooldown", UtilTime.format(perk.getCooldown())));
                            }
                            if (!perk.getRepeatCost().isEmpty()) {
                                description.add(getText("cost.repeat"));
                                for (Map.Entry<String, Long> cost : perk.getRepeatCost().entrySet()) {
                                    description.add(getText("cost.entry",
                                            "type", cost.getKey(),
                                            "icon", Utils.getIcon(resident, cost.getKey()),
                                            "amount", String.valueOf(cost.getValue())
                                    ));
                                }
                                if (!resident.getVillage().getEmpire().has(perk.getRepeatCost())) {
                                    description.add(getText("cost.too-expensive"));
                                }
                            }
                        }
                        if (!perk.getUnlockCost().isEmpty()) {
                            description.add(getText("cost.unlock"));
                            for (Map.Entry<String, Long> cost : perk.getUnlockCost().entrySet()) {
                                description.add(getText("cost.entry",
                                        "type", cost.getKey(),
                                        "icon", Utils.getIcon(resident, cost.getKey()),
                                        "amount", String.valueOf(cost.getValue())
                                ));
                            }
                            if (!resident.getVillage().getEmpire().has(perk.getUnlockCost())) {
                                description.add(getText("cost.too-expensive"));
                            }
                        }
                    }

                    if (!perk.getEffects().isEmpty()) {
                        ItemMeta itemMeta = item.getItemMeta();
                        if (itemMeta instanceof PotionMeta) {
                            PotionType type = PotionType.getByEffect(perk.getEffects().get(0).getType());
                            if (type != null) {
                                ((PotionMeta) itemMeta).setBasePotionData(new PotionData(type));
                            } else {
                                ((PotionMeta) itemMeta).setMainEffect(perk.getEffects().get(0).getType());
                            }
                            item.setItemMeta(itemMeta);
                        }
                    }

                    return new StaticGuiElement('g', item, click -> {
                        if (resident.hasEmpire() && perk.matchesLevelRequirement(resident.getVillage().getEmpire())) {
                            if (resident.hasPermission("empire.shop.repeat")
                                    && resident.getVillage().getEmpire().canRepeat(perk)
                                    && resident.getVillage().getEmpire().remove(perk.getRepeatCost())) {
                                resident.getVillage().getEmpire().repeatPerk((Player) click.getWhoClicked(), perk);
                                Message.get("command.empire.shop.unlock.success")
                                        .tell(resident.getVillage().getEmpire().getOnlineResidents(), "perk", perk.getTag());
                                Utils.runAsync(() -> {
                                    DatabaseManager.getInstance().saveEmpire(resident.getVillage().getEmpire());
                                    DatabaseManager.getInstance().log(resident.getVillage().getEmpire(), resident, "perk-repeat", perk.getRepeatCost());
                                });
                                gui.draw();
                            } else if (resident.hasPermission("empire.shop.unlock")
                                    && resident.getVillage().getEmpire().canUnlock(perk)
                                    && resident.getVillage().getEmpire().remove(perk.getUnlockCost())) {
                                resident.getVillage().getEmpire().unlockPerk((Player) click.getWhoClicked(), perk);
                                Message.get("command.empire.shop.unlock.success")
                                        .tell(resident.getVillage().getEmpire().getOnlineResidents(), "perk", perk.getTag());
                                Utils.runAsync(() -> {
                                    DatabaseManager.getInstance().saveEmpire(resident.getVillage().getEmpire());
                                    DatabaseManager.getInstance().log(resident.getVillage().getEmpire(), resident, "perk-unlock", perk.getUnlockCost());
                                });
                                gui.draw();
                            }
                        }
                        return true;
                    }, description.toArray(new String[0]));
                }));
            }
        }

        gui.addElement(group);

        gui.addElement(new StaticGuiElement('b', ItemUtils.toIcon(Material.BARRIER), click -> {
            new SettingsGUI(resident).open();
            return true;
        }, getText("back")));
    }

    public void open() {
        gui.show(resident.getPlayer());
    }
}
