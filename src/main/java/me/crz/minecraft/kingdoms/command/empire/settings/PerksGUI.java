package me.crz.minecraft.kingdoms.command.empire.settings;

import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.perk.Perk;
import me.crz.minecraft.kingdoms.perk.PerkData;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.UtilTime;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.VillageGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.Map;

public class PerksGUI extends VillageGUI {
    private final Resident resident;
    public CInventory gui;

    public PerksGUI(Resident resident) {
        super("empireperks");
        this.resident = resident;

        if (!resident.hasVillage()) {
            return;
        }

        gui = new CInventory(getText("title"), InventorySize.ONE) {};
        Empire empire = resident.getVillage().getEmpire();

        for (Perk perk : EmpireManager.getInstance().getPerks()) {
            if (empire.getLevel() + 1 < perk.getLevel()) {
                continue;
            }

            gui.addItem(new ClickableItem() {
                @Override
                public ItemStack getItem(Player p) {
                    CItem item = new CItem(perk.getIcon());
                    if (resident.getVillage().getEmpire().hasUnlocked(perk)) {
                        PerkData perkData = resident.getVillage().getEmpire().getPerkData(perk);
                        if (perk.getRepeat() != 0) {
                            item.setName(getMessage("entry.repeat").getReplacedText(
                                    "name", perk.getTag(),
                                    "used", String.valueOf(perkData.getTimes())
                            )).glow(true);
                            if (perk.getRepeat() > 0) {
                                item.addLore(getMessage("repeatable").getReplacedTextList(
                                        "times", String.valueOf(perk.getRepeat()),
                                        "used", String.valueOf(perkData.getTimes()),
                                        "left", String.valueOf(perk.getRepeat() - perkData.getLastUsed())
                                ));
                                item.setAmount(perkData.getTimes());
                            } else {
                                item.addLore(getMessage("unlimited").getTextList());
                            }
                            if (!resident.getVillage().getEmpire().checkPerkCooldown(perk)) {
                                item.addLore(getMessage("cooldown").getReplacedTextList("cooldown", UtilTime.format(perkData.getLastUsed())));
                            }
                            if (!perk.getRepeatCost().isEmpty()) {
                                item.addLore(getMessage("cost.repeat").getTextList());
                                for (Map.Entry<String, Long> cost : perk.getRepeatCost().entrySet()) {
                                    item.addLore(getMessage("cost.entry").getReplacedTextList(
                                            "type", cost.getKey(),
                                            "icon", resident.hasResourcePack() ? ConfigManager.getInstance().getIconRpMapping().getIcon(cost.getKey(), true) : "",
                                            "amount", String.valueOf(cost.getValue())
                                    ));
                                }
                            }

                        } else {
                            item.setName(getMessage("entry.purchased").getReplacedText("name", perk.getTag())).glow(true);
                        }
                        if (perk.hasEffects()) {
                            if (perkData.isActive()) {
                                item.addLore(getMessage("active").getReplacedTextList("name", perk.getTag()));
                            } else {
                                item.addLore(getMessage("inactive").getReplacedTextList("name", perk.getTag()));
                            }
                            item.addLore(getMessage("toggleable").getReplacedTextList("name", perk.getTag()));
                        }
                    } else {
                        item.setName(getMessage("entry." + (resident.getVillage().getEmpire().canUnlock(perk) ? "unlockable" : "locked")).getReplacedText(
                                "name", perk.getTag(),
                                "level", String.valueOf(perk.getLevel()),
                                "times", String.valueOf(perk.getRepeat()),
                                "cooldown", UtilTime.format(perk.getCooldown())
                        ));
                        if (perk.getRepeat() != 0) {
                            if (perk.getRepeat() > 0) {
                                item.addLore(getMessage("repeatable").getReplacedTextList(
                                        "times", String.valueOf(perk.getRepeat()),
                                        "used", String.valueOf(0),
                                        "left", String.valueOf(perk.getRepeat())
                                ));
                            } else {
                                item.addLore(getMessage("unlimited").getTextList());
                            }
                            if (perk.getCooldown() > 0) {
                                item.addLore(getMessage("cooldown").getReplacedTextList("cooldown", UtilTime.format(perk.getCooldown())));
                            }
                            if (!perk.getRepeatCost().isEmpty()) {
                                item.addLore(getMessage("cost.repeat").getTextList());
                                for (Map.Entry<String, Long> cost : perk.getRepeatCost().entrySet()) {
                                    item.addLore(getMessage("cost.entry").getReplacedTextList(
                                            "type", cost.getKey(),
                                            "icon", resident.hasResourcePack() ? ConfigManager.getInstance().getIconRpMapping().getIcon(cost.getKey(), true) : "",
                                            "amount", String.valueOf(cost.getValue())
                                    ));
                                }
                            }
                        }
                        if (!perk.getUnlockCost().isEmpty()) {
                            item.addLore(getMessage("cost.unlock").getTextList());
                            for (Map.Entry<String, Long> cost : perk.getUnlockCost().entrySet()) {
                                item.addLore(getMessage("cost.entry").getReplacedTextList(
                                        "type", cost.getKey(),
                                        "icon", resident.hasResourcePack() ? ConfigManager.getInstance().getIconRpMapping().getIcon(cost.getKey(), true) : "",
                                        "amount", String.valueOf(cost.getValue())
                                ));
                            }
                        }
                    }

                    ItemStack itemStack = item.build();
                    if (!perk.getEffects().isEmpty()) {
                        ItemMeta itemMeta = itemStack.getItemMeta();
                        if (itemMeta instanceof PotionMeta) {
                            PotionType type = PotionType.getByEffect(perk.getEffects().get(0).getType());
                            if (type != null) {
                                ((PotionMeta) itemMeta).setBasePotionData(new PotionData(type));
                            } else {
                                ((PotionMeta) itemMeta).setMainEffect(perk.getEffects().get(0).getType());
                            }
                            itemStack.setItemMeta(itemMeta);
                        }
                    }
                    return itemStack;
                }

                @Override
                public boolean click(Player p, ClickType type) {
                    if (type == ClickType.RIGHT && perk.hasEffects()) {
                        PerkData perkData = resident.getVillage().getEmpire().getPerkData(perk);
                        if (perkData != null) {
                            perkData.toggle();
                            return true;
                        }
                    } else if (resident.getVillage().getEmpire().canRepeat(perk) && resident.getVillage().getEmpire().remove(perk.getRepeatCost())) {
                        resident.getVillage().getEmpire().repeatPerk(perk);
                        MessageUtils.tellResidents(resident.getVillage().getEmpire().getOnlineResidents(), Message.get("command.village.perks.unlock.success"), "perk", perk.getTag());
                        Utils.runAsync(() -> {
                            DatabaseManager.getInstance().saveEmpire(resident.getVillage().getEmpire());
                            DatabaseManager.getInstance().log(resident.getVillage().getEmpire(), resident, "perk-repeat", perk.getRepeatCost());
                        });
                        return true;
                    } else if (resident.getVillage().getEmpire().canUnlock(perk) && resident.getVillage().getEmpire().remove(perk.getUnlockCost())) {
                        resident.getVillage().getEmpire().unlockPerk(perk);
                        MessageUtils.tellResidents(resident.getVillage().getEmpire().getOnlineResidents(), Message.get("command.village.perks.unlock.success"), "perk", perk.getTag());
                        Utils.runAsync(() -> {
                            DatabaseManager.getInstance().saveEmpire(resident.getVillage().getEmpire());
                            DatabaseManager.getInstance().log(resident.getVillage().getEmpire(), resident, "perk-unlock", perk.getUnlockCost());
                        });
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    public void open() {
        if (gui != null) {
            gui.build(resident.getPlayer());
        }
    }
}
