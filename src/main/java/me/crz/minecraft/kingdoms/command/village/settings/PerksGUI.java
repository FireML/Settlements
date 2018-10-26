package me.crz.minecraft.kingdoms.command.village.settings;

import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Pair;
import me.crz.minecraft.kingdoms.utils.UtilTime;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.VillageGUI;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.village.VillagePerk;
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
        super("perks");
        this.resident = resident;

        if (!resident.hasVillage()) {
            return;
        }

        gui = new CInventory(getText("title"), InventorySize.ONE) {};

        for (VillagePerk perk : VillageManager.getInstance().getPerks()) {
            gui.addItem(new ClickableItem() {
                @Override
                public ItemStack getItem(Player p) {
                    CItem item = new CItem(perk.getIcon());
                    if (resident.getVillage().hasUnlocked(perk)) {
                        Pair<Integer, Long> unlockData = resident.getVillage().getPerkData(perk);
                        if (perk.getRepeat() != 0) {
                            item.setName(getMessage("entry.repeat").getReplacedText(
                                    "name", perk.getTag(),
                                    "used", String.valueOf(unlockData.getLeft())
                            )).glow(true);
                            if (perk.getRepeat() > 0) {
                                item.addLore(getMessage("repeatable").getReplacedTextList(
                                        "times", String.valueOf(perk.getRepeat()),
                                        "used", String.valueOf(unlockData.getLeft()),
                                        "left", String.valueOf(perk.getRepeat() - unlockData.getLeft())
                                ));
                            } else {
                                item.addLore(getMessage("unlimited").getTextList());
                            }
                            if (!resident.getVillage().checkPerkCooldown(perk)) {
                                item.addLore(getMessage("cooldown").getReplacedTextList("cooldown", UtilTime.format(unlockData.getRight())));
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
                    } else {
                        item.setName(getMessage("entry." + (resident.getVillage().canUnlock(perk) ? "unlockable" : "locked")).getReplacedText(
                                "name", perk.getTag(),
                                "state", perk.getState().getTag(),
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
                    if (resident.getVillage().canRepeat(perk) && resident.getVillage().remove(perk.getRepeatCost())) {
                        resident.getVillage().repeatPerk(perk);
                        MessageUtils.tellResidents(resident.getVillage().getOnlineResidents(), Message.get("command.village.perks.unlock.success"), "perk", perk.getTag());
                        Utils.runAsync(() -> {
                            DatabaseManager.getInstance().saveVillage(resident.getVillage());
                            DatabaseManager.getInstance().log(resident.getVillage(), resident, "perk-repeat", perk.getRepeatCost());
                        });
                        return true;
                    } else if (resident.getVillage().canUnlock(perk) && resident.getVillage().remove(perk.getUnlockCost())) {
                        resident.getVillage().unlockPerk(perk);
                        MessageUtils.tellResidents(resident.getVillage().getOnlineResidents(), Message.get("command.village.perks.unlock.success"), "perk", perk.getTag());
                        Utils.runAsync(() -> {
                            DatabaseManager.getInstance().saveVillage(resident.getVillage());
                            DatabaseManager.getInstance().log(resident.getVillage(), resident, "perk-unlock", perk.getUnlockCost());
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
