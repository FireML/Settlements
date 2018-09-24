package me.crz.minecraft.kingdoms.command.village.settings;

import me.crz.minecraft.kingdoms.cinventory.CInventoryManager;
import me.crz.minecraft.kingdoms.village.VillageState;
import org.bukkit.event.inventory.ClickType;
import me.crz.minecraft.kingdoms.village.Village;

import java.util.Arrays;

import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import org.bukkit.Material;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.cinventory.CInventory;

public class ModeGUI {
    private static ModeGUI instance;
    public CInventory gui;

    public static String cfc(final String string) {
        if (string == null || string.length() == 0) {
            return string;
        }
        return String.valueOf(string.substring(0, 1).toUpperCase()) + string.substring(1, string.length());
    }

    public String yesOrNo(final boolean b) {
        return b ? "Yes" : "No";
    }

    public ModeGUI() {
        super();
        (gui = new CInventory("&3&lVillage Mode", InventorySize.ONE) {}).addItem(1, new ClickableItem() {
            public ItemStack getItem(final Player p) {
                if (ResidentManager.getInstance().doesResidentHaveVillage(p)) {
                    final Village village = VillageManager.getInstance().getVillageFromPlayer(p);
                    return new CItem(Material.IRON_SWORD).setName("&b&lPvP").setLore(Arrays.asList(MessageUtils.replaceColorCodes("&7&lState"), MessageUtils.replaceColorCodes("&b&l" + ModeGUI.cfc(yesOrNo(village.isPvp()))))).build();
                }
                return null;
            }

            public boolean click(final Player p, final ClickType clickType) {
                if (!ResidentManager.getInstance().doesResidentHaveVillage(p)) {
                    return false;
                }
                final Village village = VillageManager.getInstance().getVillageFromPlayer(p);
                if (village.getChief().equals(p.getUniqueId())) {
                    village.setPvp(!village.isPvp());
                    MessageUtils.tell(p, "&7PvP State &7 changed to &6" + yesOrNo(village.isPvp()));
                    gui.build(p);
                    return true;
                }
                return false;
            }
        });
        gui.addItem(2, new ClickableItem() {
            public ItemStack getItem(final Player p) {
                if (ResidentManager.getInstance().doesResidentHaveVillage(p)) {
                    final Village village = VillageManager.getInstance().getVillageFromPlayer(p);
                    return new CItem(Material.SKELETON_SKULL).setName("&b&lMobSpawning").setLore(Arrays.asList(MessageUtils.replaceColorCodes("&7&lState"), MessageUtils.replaceColorCodes("&b&l" + ModeGUI.cfc(yesOrNo(village.isMobSpawning()))))).build();
                }
                return null;
            }

            public boolean click(final Player p, final ClickType clickType) {
                if (!ResidentManager.getInstance().doesResidentHaveVillage(p)) {
                    return false;
                }
                final Village village = VillageManager.getInstance().getVillageFromPlayer(p);
                if (village.getChief().equals(p.getUniqueId())) {
                    village.setMobSpawning(!village.isMobSpawning());
                    MessageUtils.tell(p, "&7MobSpawning State &7 changed to &6" + yesOrNo(village.isMobSpawning()));
                    gui.build(p);
                    return true;
                }
                return false;
            }
        });
        gui.addItem(3, new ClickableItem() {
            public ItemStack getItem(final Player p) {
                if (ResidentManager.getInstance().doesResidentHaveVillage(p)) {
                    final Village village = VillageManager.getInstance().getVillageFromPlayer(p);
                    return new CItem(Material.FIREWORK_STAR).setName("&b&lFireSpread").setLore(Arrays.asList(MessageUtils.replaceColorCodes("&7&lState"), MessageUtils.replaceColorCodes("&b&l" + ModeGUI.cfc(yesOrNo(village.isFireSpread()))))).build();
                }
                return null;
            }

            public boolean click(final Player p, final ClickType clickType) {
                if (!ResidentManager.getInstance().doesResidentHaveVillage(p)) {
                    return false;
                }
                final Village village = VillageManager.getInstance().getVillageFromPlayer(p);
                if (village.getChief().equals(p.getUniqueId())) {
                    village.setFireSpread(!village.isFireSpread());
                    MessageUtils.tell(p, "&7FireSpread State &7 changed to &6" + yesOrNo(village.isFireSpread()));
                    gui.build(p);
                    return true;
                }
                return false;
            }
        });
        gui.addItem(4, new ClickableItem() {
            public ItemStack getItem(final Player p) {
                if (ResidentManager.getInstance().doesResidentHaveVillage(p)) {
                    final Village village = VillageManager.getInstance().getVillageFromPlayer(p);
                    return new CItem(Material.TNT).setName("&b&lExplosions").setLore(Arrays.asList(MessageUtils.replaceColorCodes("&7&lState"), MessageUtils.replaceColorCodes("&b&l" + ModeGUI.cfc(yesOrNo(village.isExplosions()))))).build();
                }
                return null;
            }

            public boolean click(final Player p, final ClickType clickType) {
                if (!ResidentManager.getInstance().doesResidentHaveVillage(p)) {
                    return false;
                }
                final Village village = VillageManager.getInstance().getVillageFromPlayer(p);
                if (village.getChief().equals(p.getUniqueId())) {
                    village.setExplosions(!village.isExplosions());
                    MessageUtils.tell(p, "&7Explosion State &7 changed to &6" + yesOrNo(village.isExplosions()));
                    gui.build(p);
                    return true;
                }
                return false;
            }
        });
        gui.addItem(5, new ClickableItem() {
            public ItemStack getItem(final Player p) {
                if (ResidentManager.getInstance().doesResidentHaveVillage(p)) {
                    final Village village = VillageManager.getInstance().getVillageFromPlayer(p);
                    return new CItem(Material.POTION).setName("&b&lBuff").setLore(Arrays.asList(MessageUtils.replaceColorCodes("&7&lState"), MessageUtils.replaceColorCodes("&b&l" + ModeGUI.cfc(yesOrNo(village.isBuff()))))).build();
                }
                return null;
            }

            public boolean click(final Player p, final ClickType clickType) {
                if (!ResidentManager.getInstance().doesResidentHaveVillage(p)) {
                    return false;
                }
                final Village village = VillageManager.getInstance().getVillageFromPlayer(p);
                if (village.getChief().equals(p.getUniqueId())) {
                    village.setBuff(!village.isBuff());
                    MessageUtils.tell(p, "&7Buff State &7 changed to &6" + yesOrNo(village.isBuff()));
                    gui.build(p);
                    return true;
                }
                return false;
            }
        });
        gui.addItem(6, new ClickableItem() {
            public ItemStack getItem(final Player p) {
                if (!ResidentManager.getInstance().doesResidentHaveVillage(p)) {
                    return null;
                }
                final Village village = VillageManager.getInstance().getVillageFromPlayer(p);
                final String yon = (village.getState() == VillageState.KINGDOM && village.getLevel() >= 4) ? (village.isTeleporter() ? "Private" : "Public") : "Not Avalable";
                if (yon.equals("Not Avalable")) {
                    return null;
                }
                return new CItem(Material.ENDER_PEARL).setName("&b&lTeleporter").setLore(Arrays.asList(MessageUtils.replaceColorCodes("&7&lState"), MessageUtils.replaceColorCodes("&b&l" + yon))).build();
            }

            public boolean click(final Player p, final ClickType clickType) {
                if (!ResidentManager.getInstance().doesResidentHaveVillage(p)) {
                    return false;
                }
                final Village village = VillageManager.getInstance().getVillageFromPlayer(p);
                if (!village.getChief().equals(p.getUniqueId())) {
                    return false;
                }
                String yon = (village.getState() == VillageState.KINGDOM && village.getLevel() >= 4) ? (village.isTeleporter() ? "Private" : "Public") : "Not Avalable";
                if (yon.equals("Not Avalable")) {
                    return false;
                }
                village.setTeleporter(!village.isTeleporter());
                yon = ((village.getState() == VillageState.KINGDOM && village.getLevel() >= 4) ? (village.isTeleporter() ? "Private" : "Public") : "Not Avalable");
                MessageUtils.tell(p, "&7Teleporter State &7 changed to &6" + yon);
                gui.build(p);
                return true;
            }
        });
        gui.addItem(8, new ClickableItem() {
            public ItemStack getItem(final Player p) {
                return new CItem(Material.BARRIER).setName("&c&lBack To Settings").build();
            }

            public boolean click(final Player p, final ClickType clickType) {
                SettingsGUI.getInstance().open(p);
                return true;
            }
        });
        CInventoryManager.getInstance().addCInvetory(gui);
    }

    public void open(final Player p) {
        gui.build(p);
    }

    public static ModeGUI getInstance() {
        if (instance == null) {
            instance = new ModeGUI();
        }
        return instance;
    }
}
