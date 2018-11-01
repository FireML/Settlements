package me.crz.minecraft.kingdoms.empire.gui;

import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.village.gui.SettingsGUI;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.perk.Perk;
import me.crz.minecraft.kingdoms.perk.PerkData;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.UtilTime;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.KingdomsGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.Map;

public class PerksGUI extends KingdomsGUI {
    private final Resident resident;
    public CInventory gui;

    public PerksGUI(Resident resident) {
        super("empire.perks");
        this.resident = resident;

        if (!resident.hasVillage()) {
            return;
        }

        gui = new CInventory(getText("title"), InventorySize.ONE) {};
        Empire empire = resident.getVillage().getEmpire();

        for (Perk perk : EmpireManager.getInstance().getPerks()) {
            if (!empire.hasUnlocked(perk) || !perk.hasEffects()) {
                continue;
            }

            gui.addItem(new ClickableItem() {
                @Override
                public ItemStack getItem(Player p) {
                    if (!empire.hasUnlocked(perk)) {
                        return null;
                    }
                    CItem item = new CItem(perk.getIcon());
                        PerkData perkData = resident.getVillage().getEmpire().getPerkData(perk);
                        item.setName(getMessage("entry").getReplacedText("name", perk.getTag())).glow(true);
                        if (perk.hasEffects()) {
                            if (perkData.isActive()) {
                                item.addLore(getMessage("active").getReplacedTextList("name", perk.getTag()));
                            } else {
                                item.addLore(getMessage("inactive").getReplacedTextList("name", perk.getTag()));
                            }
                            item.addLore(getMessage("toggleable").getReplacedTextList("name", perk.getTag()));
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
                    if (!resident.hasEmpire() || resident.getEmpireRank().below(EmpireRank.LEADER)) {
                        return false;
                    }
                    if (perk.hasEffects()) {
                        PerkData perkData = resident.getVillage().getEmpire().getPerkData(perk);
                        if (perkData != null) {
                            perkData.toggle();
                            gui.build(p);
                            Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(resident.getVillage()));
                            return true;
                        }
                    }
                    return false;
                }
            });
        }
        gui.addItem((gui.getMaxSlot() / 9) * 9 + 8, new ClickableItem() {
            public ItemStack getItem(final Player p) {
                return new CItem(Material.BARRIER).setName(getText("back")).build();
            }

            public boolean click(final Player p, final ClickType clickType) {
                SettingsGUI.getInstance().open(p);
                return true;
            }
        });
    }

    public void open() {
        if (gui != null) {
            gui.build(resident.getPlayer());
        }
    }
}
