package me.crz.minecraft.kingdoms.command.village.settings;

import me.crz.minecraft.kingdoms.cinventory.CInventoryManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.VillageGUI;
import me.crz.minecraft.kingdoms.village.Village;

import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.landflags.UseGui;
import org.bukkit.event.inventory.ClickType;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;

import java.util.HashMap;

import me.crz.minecraft.kingdoms.cinventory.CInventory;

import java.util.UUID;
import java.util.Map;

public class SettingsGUI extends VillageGUI {
    private static SettingsGUI instance;
    private static Map<UUID, ResidentsGUI> guis;
    public CInventory gui;

    static {
        guis = new HashMap<>();
    }

    public SettingsGUI() {
        super("settings");
        gui = new CInventory(getText("title"), InventorySize.ONE) {};
        gui.addItem(new ClickableItem() {
            public ItemStack getItem(Player p) {
                return new CItem(Material.GRASS).setName(getText("access")).build();
            }

            public boolean click(Player p, ClickType clickType) {
                AccessGUI.getInstance().open(p);
                return true;
            }
        });
        gui.addItem(new ClickableItem() {
            public ItemStack getItem(Player p) {
                return new CItem(Material.GRASS).setName(getText("use")).build();
            }

            public boolean click(Player p, ClickType clickType) {
                UseGui.getInstance().open(p);
                return true;
            }
        });
        gui.addItem(new ClickableItem() {
            public ItemStack getItem(Player p) {
                return new CItem(Material.REDSTONE).setName(getText("mode")).build();
            }

            public boolean click(Player p, ClickType clickType) {
                ModeGUI.getInstance().open(p);
                return true;
            }
        });
        gui.addItem(8, new ClickableItem() {
            public ItemStack getItem(Player p) {
                Village village = VillageManager.getInstance().getVillageFromPlayer(p);
                if (village == null) {
                    return null;
                }
                return new CItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE).setName(
                        getMessage("entry").getReplacedText(
                                "name", village.getName(),
                                "state", village.getState().getTag(),
                                "bank", String.valueOf(village.getBank()),
                                "bonusClaims", String.valueOf(village.getBonusClaims()),
                                "bonus", village.getBonusClaims() > 0 ? getMessage("bonus").getReplacedText("amount", String.valueOf(village.getBonusClaims())) : "",
                                "land", String.valueOf(LandManager.getInstance().getClaimedLandSize(village)),
                                "maxLand", String.valueOf(village.getMaxClaimableChunks()),
                                "outposts", String.valueOf(village.getOutpostHomes().size()),
                                "maxOutposts", String.valueOf(village.getMaxOutposts()),
                                "chiefRank", VillageRank.CHIEF.getTag(village),
                                "chief", ResidentManager.getInstance().getResidentName(village.getChief()),
                                "residents", String.valueOf(village.getResidentIds().size())
                        )
                ).build();
            }

            public boolean click(Player p, ClickType clickType) {
                return false;
            }
        });
        CInventoryManager.getInstance().addCInvetory(gui);
    }

    public void open(Player p) {
        Utils.runAsync(() -> gui.build(p));
    }

    public static ResidentsGUI getGUI(Player p) {
        if (!guis.containsKey(p.getUniqueId())) {
            ResidentsGUI instance = new ResidentsGUI();
            guis.put(p.getUniqueId(), instance);
            return instance;
        }
        return guis.get(p.getUniqueId());
    }

    public static SettingsGUI getInstance() {
        if (instance == null) {
            instance = new SettingsGUI();
        }
        return instance;
    }
}
