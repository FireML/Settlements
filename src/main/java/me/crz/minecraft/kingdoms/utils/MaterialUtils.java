package me.crz.minecraft.kingdoms.utils;

import com.destroystokyo.paper.MaterialTags;
import org.bukkit.Material;
import org.bukkit.Tag;

import java.util.EnumSet;
import java.util.Set;

public class MaterialUtils {
    public static final Set<Material> MINECARTS = EnumSet.of(
            Material.MINECART,
            Material.CHEST_MINECART,
            Material.COMMAND_BLOCK_MINECART,
            Material.FURNACE_MINECART,
            Material.HOPPER_MINECART,
            Material.TNT_MINECART
    );
}
