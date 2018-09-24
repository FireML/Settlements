package me.crz.minecraft.kingdoms.landflags;

import me.crz.minecraft.kingdoms.utils.MaterialUtils;
import org.bukkit.Material;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import org.bukkit.Tag;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public enum Use {
    CHEST("Chests", Material.CHEST, Material.TRAPPED_CHEST),
    LEVER("Levers", Material.LEVER),
    DOOR("Doors", Tag.DOORS.getValues()),
    GATE("Gates", MaterialUtils.FENCE_GATES),
    TRAP_DOOR("Trap Doors", Tag.TRAPDOORS.getValues()),
    BUTTON("Buttons", Tag.BUTTONS.getValues()),
    PRESSURE_PLATE("Pressure plates", MaterialUtils.PRESSURE_PLATES),
    BUCKET("Buckets", MaterialUtils.BUCKETS),
    RIDE("Ridable Stuff", MaterialUtils.MINECARTS),
    FURNACE("Furnaces", Material.FURNACE),
    HOPPER("Hoppers", Material.HOPPER),
    DROPPER("Droppers", Material.DROPPER),
    DISPENSER("Dispensers", Material.DISPENSER),
    ANVIL("Anvils", Tag.ANVIL.getValues()),
    ENCHANTMENT_TABLE("Enchantment Tables", Material.ENCHANTING_TABLE),
    PORTAL("Portals", Material.END_PORTAL_FRAME),
    JUKEBOX("Jukeboxes", Material.JUKEBOX),
    SHULKER_BOX("Shulker Boxes", MaterialUtils.SHULKER_BOXES),
    BED("Beds", MaterialUtils.BEDS),
    ARMOR_STAND("Armorstands", Material.ARMOR_STAND),
    ITEM_FRAME("Item Frames", Material.ITEM_FRAME),
    CRAFTING_TABLE("Crafting tables", Material.CRAFTING_TABLE),
    SHEER("Sheering Animals", Material.SHEARS),
    MILK("Milking Cows", Material.MILK_BUCKET),
    LEASH("Leashing Animals", Material.LEAD),
    BECON("Beacons", Material.BEACON),
    BREWING_STAND("Brewing Stands", Material.BREWING_STAND),
    FISH("Fishing", Material.FISHING_ROD),
    ITEM(Material.IRON_INGOT, "Pickup Items", Collections.EMPTY_SET),
    REPEATERS("Repeaters", Material.REPEATER, Material.COMPARATOR),
    CAKE("Cake", Material.CAKE),
    INTERACT(Material.BARRIER, "Other", Collections.EMPTY_SET);

    private final Collection<Material> materials;
    private CItem item;

    private static final Map<Material, Use> BY_MATERIAL = new EnumMap<>(Material.class);

    static {
        for (Use use : values()) {
            for (Material material : use.getMaterials()) {
                BY_MATERIAL.put(material, use);
            }
        }
    }

    Use(String name, Material... materials) {
        this(name, Arrays.asList(materials));
    }

    Use(String name, Collection<Material> materials) {
        this(materials.isEmpty() ? Material.BARRIER : materials.iterator().next(), name, materials);
    }

    Use(Material icon, String name, Collection<Material> materials) {
        this.item = new CItem(icon).setName("&b&l" + name);
        this.materials = materials;
    }

    public CItem getItem() {
        return item;
    }

    private Collection<Material> getMaterials() {
        return materials;
    }

    public static Use getByMaterial(Material material) {
        return BY_MATERIAL.get(material);
    }
}
