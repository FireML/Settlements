package de.themoep.settlements.settlement.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.perk.SettlementPerk;
import de.themoep.settlements.perk.PerkData;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.UtilTime;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShopGUI extends SettlementsGUI {
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
        super("shop");
        this.resident = resident;

        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title"), SETUP);
        gui.setCloseAction(close -> false);
        gui.setFiller(BORDER_ITEM);

        GuiElementGroup group = new GuiElementGroup('g');

        if (resident.hasSettlement()) {
            Settlement settlement = resident.getSettlement();

            for (SettlementPerk perk : SettlementManager.getInstance().getPerks()) {
                if (settlement.hasUnlocked(perk) && !settlement.checkPerkCount(perk)) {
                    continue;
                }

                group.addElement(new DynamicGuiElement('g', h -> {
                    List<String> description = new ArrayList<>();
                    ItemStack item = ItemUtils.toIcon(perk.getIcon());
                    if (resident.getSettlement().hasUnlocked(perk)) {
                        PerkData perkData = resident.getSettlement().getPerkData(perk);
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
                            if (!resident.getSettlement().checkPerkCooldown(perk)) {
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
                                if (!resident.getSettlement().has(perk.getRepeatCost())) {
                                    description.add(getText("cost.too-expensive"));
                                }
                            }

                        } else {
                            description.add(getText("entry.purchased", "name", perk.getTag()));
                            ItemUtils.setGlowing(item, true);
                        }
                    } else {
                        description.add(getText("entry." + (perk.matchesLevelRequirement(resident.getSettlement()) ? "unlockable" : "locked"),
                                "name", perk.getTag(),
                                "state", perk.getState().getTag(),
                                "level", String.valueOf(perk.getLevel()),
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
                                if (!resident.getSettlement().has(perk.getRepeatCost())) {
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
                            if (!resident.getSettlement().has(perk.getUnlockCost())) {
                                description.add(getText("cost.too-expensive"));
                            }
                        }
                    }

                    if (!perk.getEffects().isEmpty()) {
                        ItemMeta itemMeta = item.getItemMeta();
                        if (itemMeta instanceof PotionMeta) {
                            itemMeta.removeItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
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
                        if (resident.hasSettlement() && perk.matchesLevelRequirement(resident.getSettlement())) {
                            if (resident.hasPermission("settlement.shop.repeat")
                                    && resident.getSettlement().canRepeat(perk)
                                    && resident.getSettlement().remove(perk.getRepeatCost())) {
                                resident.getSettlement().repeatPerk((Player) click.getWhoClicked(), perk);
                                Message.get("command.settlement.shop.unlock.success")
                                        .tell(resident.getSettlement().getOnlineResidents(), "perk", perk.getTag());
                                Utils.runAsync(() -> {
                                    DatabaseManager.getInstance().saveSettlement(resident.getSettlement());
                                    DatabaseManager.getInstance().log(resident.getSettlement(), resident, "perk-repeat", perk.getRepeatCost());
                                });
                                gui.draw();
                            } else if (resident.hasPermission("settlement.shop.unlock")
                                    && resident.getSettlement().canUnlock(perk)
                                    && resident.getSettlement().remove(perk.getUnlockCost())) {
                                resident.getSettlement().unlockPerk(resident, perk);
                                Message.get("command.settlement.shop.unlock.success")
                                        .tell(resident.getSettlement(), "perk", perk.getTag());
                                resident.getSettlement().save();
                                Utils.runAsync(() -> {
                                    DatabaseManager.getInstance().log(resident.getSettlement(), resident, "perk-unlock", perk.getUnlockCost());
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
