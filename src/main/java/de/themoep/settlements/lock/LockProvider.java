package de.themoep.settlements.lock;

import de.themoep.settlements.utils.ConfigMessages;
import de.themoep.settlements.world.SimpleChunkLocation;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.land.LandAccessChecker;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Collection;

public interface LockProvider extends ConfigMessages, LandAccessChecker {
    boolean isLocked(Block block);

    boolean canAccess(Player player, Block block);

    static boolean canAccessCheck(Player player, Block block) {
        for (LockProvider lockProvider : SettlementsPlugin.getInstance().getLockProviders()) {
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

    void renameWorld(String oldName, String newName);
}
