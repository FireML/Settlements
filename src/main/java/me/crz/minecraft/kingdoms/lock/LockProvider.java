package me.crz.minecraft.kingdoms.lock;

import me.crz.minecraft.kingdoms.landflags.LandFlagBase;
import me.crz.minecraft.kingdoms.utils.ConfigMessages;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;
import org.bukkit.World;

import java.util.Collection;

public interface LockProvider extends ConfigMessages, LandFlagBase {
    void removeInChunks(Collection<? extends SimpleChunkLocation> chunks);

    @Override
    default String type() {
        return "integration";
    }

    void removeInWorld(World world);
}
