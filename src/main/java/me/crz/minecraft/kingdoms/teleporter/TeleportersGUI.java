package me.crz.minecraft.kingdoms.teleporter;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CInventoryManager;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageGUI;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.village.VillageState;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TeleportersGUI extends VillageGUI {
    private CInventory ci;
    private ItemStack locked;

    public TeleportersGUI() {
        super("teleporters");
        ci = new CInventory(getText("title"), InventorySize.ONE) {};
        locked = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta lm = locked.getItemMeta();
        lm.setDisplayName(getText("locked"));
        locked.setItemMeta(lm);
        ci.addItem(3, new ClickableItem() {
            public ItemStack getItem(Player p) {
                ItemStack item = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
                ItemMeta pm = item.getItemMeta();
                pm.setDisplayName(getText("private"));
                item.setItemMeta(pm);
                return item;
            }

            public boolean click(Player p, ClickType clickType) {
                Village v = VillageManager.getInstance().getVillageFromPlayer(p);
                if (v == null) {
                    return false;
                }
                Teleporters.getInstance().getPrivateTeleporters(v, p).open(p, 0);
                return true;
            }
        });
        ci.addItem(4, new ClickableItem() {
            public ItemStack getItem(Player p) {
                Village v = VillageManager.getInstance().getVillageFromPlayer(p);
                if (v == null || v.getState() == VillageState.VILLAGE || v.getLevel() < 10) {
                    ItemStack item = new ItemStack(locked);
                    ItemMeta pm = item.getItemMeta();
                    pm.setDisplayName(getText("kingdom-required"));
                    item.setItemMeta(pm);
                    return item;
                }
                ItemStack item = new ItemStack(Material.GRASS);
                ItemMeta pm = item.getItemMeta();
                pm.setDisplayName(MessageUtils.replaceColorCodes(getText("kingdoms-world")));
                item.setItemMeta(pm);
                return item;
            }

            public boolean click(Player p, ClickType clickType) {
                Village v = VillageManager.getInstance().getVillageFromPlayer(p);
                if (v == null || v.getState() == VillageState.VILLAGE || v.getLevel() < 10) {
                    MessageUtils.tell(p, getMessage("kingdom-required"));
                    return false;
                }
                try {
                    p.teleport(Bukkit.getWorld(KingdomsPlugin.getInstance().getKingdomsWorld()).getSpawnLocation());
                } catch (Exception e) {
                    MessageUtils.tell(p, "&c&lAn error occurred somewhere! warp possibly not set!");
                }
                return true;
            }
        });
        ci.addItem(5, new ClickableItem() {
            public ItemStack getItem(Player p) {
                Village village = VillageManager.getInstance().getVillageFromPlayer(p);
                if (village == null || village.getVillageHome() == null) {
                    ItemStack l = locked.clone();
                    l.setLore(getMessage("empire-home").getTextList());
                    return l;
                }
                return Utils.getHead(village.getChiefId(), getText("village-home"));
            }

            public boolean click(Player p, ClickType clickType) {
                Village v = VillageManager.getInstance().getVillageFromPlayer(p);
                if (v == null || v.getVillageHome() == null) {
                    return false;
                }

                p.teleport(v.getVillageHome().toBukkitLocation());
                return false;
            }
        });
        ci.addItem(6, new ClickableItem() {
            public ItemStack getItem(Player p) {
                Resident resident = ResidentManager.getInstance().getResident(p);
                if (!resident.hasEmpire() || resident.getVillage().getEmpire().getHome() == null) {
                    ItemStack l = locked.clone();
                    l.setLore(getMessage("empire-home").getTextList());
                    return l;
                }
                return Utils.getHead(resident.getVillage().getEmpire().getCapital().getChiefId(), getText("empire-home"));
            }

            public boolean click(Player p, ClickType clickType) {
                Resident resident = ResidentManager.getInstance().getResident(p);
                if (!resident.hasEmpire() || resident.getVillage().getEmpire().getHome() == null) {
                    return false;
                }

                p.teleport(resident.getVillage().getEmpire().getHome().toBukkitLocation());
                return false;
            }
        });
        CInventoryManager.getInstance().addCInvetory(ci);
    }

    public void build(Player p) {
        ci.build(p);
    }
}
