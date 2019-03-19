package me.crz.minecraft.kingdoms.empire.gui;

import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.KingdomsGUI;
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
        super("empire.settings");
        this.resident = resident;
        gui = new CInventory(getText("title"), InventorySize.ONE) {};

        if (resident.hasPermission("empire.setaccess")) {
            gui.addItem(new ClickableItem() {
                public ItemStack getItem(Player p) {
                    return new CItem(Material.IRON_DOOR).setName(getText("access",
                            "access", resident.getVillage().getEmpire().getAccess().getTag(),
                            "state", resident.getVillage().getEmpire().getStateTag()
                    )).build();
                }

                public boolean click(Player p, ClickType clickType) {
                    if (resident.hasPermission("empire.setaccess")) {
                        AccessGUI.getInstance().open(p);
                        return true;
                    }
                    return false;
                }
            });
        }

        if (resident.hasPermission("empire.build")) {
            gui.addItem(new ClickableItem() {
                public ItemStack getItem(Player p) {
                    return new CItem(Material.DIAMOND_PICKAXE).setName(getText("build",
                            "build", resident.getVillage().getEmpire().getBuildState().getTag(),
                            "state", resident.getVillage().getEmpire().getStateTag()
                    )).build();
                }

                public boolean click(Player p, ClickType clickType) {
                    if (resident.hasPermission("empire.build")) {
                        BuildGui.getInstance().open(p);
                        return true;
                    }
                    return false;
                }
            });
        }

        if (resident.hasPermission("empire.use")) {
            gui.addItem(new ClickableItem() {
                public ItemStack getItem(Player p) {
                    return new CItem(Material.LEVER).setName(getText("use",
                            "state", resident.getVillage().getEmpire().getStateTag()
                    )).build();
                }

                public boolean click(Player p, ClickType clickType) {
                    if (resident.hasPermission("empire.use")) {
                        UseGui.getInstance().open(p);
                        return true;
                    }
                    return false;
                }
            });
        }

        if (resident.hasPermission("empire.mode")) {
            gui.addItem(new ClickableItem() {
                public ItemStack getItem(Player p) {
                    return new CItem(Material.REDSTONE).setName(getText("mode",
                            "state", resident.getVillage().getEmpire().getStateTag()
                    )).build();
                }

                public boolean click(Player p, ClickType clickType) {
                    if (resident.hasPermission("empire.mode")) {
                        ModeGUI.getInstance().open(p);
                        return true;
                    }
                    return false;
                }
            });
        }

        if (resident.hasPermission("empire.perms")) {
            gui.addItem(new ClickableItem() {
                public ItemStack getItem(Player p) {
                    return new CItem(Material.DIAMOND_SWORD).setName(getText("perms",
                            "state", resident.getVillage().getEmpire().getStateTag()
                    )).build();
                }

                public boolean click(Player p, ClickType clickType) {
                    if (resident.hasPermission("empire.perms")) {
                        PermsGUI.getInstance().open(p);
                        return true;
                    }
                    return false;
                }
            });
        }

        if (resident.hasPermission("empire.perks")) {
            gui.addItem(new ClickableItem() {
                public ItemStack getItem(Player p) {
                    return new CItem(Material.LINGERING_POTION).setName(getText("perks",
                            "state", resident.getVillage().getEmpire().getStateTag()
                    )).build();
                }

                public boolean click(Player p, ClickType clickType) {
                    if (resident.hasPermission("empire.perks")) {
                        new PerksGUI(resident).open();
                        return true;
                    }
                    return false;
                }
            });
        }

        if (resident.hasPermission("empire.shop")) {
            gui.addItem(new ClickableItem() {
                public ItemStack getItem(Player p) {
                    return new CItem(Material.POTION).setName(getText("shop",
                            "state", resident.getVillage().getEmpire().getStateTag()
                    )).build();
                }

                public boolean click(Player p, ClickType clickType) {
                    if (resident.hasPermission("empire.shop")) {
                        new ShopGUI(resident).open();
                        return true;
                    }
                    return false;
                }
            });
        }

        gui.addItem(8, new ClickableItem() {
            public ItemStack getItem(Player p) {
                Resident resident = ResidentManager.getInstance().getResident(p);
                if (!resident.hasEmpire()) {
                    return null;
                }
                Empire empire = resident.getVillage().getEmpire();
                return new CItem(Material.BLUE_BED).setName(
                        getMessage("entry").getReplacedText(empire.getReplacements(SettingsGUI.this))
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
