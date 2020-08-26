package me.crz.minecraft.kingdoms.empire.gui;

import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.utils.ConfirmationGui;
import me.crz.minecraft.kingdoms.utils.Utils;
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
        super("empire.perms");
        gui = new CInventory(getText("title"), InventorySize.SIX) {};

        for (int i = 0; i < InventorySize.TWO.getSize(); i++) {
            gui.addItem(i, BORDER);
        }

        for (EmpireRank rank : EmpireRank.values()) {
            gui.addItem(new ClickableItem() {
                @Override
                public ItemStack getItem(Player p) {
                    Resident res = ResidentManager.getInstance().getResident(p);
                    if (res.hasEmpire()) {
                        CItem item = rank.getItem().setName(getMessage("entry.title").getReplacedText(
                                "rank", rank.getTag()
                        ));
                        if (res.getEmpireRank() == EmpireRank.getLeader()
                                || (res.getEmpireRank().above(rank)
                                        && (res.hasPermission("empire.perms.set") || res.hasPermission("empire.perms.unset")))) {
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
                    if (res.hasEmpire() && res.hasPermission("empire.perms")) {
                        RankPermGroupsGUI.getInstance(rank).open(p);
                        return true;
                    }
                    return false;
                }
            });
        }
        gui.addItem((gui.getMaxSlot() / 9) * 9 + 8, new ClickableItem() {
            @Override
            public ItemStack getItem(Player p) {
                Resident res = ResidentManager.getInstance().getResident(p);
                if (res.hasVillage() && res.hasPermission("village.perms.reset")) {
                    return new CItem(Material.ORANGE_WOOL).setName(getText("reset.title")).build();
                }
                return null;
            }

            @Override
            public boolean click(Player p, ClickType clickType) {
                Resident res = ResidentManager.getInstance().getResident(p);
                if (res.hasEmpire() && res.hasPermission("empire.perms.reset")) {
                    Empire empire = res.getVillage().getEmpire();
                    new ConfirmationGui(PermsGUI.this, "reset", () -> {
                        if (res.hasPermission("empire.perms.reset")) {
                            empire.getRanksPermissions().clear();
                            open(p);
                            Utils.runAsync(() -> DatabaseManager.getInstance().save(empire));
                        }
                    }, () -> open(p), "state", empire.getStateTag(), "name", empire.getName()).build(p);
                }
                return true;
            }
        });

        for (int i = InventorySize.FOUR.getSize(); i < InventorySize.SIX.getSize(); i++) {
            gui.addItem(i, BORDER);
        }

        gui.addItem(InventorySize.FIVE.getSize() + 4, new ClickableItem() {
            public ItemStack getItem(final Player p) {
                return new CItem(Material.BARRIER).setName(getText("back")).build();
            }

            public boolean click(final Player p, final ClickType clickType) {
                new SettingsGUI(p).open();
                return true;
            }
        });
    }

    public void open(final Player p) {
        gui.build(p);
    }

    synchronized public static PermsGUI getInstance() {
        if (instance == null) {
            instance = new PermsGUI();
        }
        return instance;
    }
}
