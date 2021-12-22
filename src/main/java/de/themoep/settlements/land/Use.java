package de.themoep.settlements.land;

import de.themoep.settlements.utils.MaterialUtils;
import de.themoep.settlements.utils.NameTag;
import org.bukkit.Material;
import org.bukkit.Tag;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

public enum Use implements NameTag {
    CHEST(Material.CHEST, Material.TRAPPED_CHEST),
    BARREL(Material.BARREL),
    LEVER(Material.LEVER),
    DOOR(Tag.DOORS.getValues()),
    GATE(Tag.FENCE_GATES.getValues()),
    TRAP_DOOR(Tag.TRAPDOORS.getValues()),
    BUTTON(Tag.BUTTONS.getValues()),
    PRESSURE_PLATE(Tag.PRESSURE_PLATES.getValues()),
    BUCKET(MaterialUtils.BUCKETS),
    RIDE(MaterialUtils.MINECARTS),
    FURNACE(Material.FURNACE),
    BLAST_FURNACE(Material.BLAST_FURNACE),
    CAMPFIRE(Material.CAMPFIRE),
    SMOKER(Material.SMOKER),
    HOPPER(Material.HOPPER),
    DROPPER(Material.DROPPER),
    DISPENSER(Material.DISPENSER),
    ANVIL(Tag.ANVIL.getValues()),
    ENCHANTMENT_TABLE(Material.ENCHANTING_TABLE),
    GRINDSTONE(Material.GRINDSTONE),
    SMITHING_TABLE(Material.SMITHING_TABLE),
    PORTAL(Material.END_PORTAL_FRAME),
    JUKEBOX(Material.JUKEBOX),
    SHULKER_BOX(Tag.SHULKER_BOXES.getValues()),
    BED(Tag.BEDS.getValues()),
    ARMOR_STAND(Material.ARMOR_STAND),
    ITEM_FRAME(Material.ITEM_FRAME),
    LECTERN(Material.LECTERN),
    CRAFTING_TABLE(Material.CRAFTING_TABLE),
    CARTOGRAPHY_TABLE(Material.CARTOGRAPHY_TABLE),
    LOOM(Material.LOOM),
    BELL(Material.BELL),
    FARMING(Material.STONE_HOE),
    TREES(Material.OAK_SAPLING),
    COMPOSTER(Material.COMPOSTER),
    MONSTER(),
    ANIMAL(Material.LEATHER),
    SHEAR(Material.SHEARS),
    MILK(Material.MILK_BUCKET),
    LEASH(Material.LEAD),
    BEACON(Material.BEACON),
    BREWING_STAND(Material.BREWING_STAND),
    DRAGON_EGG(Material.DRAGON_EGG),
    FISH(Material.FISHING_ROD),
    ITEM(),
    REPEATERS(Material.REPEATER),
    COMPARATORS(Material.COMPARATOR),
    CAKE(Material.CAKE),
    SHOP(),
    MAP(Material.MAP),
    NPC(),
    INTERACT();

    private final Collection<Material> materials;

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
        this.materials = materials;
    }

    public Collection<Material> getMaterials() {
        return materials;
    }

    public static Use getByMaterial(Material material) {
        return BY_MATERIAL.get(material);
    }
}
