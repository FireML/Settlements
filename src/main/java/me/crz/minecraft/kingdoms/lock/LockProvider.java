package me.crz.minecraft.kingdoms.lock;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.landflags.LandFlagBase;
import me.crz.minecraft.kingdoms.utils.ConfigMessages;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Collection;

public interface LockProvider extends ConfigMessages, LandFlagBase {
    boolean isLocked(Block block);

    boolean canAccess(Player player, Block block);

    static boolean canAccessCheck(Player player, Block block) {
        for (LockProvider lockProvider : KingdomsPlugin.getInstance().getLockProviders()) {
            if (lockProvider.isLocked(block) && !lockProvider.canAccess(player, block)) {
                return false;
            }
        }
        return true;
    }

    void removeInChunks(Collection<? extends SimpleChunkLocation> chunks);

    @Override
    default String type() {
        return "integration";
    }

    void removeInWorld(World world);
}
