package me.crz.minecraft.kingdoms.village.gui;

import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.village.VillageState;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class SettingsGUI extends KingdomsGUI {

    private final Resident resident;
    public CInventory gui;

    public SettingsGUI(Player player) {
        this(ResidentManager.getInstance().getResident(player));
    }

    public SettingsGUI(Resident resident) {
        super("settings");
        this.resident = resident;
        gui = new CInventory(getText("title"), InventorySize.ONE) {};

        if (resident.hasPermission("village.setaccess")) {
            gui.addItem(new ClickableItem() {
                public ItemStack getItem(Player p) {
                    return new CItem(Material.IRON_DOOR).setName(getText("access",
                            "access", resident.getVillage().getAccess().getTag(),
                            "state", resident.getVillage().getStateTag()
                    )).build();
                }

                public boolean click(Player p, ClickType clickType) {
                    if (resident.hasPermission("village.setaccess")) {
                        AccessGUI.getInstance().open(p);
                        return true;
                    }
                    return false;
                }
            });
        }

        if (resident.hasPermission("village.build")) {
            gui.addItem(new ClickableItem() {
                public ItemStack getItem(Player p) {
                    return new CItem(Material.DIAMOND_PICKAXE).setName(getText("build",
                            "build", resident.getVillage().getBuildState().getTag(),
                            "state", resident.getVillage().getStateTag()
                    )).build();
                }

                public boolean click(Player p, ClickType clickType) {
                    if (resident.hasPermission("village.build")) {
                        BuildGui.getInstance().open(p);
                        return true;
                    }
                    return false;
                }
            });
        }

        if (resident.hasPermission("village.use")) {
            gui.addItem(new ClickableItem() {
                public ItemStack getItem(Player p) {
                    return new CItem(Material.LEVER).setName(getText("use",
                            "state", resident.getVillage().getStateTag()
                    )).build();
                }

                public boolean click(Player p, ClickType clickType) {
                    if (resident.hasPermission("village.use")) {
                        UseGui.getInstance().open(p);
                        return true;
                    }
                    return false;
                }
            });
        }

        if (resident.hasPermission("village.mode")) {
            gui.addItem(new ClickableItem() {
                public ItemStack getItem(Player p) {
                    return new CItem(Material.REDSTONE).setName(getText("mode",
                            "state", resident.getVillage().getStateTag()
                    )).build();
                }

                public boolean click(Player p, ClickType clickType) {
                    if (resident.hasPermission("village.mode")) {
                        ModeGUI.getInstance().open(p);
                        return true;
                    }
                    return false;
                }
            });
        }

        if (resident.hasPermission("village.perms")) {
            gui.addItem(new ClickableItem() {
                public ItemStack getItem(Player p) {
                    return new CItem(Material.DIAMOND_SWORD).setName(getText("perms",
                            "state", resident.getVillage().getStateTag()
                    )).build();
                }

                public boolean click(Player p, ClickType clickType) {
                    if (resident.hasPermission("village.perms")) {
                        PermsGUI.getInstance().open(p);
                        return true;
                    }
                    return false;
                }
            });
        }

        if (resident.hasPermission("village.perks")) {
            gui.addItem(new ClickableItem() {
                public ItemStack getItem(Player p) {
                    return new CItem(Material.LINGERING_POTION).setName(getText("perks",
                            "state", resident.getVillage().getStateTag()
                    )).build();
                }

                public boolean click(Player p, ClickType clickType) {
                    if (resident.hasPermission("village.perks")) {
                        new PerksGUI(resident).open();
                        return true;
                    }
                    return false;
                }
            });
        }

        if (resident.hasPermission("village.shop")) {
            gui.addItem(new ClickableItem() {
                public ItemStack getItem(Player p) {
                    return new CItem(Material.POTION).setName(getText("shop",
                            "state", resident.getVillage().getStateTag()
                    )).build();
                }

                public boolean click(Player p, ClickType clickType) {
                    if (resident.hasPermission("village.shop")) {
                        new ShopGUI(resident).open();
                        return true;
                    }
                    return false;
                }
            });
        }

        gui.addItem(8, new ClickableItem() {
            public ItemStack getItem(Player p) {
                Village village = VillageManager.getInstance().getVillageFromPlayer(p);
                if (village == null) {
                    return null;
                }
                return new CItem(village.getState() == VillageState.KINGDOM ? Material.RED_BED : Material.GREEN_BED).setName(
                        getMessage("entry").getReplacedText(village.getReplacements(SettingsGUI.this))
                ).build();
            }

            public boolean click(Player p, ClickType clickType) {
                return false;
            }
        });
    }

    public void open() {
        gui.build(resident.getPlayer());
    }
}
