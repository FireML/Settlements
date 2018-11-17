package me.crz.minecraft.kingdoms.integration;

import com.onarandombox.MultiverseCore.MultiverseCore;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import org.bukkit.World;

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
}
