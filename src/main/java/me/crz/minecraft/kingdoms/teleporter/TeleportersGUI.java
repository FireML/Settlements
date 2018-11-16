package me.crz.minecraft.kingdoms.teleporter;

import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.village.VillageState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TeleportersGUI extends KingdomsGUI {
    private static TeleportersGUI instance;
    private CInventory ci;
    private ItemStack locked;

    private TeleportersGUI() {
        super("teleporters");
        ci = new CInventory(getText("title"), InventorySize.ONE) {};
        locked = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta lm = locked.getItemMeta();
        lm.setDisplayName(getText("locked"));
        locked.setItemMeta(lm);
        ci.addItem(3, new ClickableItem() {
            public ItemStack getItem(Player p) {
                ItemStack item = new ItemStack(Material.PLAYER_HEAD);
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
                Teleporters.getInstance().getPrivateTeleporters(v, p).open(p, 1);
                return true;
            }
        });
        ci.addItem(4, new ClickableItem() {
            public ItemStack getItem(Player p) {
                Village village = VillageManager.getInstance().getVillageFromPlayer(p);
                if (village == null || village.getHome() == null) {
                    ItemStack l = locked.clone();
                    l.setLore(getMessage("village-home").getReplacedTextList("state", village != null ? village.getStateTag() : VillageState.VILLAGE.getTag()));
                    return l;
                }
                return Utils.getHead(village.getChiefId(), getMessage("village-home").getReplacedText("state", village.getStateTag()));
            }

            public boolean click(Player p, ClickType clickType) {
                Village v = VillageManager.getInstance().getVillageFromPlayer(p);
                if (v == null || v.getHome() == null) {
                    return false;
                }

                Utils.teleport(p, v.getHome().toBukkitLocation(), v.getState().getKey(), "command.village.home",
                        "state", v.getState().getTag(), "name", v.getName());
                return false;
            }
        });
        ci.addItem(5, new ClickableItem() {
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
                Empire empire = resident.getVillage().getEmpire();
                Location loc = empire.getHome().toBukkitLocation();
                Utils.teleport(resident.getPlayer(), loc, "empire", "command.empire.home", "name", empire.getName(), "state", empire.getStateTag());
                return false;
            }
        });
    }

    public void build(Player p) {
        ci.build(p);
    }

    public static TeleportersGUI getInstance() {
        if (instance == null) {
            instance = new TeleportersGUI();
        }
        return instance;
    }
}
