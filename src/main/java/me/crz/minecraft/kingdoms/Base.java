package me.crz.minecraft.kingdoms;

import me.crz.minecraft.kingdoms.land.LandPerks;
import org.bukkit.plugin.Plugin;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.event.Listener;

public class Base implements Listener
{
    public Base() {
        super();
    }
    
    public static Economy getEconomy() {
        return getPlugin().econ;
    }
    
    public static ResidentManager getResidentManager() {
        return ResidentManager.getInstance();
    }
    
    public static void registerListener(final Listener listener) {
        getPlugin().getServer().getPluginManager().registerEvents(listener, (Plugin)getPlugin());
    }
    
    public static KingdomsPlugin getPlugin() {
        return KingdomsPlugin.getInstance();
    }
    
    public static LandPerks getLandPerks() {
        return getPlugin().getLandPerks();
    }
}
