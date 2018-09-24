package me.crz.minecraft.kingdoms.command.village.settings;

import java.util.Iterator;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.OfflinePlayer;
import java.util.List;
import java.util.Arrays;
import org.bukkit.Bukkit;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.cinventory.CInventoryManager;
import org.bukkit.event.inventory.ClickType;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import net.md_5.bungee.api.ChatColor;
import me.crz.minecraft.kingdoms.cinventory.CInventory;

public class ResidentsGUI
{
    public CInventory gui;
    
    public static String cfc(final String string) {
        if (string == null || string.length() == 0) {
            return string;
        }
        return String.valueOf(string.substring(0, 1).toUpperCase()) + string.substring(1, string.length());
    }
    
    public static String c(final String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
    
    public ResidentsGUI() {
        super();
        (gui = new CInventory("&3&lVillage Residents", InventorySize.SIX) {
            final /* synthetic */ ResidentsGUI this$0;
            
            ResidentsGUI$1(final String $anonymous0, final InventorySize $anonymous1) {
                this$0 = this$0;
                super($anonymous0, $anonymous1);
            }
        }).addItem(53, (ClickableItem)new ClickableItem() {
            final /* synthetic */ ResidentsGUI this$0;
            
            ResidentsGUI$2() {
                this$0 = this$0;
                super();
            }
            
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
        final Village village = VillageManager.getInstance().getVillageFromPlayer(p);
        gui.clearItems();
        int i = 0;
        for (final Resident res : ResidentManager.getInstance().getResidentsForVillage(village)) {
            if (i > 52) {
                break;
            }
            gui.addItem((ClickableItem)new ClickableItem() {
                final /* synthetic */ ResidentsGUI this$0;
                private final /* synthetic */ Resident val$res;
                
                ResidentsGUI$3() {
                    this$0 = this$0;
                    super();
                }
                
                public ItemStack getItem(final Player p) {
                    final OfflinePlayer op = Bukkit.getOfflinePlayer(res.getUUID());
                    return new CItem(Material.SKULL_ITEM).setName("&c&l" + op.getName()).setLore((List)Arrays.<String>asList(ResidentsGUI.c("&7&lRole"), ResidentsGUI.c("&b&l" + ResidentsGUI.cfc(res.getVillageRank().name())))).build();
                }
                
                public boolean click(final Player p, final ClickType clickType) {
                    return true;
                }
            });
            ++i;
        }
        gui.build(p);
    }
}
