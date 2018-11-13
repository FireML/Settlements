package me.crz.minecraft.kingdoms.integration;

import me.crz.minecraft.kingdoms.landflags.LandFlagBase;
import me.crz.minecraft.kingdoms.utils.ConfigMessages;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;

import java.util.Collection;

public interface PluginIntegration extends ConfigMessages, LandFlagBase {
    int removeInChunk(Collection<SimpleChunkLocation> chunks);

    @Override
    default String type() {
        return "integration";
    }
}
