package me.crz.minecraft.kingdoms.landflags;

import org.bukkit.Material;
import me.crz.minecraft.kingdoms.cinventory.CItem;

public enum Use
{
    CHEST("CHEST", 0, new CItem(Material.CHEST).setName("&b&lChests")), 
    LEVER("LEVER", 1, new CItem(Material.LEVER).setName("&b&lLevers")), 
    DOOR("DOOR", 2, new CItem(Material.SPRUCE_DOOR_ITEM).setName("&b&lDoors")), 
    GATE("GATE", 3, new CItem(Material.FENCE_GATE).setName("&b&lGates")), 
    TRAP_DOOR("TRAP_DOOR", 4, new CItem(Material.TRAP_DOOR).setName("&b&lTrap Doors")), 
    BUTTON("BUTTON", 5, new CItem(Material.WOOD_BUTTON).setName("&b&lButtons")), 
    PRESSURE_PLATE("PRESSURE_PLATE", 6, new CItem(Material.WOOD_PLATE).setName("&b&lPressure plates")), 
    BUCKET("BUCKET", 7, new CItem(Material.BUCKET).setName("&b&lBuckets")), 
    RIDE("RIDE", 8, new CItem(Material.MINECART).setName("&b&lRidable Stuff")), 
    FURNACE("FURNACE", 9, new CItem(Material.FURNACE).setName("&b&lFurnaces")), 
    HOPPER("HOPPER", 10, new CItem(Material.HOPPER).setName("&b&lHoppers")), 
    DROPPER("DROPPER", 11, new CItem(Material.DROPPER).setName("&b&lDroppers")), 
    DISPENSER("DISPENSER", 12, new CItem(Material.DISPENSER).setName("&b&lDispensers")), 
    ANVIL("ANVIL", 13, new CItem(Material.ANVIL).setName("&b&lAnvils")), 
    ENCHANTMENT_TABLE("ENCHANTMENT_TABLE", 14, new CItem(Material.ENCHANTMENT_TABLE).setName("&b&lEnchantment Tables")), 
    PORTAL("PORTAL", 15, new CItem(Material.ENDER_PORTAL_FRAME).setName("&b&lPortals")), 
    JUKEBOX("JUKEBOX", 16, new CItem(Material.JUKEBOX).setName("&b&lJukeboxes")), 
    SHULKER_BOX("SHULKER_BOX", 17, new CItem(Material.RED_SHULKER_BOX).setName("&b&lShulker Boxes")), 
    BED("BED", 18, new CItem(Material.BED).setName("&b&lBeds")), 
    ARMOR_STAND("ARMOR_STAND", 19, new CItem(Material.ARMOR_STAND).setName("&b&lArmorstands")), 
    ITEM_FRAME("ITEM_FRAME", 20, new CItem(Material.ITEM_FRAME).setName("&b&lItem Frames")), 
    CRAFTING_TABLE("CRAFTING_TABLE", 21, new CItem(Material.WORKBENCH).setName("&b&lCrafting tables")), 
    SHEER("SHEER", 22, new CItem(Material.SHEARS).setName("&b&lSheering Animals")), 
    MILK("MILK", 23, new CItem(Material.MILK_BUCKET).setName("&b&lMilking Cows")), 
    LEASH("LEASH", 24, new CItem(Material.LEASH).setName("&b&lLeashing Animals")), 
    BECON("BECON", 25, new CItem(Material.BEACON).setName("&b&lBeacons")), 
    BREWING_STAND("BREWING_STAND", 26, new CItem(Material.BREWING_STAND_ITEM).setName("&b&lBrewing Stands")), 
    FISH("FISH", 27, new CItem(Material.FISHING_ROD).setName("&b&lFishing")), 
    ITEM("ITEM", 28, new CItem(Material.IRON_INGOT).setName("&b&lPickup Items")), 
    REPEATERS("REPEATERS", 29, new CItem(Material.REDSTONE_COMPARATOR).setName("&b&lRepeaters")), 
    CAKE("CAKE", 30, new CItem(Material.CAKE).setName("&b&lCake")), 
    INTERACT("INTERACT", 31, new CItem(Material.BARRIER).setName("&b&lOther"));
    
    private CItem item;
    private static final /* synthetic */ Use[] ENUM$VALUES;
    
    static {
        ENUM$VALUES = new Use[] { CHEST, LEVER, DOOR, GATE, TRAP_DOOR, BUTTON, PRESSURE_PLATE, BUCKET, RIDE, FURNACE, HOPPER, DROPPER, DISPENSER, ANVIL, ENCHANTMENT_TABLE, PORTAL, JUKEBOX, SHULKER_BOX, BED, ARMOR_STAND, ITEM_FRAME, CRAFTING_TABLE, SHEER, MILK, LEASH, BECON, BREWING_STAND, FISH, ITEM, REPEATERS, CAKE, INTERACT };
    }
    
    private Use(final String s, final int n, final CItem item) {
        item = item;
    }
    
    public CItem getItem() {
        return item;
    }
    
    public static Use[] values() {
        final Use[] enum$VALUES = ENUM$VALUES;
        final int length;
        final Use[] array;
        System.arraycopy(enum$VALUES, 0, array = new Use[length = enum$VALUES.length], 0, length);
        return array;
    }
    
    public static Use valueOf(final String s) {
        return Enum.<Use>valueOf(Use.class, s);
    }
}
