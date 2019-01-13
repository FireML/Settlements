package me.crz.minecraft.kingdoms.empire.gui;

import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class PermsGUI extends KingdomsGUI {
    private static PermsGUI instance;
    public CInventory gui;

    private PermsGUI() {
        super("perms");
        gui = new CInventory(getText("title"), InventorySize.ONE) {};

        for (EmpireRank rank : EmpireManager.getInstance().getRanks()) {
            gui.addItem(new ClickableItem() {
                @Override
                public ItemStack getItem(Player p) {
                    Resident res = ResidentManager.getInstance().getResident(p);
                    if (res.hasEmpire()) {
                        CItem item = rank.getItem().setName(getMessage("entry.title").getReplacedText(
                                "rank", rank.getTag()
                        ));
                        if (res.getEmpireRank().above(rank) || res.getEmpireRank() == EmpireManager.getInstance().getLeaderRank()) {
                            item.addLore(getMessage("entry.edit").getReplacedTextList(
                                    "rank",  rank.getTag()
                            ));
                        }
                        if (rank == res.getEmpireRank()) {
                            item.glow(true);
                        }
                        return item.build();
                    }
                    return null;
                }

                @Override
                public boolean click(Player p, ClickType type) {
                    Resident res = ResidentManager.getInstance().getResident(p);
                    if (res.hasEmpire()) {
                        RankPermsGUI.getInstance(rank).open(p);
                        return true;
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

    public void open(final Player p) {
        gui.build(p);
    }

    public static PermsGUI getInstance() {
        if (instance == null) {
            instance = new PermsGUI();
        }
        return instance;
    }
}
