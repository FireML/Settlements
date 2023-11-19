package de.themoep.settlements.empire.gui;

import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.perk.EmpirePerk;
import de.themoep.settlements.perk.PerkData;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.ConfigurableGui;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.UtilTime;
import de.themoep.settlements.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import uk.firedev.poleislib.libs.themoep.inventorygui.DynamicGuiElement;
import uk.firedev.poleislib.libs.themoep.inventorygui.GuiBackElement;
import uk.firedev.poleislib.libs.themoep.inventorygui.GuiElementGroup;
import uk.firedev.poleislib.libs.themoep.inventorygui.StaticGuiElement;
import uk.firedev.poleislib.utils.ItemUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShopGUI extends ConfigurableGui {

    public ShopGUI(Resident resident) {
        super(resident, "empire.shop");

        GuiElementGroup group = createGroup('g');

        if (resident.hasEmpire()) {
            Empire empire = resident.getSettlement().getEmpire();

            for (EmpirePerk perk : EmpireManager.getInstance().getPerks()) {
                if (empire.hasUnlocked(perk) && !empire.checkPerkCount(perk)) {
                    continue;
                }

                group.addElement(new DynamicGuiElement('g', h -> {
                    List<String> description = new ArrayList<>();
                    ItemStack item = ItemUtils.toIcon(perk.getIcon());
                    if (resident.getSettlement().getEmpire().hasUnlocked(perk)) {
                        PerkData perkData = resident.getSettlement().getEmpire().getPerkData(perk);
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
                            if (!resident.getSettlement().getEmpire().checkPerkCooldown(perk)) {
                                description.add(getText("cooldown", "cooldown", UtilTime.format(System.currentTimeMillis() - perkData.getLastUsed())));
                            }
                            if (!perk.getRepeatCost().isEmpty()) {
                                description.add(getText("cost.repeat"));
                                for (Map.Entry<String, Long> cost : perk.getRepeatCost().entrySet()) {
                                    description.add(getText("cost.entry",
                                            "type", cost.getKey(),
                                            "icon", "",
                                            "amount", String.valueOf(cost.getValue())
                                    ));
                                }
                                if (!resident.getSettlement().getEmpire().has(perk.getRepeatCost())) {
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
                                            "icon", "",
                                            "amount", String.valueOf(cost.getValue())
                                    ));
                                }
                                if (!resident.getSettlement().getEmpire().has(perk.getRepeatCost())) {
                                    description.add(getText("cost.too-expensive"));
                                }
                            }
                        }
                        if (!perk.getUnlockCost().isEmpty()) {
                            description.add(getText("cost.unlock"));
                            for (Map.Entry<String, Long> cost : perk.getUnlockCost().entrySet()) {
                                description.add(getText("cost.entry",
                                        "type", cost.getKey(),
                                        "icon", "",
                                        "amount", String.valueOf(cost.getValue())
                                ));
                            }
                            if (!resident.getSettlement().getEmpire().has(perk.getUnlockCost())) {
                                description.add(getText("cost.too-expensive"));
                            }
                        }
                    }

                    if (!perk.getEffects().isEmpty()) {
                        ItemMeta itemMeta = item.getItemMeta();
                        if (itemMeta instanceof PotionMeta potionMeta) {
                            PotionType type = perk.getEffects().get(0).getType();
                            if (type != null) {
                                potionMeta.setBasePotionType(type);
                            }
                            item.setItemMeta(itemMeta);
                        }
                    }

                    return new StaticGuiElement('g', item, click -> {
                        if (resident.hasEmpire() && perk.matchesLevelRequirement(resident.getSettlement().getEmpire())) {
                            if (resident.hasPermission("empire.shop.repeat")
                                    && resident.getSettlement().getEmpire().canRepeat(perk)
                                    && resident.getSettlement().getEmpire().remove(perk.getRepeatCost())) {
                                resident.getSettlement().getEmpire().repeatPerk((Player) click.getWhoClicked(), perk);
                                Message.get("command.empire.shop.unlock.success")
                                        .tell(resident.getSettlement().getEmpire().getOnlineResidents(), "perk", perk.getTag());
                                DatabaseManager.getInstance().saveEmpire(resident.getSettlement().getEmpire());
                                Utils.runAsync(() -> DatabaseManager.getInstance().log(resident.getSettlement().getEmpire(), resident, "perk-repeat", perk.getRepeatCost()));
                                gui.draw();
                            } else if (resident.hasPermission("empire.shop.unlock")
                                    && resident.getSettlement().getEmpire().canUnlock(perk)
                                    && resident.getSettlement().getEmpire().remove(perk.getUnlockCost())) {
                                resident.getSettlement().getEmpire().unlockPerk(resident, perk);
                                Message.get("command.empire.shop.unlock.success")
                                        .tell(resident.getSettlement().getEmpire(), "perk", perk.getTag());
                                resident.getSettlement().getEmpire().save();
                                Utils.runAsync(() -> DatabaseManager.getInstance().log(resident.getSettlement().getEmpire(), resident, "perk-unlock", perk.getUnlockCost()));
                                gui.draw();
                            }
                        }
                        return true;
                    }, description.toArray(new String[0]));
                }));
            }
        }

        gui.addElement(group);

        gui.addElement(new GuiBackElement('b', getIcon("back"), true, getText("back")));
    }

    public void open() {
        gui.show(resident.getPlayer());
    }
}
