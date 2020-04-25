package me.crz.minecraft.kingdoms.integration;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.utils.Utils;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
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

    public World renameWorld(World world, String newWorldName) {
        Map<Player, Location> positions = new LinkedHashMap<>();
        for (Player player : world.getPlayers()) {
            positions.put(player, player.getLocation());
            Utils.teleportToSpawn(player);
        }

        MultiverseWorld oldMvWorld = mvCore.getMVWorldManager().getMVWorld(world);
        if (oldMvWorld != null) {
            oldMvWorld.setAlias(null);
        }

        boolean wasAutoSave = world.isAutoSave();
        if (!mvCore.getMVWorldManager().cloneWorld(world.getName(), newWorldName)) {
            KingdomsPlugin.getInstance().getLogger().log(Level.SEVERE, "Could not clone world " + world.getName() + " to " + newWorldName);
            return null;
        }

        World newWorld = Bukkit.getWorld(newWorldName);
        if (newWorld == null) {
            KingdomsPlugin.getInstance().getLogger().log(Level.SEVERE, "Could not get the cloned world " + newWorldName + "!");
            mvCore.getMVWorldManager().removeWorldFromConfig(newWorldName);
            return null;
        }

        File newWorldFolder = newWorld.getWorldFolder();
        if (!mvCore.getMVWorldManager().unloadWorld(newWorld.getName())) {
            KingdomsPlugin.getInstance().getLogger().log(Level.SEVERE, "Could not fully create the new world " + newWorldName + "! (Unload error)");
            deleteWorld(newWorld);
            return null;
        }
        File worldFolder = world.getWorldFolder();
        File uidFile = new File(newWorldFolder, "uid.dat");
        File newUidFile = new File(newWorldFolder, "uid.dat");
        try {
            Files.copy(uidFile.toPath(), newUidFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            KingdomsPlugin.getInstance().getLogger().log(Level.SEVERE, "Could not clone UUID of new world " + newWorldName + "!", e);
            return null;
        }
        String worldName = world.getName();
        if (!mvCore.getMVWorldManager().unloadWorld(world.getName())) {
            KingdomsPlugin.getInstance().getLogger().log(Level.SEVERE, "Could not unload old world " + world.getName() + "!");
            mvCore.getMVWorldManager().removeWorldFromConfig(newWorldName);
            return null;
        }

        if (!mvCore.getMVWorldManager().loadWorld(newWorldName)) {
            KingdomsPlugin.getInstance().getLogger().log(Level.SEVERE, "Could not load new world " + world.getName() + "!");
            mvCore.getMVWorldManager().removeWorldFromConfig(newWorldName);
            mvCore.getMVWorldManager().loadWorld(worldName);
            return null;
        }

        newWorld = Bukkit.getWorld(newWorldName);
        if (newWorld == null) {
            KingdomsPlugin.getInstance().getLogger().log(Level.SEVERE, "Could not get the new world " + world.getName() + " in Bukkit!");
            mvCore.getMVWorldManager().removeWorldFromConfig(newWorldName);
            mvCore.getMVWorldManager().loadWorld(worldName);
            return null;
        }

        mvCore.getMVWorldManager().removeWorldFromConfig(worldName);
        try {
            FileUtils.deleteDirectory(worldFolder);
        } catch (IOException e) {
            KingdomsPlugin.getInstance().getLogger().log(Level.SEVERE, "Could not delete old world folder " + worldFolder.getName() + "!", e);
        }

        MultiverseWorld newMvWorld = mvCore.getMVWorldManager().getMVWorld(world);
        if (newMvWorld.getAlias() != null) {
            newMvWorld.setAlias(null);
        }

        if (wasAutoSave) {
            newWorld.setAutoSave(true);
        }

        mvCore.saveWorldConfig();

        for (Map.Entry<Player, Location> e : positions.entrySet()) {
            if (e.getKey().isOnline()) {
                e.getValue().setWorld(newWorld);
                e.getKey().teleportAsync(e.getValue());
            }
        }
        return newWorld;
    }
}
