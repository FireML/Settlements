package me.crz.minecraft.kingdoms.integration;

import com.onarandombox.MultiverseCore.MultiverseCore;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.utils.Utils;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

public class MultiverseIntegration {
    private final MultiverseCore mvCore;

    public MultiverseIntegration(KingdomsPlugin plugin) {
        mvCore = (MultiverseCore) plugin.getServer().getPluginManager().getPlugin("Multiverse-Core");
    }

    public void importWorld(World world) {
        if (world != null && mvCore.getMVWorldManager().getMVWorld(world) == null) {
            mvCore.getMVWorldManager().addWorld(
                    world.getName(),
                    world.getEnvironment(),
                    String.valueOf(world.getSeed()),
                    world.getWorldType(),
                    world.canGenerateStructures(),
                    KingdomsPlugin.getInstance().getConfig().getString("empires-world.generator", ""),
                    true
            );
        }
    }

    public boolean deleteWorld(World world) {
        return mvCore.getMVWorldManager().deleteWorld(world.getName());
    }

    public String getVersion() {
        return mvCore.getDescription().getVersion();
    }

    public boolean renameWorld(World world, String newWorldName) {
        Map<Player, Location> positions = new LinkedHashMap<>();
        for (Player player : world.getPlayers()) {
            positions.put(player, player.getLocation());
            Utils.teleportToSpawn(player);
        }

        boolean wasAutoSave = world.isAutoSave();
        world.setAutoSave(false);
        if (!mvCore.getMVWorldManager().cloneWorld(world.getName(), newWorldName)) {
            KingdomsPlugin.getInstance().getLogger().log(Level.SEVERE, "Could not clone world " + world.getName() + " to " + newWorldName);
            return false;
        }

        World newWorld = Bukkit.getWorld(newWorldName);
        if (newWorld == null) {
            KingdomsPlugin.getInstance().getLogger().log(Level.SEVERE, "Could not get the cloned world " + newWorldName + "!");
            mvCore.getMVWorldManager().removeWorldFromConfig(newWorldName);
            return false;
        }

        File newWorldFolder = newWorld.getWorldFolder();
        if (!mvCore.getMVWorldManager().unloadWorld(newWorld.getName())) {
            KingdomsPlugin.getInstance().getLogger().log(Level.SEVERE, "Could not fully create the new world " + newWorldName + "! (Unload error)");
            deleteWorld(newWorld);
            return false;
        }
        File worldFolder = world.getWorldFolder();
        File uidFile = new File(newWorldFolder, "uid.dat");
        File newUidFile = new File(newWorldFolder, "uid.dat");
        try {
            Files.copy(uidFile.toPath(), newUidFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            KingdomsPlugin.getInstance().getLogger().log(Level.SEVERE, "Could not clone UUID of new world " + newWorldName + "!", e);
            return false;
        }
        String worldName = world.getName();
        if (!mvCore.getMVWorldManager().unloadWorld(world.getName())) {
            KingdomsPlugin.getInstance().getLogger().log(Level.SEVERE, "Could not unload old world " + world.getName() + "!");
            mvCore.getMVWorldManager().removeWorldFromConfig(newWorldName);
            return false;
        }

        if (!mvCore.getMVWorldManager().loadWorld(newWorldName)) {
            KingdomsPlugin.getInstance().getLogger().log(Level.SEVERE, "Could not load new world " + world.getName() + "!");
            mvCore.getMVWorldManager().removeWorldFromConfig(newWorldName);
            mvCore.getMVWorldManager().loadWorld(worldName);
            return false;
        }

        newWorld = Bukkit.getWorld(newWorldName);
        if (newWorld == null) {
            KingdomsPlugin.getInstance().getLogger().log(Level.SEVERE, "Could not get the new world " + world.getName() + " in Bukkit!");
            mvCore.getMVWorldManager().removeWorldFromConfig(newWorldName);
            mvCore.getMVWorldManager().loadWorld(worldName);
            return false;
        }

        mvCore.getMVWorldManager().removeWorldFromConfig(worldName);
        try {
            FileUtils.deleteDirectory(worldFolder);
        } catch (IOException e) {
            KingdomsPlugin.getInstance().getLogger().log(Level.SEVERE, "Could not delete old world folder " + worldFolder.getName() + "!", e);
        }

        if (wasAutoSave) {
            newWorld.setAutoSave(true);
        }

        for (Map.Entry<Player, Location> e : positions.entrySet()) {
            if (e.getKey().isOnline()) {
                e.getValue().setWorld(newWorld);
                e.getKey().teleport(e.getValue());
            }
        }
        return true;
    }
}
