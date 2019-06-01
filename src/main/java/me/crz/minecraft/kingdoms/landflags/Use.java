package me.crz.minecraft.kingdoms.landflags;

import com.destroystokyo.paper.MaterialTags;
import me.crz.minecraft.kingdoms.utils.MaterialUtils;
import me.crz.minecraft.kingdoms.utils.NameTag;
import org.bukkit.Material;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import org.bukkit.Tag;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public enum Use implements NameTag {
    CHEST(Material.CHEST, Material.TRAPPED_CHEST),
    LEVER(Material.LEVER),
    DOOR(Tag.DOORS.getValues()),
    GATE(MaterialTags.FENCE_GATES.getValues()),
    TRAP_DOOR(Tag.TRAPDOORS.getValues()),
    BUTTON(Tag.BUTTONS.getValues()),
    PRESSURE_PLATE(MaterialTags.PRESSURE_PLATES.getValues()),
    BUCKET(MaterialTags.BUCKETS.getValues()),
    RIDE(MaterialUtils.MINECARTS),
    FURNACE(Material.FURNACE),
    HOPPER(Material.HOPPER),
    DROPPER(Material.DROPPER),
    DISPENSER(Material.DISPENSER),
    ANVIL(Tag.ANVIL.getValues()),
    ENCHANTMENT_TABLE(Material.ENCHANTING_TABLE),
    PORTAL(Material.END_PORTAL_FRAME),
    JUKEBOX(Material.JUKEBOX),
    SHULKER_BOX(MaterialTags.SHULKER_BOXES.getValues()),
    BED(MaterialTags.BEDS.getValues()),
    ARMOR_STAND(Material.ARMOR_STAND),
    ITEM_FRAME(Material.ITEM_FRAME),
    CRAFTING_TABLE(Material.CRAFTING_TABLE),
    FARMING(Material.STONE_HOE),
    ANIMAL(Material.LEATHER),
    SHEER(Material.SHEARS),
    MILK(Material.MILK_BUCKET),
    LEASH(Material.LEAD),
    BECON(Material.BEACON),
    BREWING_STAND(Material.BREWING_STAND),
    FISH(Material.FISHING_ROD),
    ITEM(BuildState.PUBLIC, Material.IRON_INGOT, Collections.EMPTY_SET),
    REPEATERS(Material.REPEATER, Material.COMPARATOR),
    CAKE(Material.CAKE),
    SHOP(BuildState.PUBLIC, Material.OAK_SIGN, Collections.EMPTY_SET),
    INTERACT(Material.BARRIER, Collections.EMPTY_SET);

    private final Collection<Material> materials;
    private final Material icon;

    private final BuildState defaultState;

    private static final Map<Material, Use> BY_MATERIAL = new EnumMap<>(Material.class);

    static {
        for (Use use : values()) {
            for (Material material : use.getMaterials()) {
                BY_MATERIAL.put(material, use);
            }
        }
    }

    Use(Material... materials) {
        this(Arrays.asList(materials));
    }

    Use(Collection<Material> materials) {
        this(materials.isEmpty() ? Material.BARRIER : materials.iterator().next(), materials);
    }

    Use(Material icon, Collection<Material> materials) {
        this(null, icon, materials);
    }

    Use(BuildState defaultState, Material icon, Collection<Material> materials) {
        this.defaultState = defaultState;
        this.icon = icon;
        this.materials = materials;
    }

    public CItem getItem() {
        return new CItem(icon).hideAllFlags(true);
    }

    private Collection<Material> getMaterials() {
        return materials;
    }

    public static Use getByMaterial(Material material) {
        return BY_MATERIAL.get(material);
    }

    public BuildState getDefault() {
        return defaultState;
    }

    public BuildState getDefault(BuildState defaultState) {
        return getDefault() != null ? getDefault() : defaultState;
    }
}
