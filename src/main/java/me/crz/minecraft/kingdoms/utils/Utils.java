package me.crz.minecraft.kingdoms.utils;

import me.crz.minecraft.kingdoms.village.VillageState;
import me.crz.minecraft.kingdoms.db.TownCache;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.ArrayList;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import java.util.List;
import org.bukkit.World;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.EntityHuman;
import org.bukkit.craftbukkit.v1_12_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.OfflinePlayer;

public class Utils
{
    private static final int CHUNK_VILLAGE_LEVEL_ONE;
    private static final int CHUNK_VILLAGE_LEVEL_TWO;
    private static final int CHUNK_VILLAGE_LEVEL_THREE;
    private static final int CHUNK_VILLAGE_LEVEL_FOUR;
    private static final int CHUNK_VILLAGE_LEVEL_FIVE;
    private static final int CHUNK_KINGDOM_LEVEL_ONE;
    private static final int CHUNK_KINGDOM_LEVEL_TWO;
    private static final int CHUNK_KINGDOM_LEVEL_THREE;
    private static final int CHUNK_KINGDOM_LEVEL_FOUR;
    private static final int CHUNK_KINGDOM_LEVEL_FIVE;
    private static final int CHUNK_KINGDOM_LEVEL_SIX;
    private static final int CHUNK_KINGDOM_LEVEL_SEVEN;
    private static final int CHUNK_KINGDOM_LEVEL_EIGHT;
    private static final int CHUNK_KINGDOM_LEVEL_NINE;
    private static final int CHUNK_KINGDOM_LEVEL_TEN;
    
    static {
        CHUNK_VILLAGE_LEVEL_ONE = getChunkFromConfig("village-level-one-chunks");
        CHUNK_VILLAGE_LEVEL_TWO = getChunkFromConfig("village-level-two-chunks");
        CHUNK_VILLAGE_LEVEL_THREE = getChunkFromConfig("village-level-three-chunks");
        CHUNK_VILLAGE_LEVEL_FOUR = getChunkFromConfig("village-level-four-chunks");
        CHUNK_VILLAGE_LEVEL_FIVE = getChunkFromConfig("village-level-five-chunks");
        CHUNK_KINGDOM_LEVEL_ONE = getChunkFromConfig("kingdom-level-one-chunks");
        CHUNK_KINGDOM_LEVEL_TWO = getChunkFromConfig("kingdom-level-two-chunks");
        CHUNK_KINGDOM_LEVEL_THREE = getChunkFromConfig("kingdom-level-three-chunks");
        CHUNK_KINGDOM_LEVEL_FOUR = getChunkFromConfig("kingdom-level-four-chunks");
        CHUNK_KINGDOM_LEVEL_FIVE = getChunkFromConfig("kingdom-level-five-chunks");
        CHUNK_KINGDOM_LEVEL_SIX = getChunkFromConfig("kingdom-level-six-chunks");
        CHUNK_KINGDOM_LEVEL_SEVEN = getChunkFromConfig("kingdom-level-seven-chunks");
        CHUNK_KINGDOM_LEVEL_EIGHT = getChunkFromConfig("kingdom-level-eight-chunks");
        CHUNK_KINGDOM_LEVEL_NINE = getChunkFromConfig("kingdom-level-nine-chunks");
        CHUNK_KINGDOM_LEVEL_TEN = getChunkFromConfig("kingdom-level-ten-chunks");
    }
    
    public Utils() {
        super();
    }
    
    public static ItemStack getHead(final OfflinePlayer player, final String name) {
        final ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
        final SkullMeta skull = (SkullMeta)item.getItemMeta();
        skull.setDisplayName(name);
        skull.setOwner(player.getName());
        item.setItemMeta((ItemMeta)skull);
        return item;
    }
    
    public static void handleInventoryChange(final Player p) {
        final EntityPlayer entityPlayer = ((CraftPlayer)p).getHandle();
        if (entityPlayer.activeContainer != entityPlayer.defaultContainer) {
            CraftEventFactory.handleInventoryCloseEvent((EntityHuman)entityPlayer);
            entityPlayer.n();
        }
    }
    
    public static boolean playerIsElder(final Player player) {
        return ResidentManager.getInstance().getResident(player).getVillageRank().equals(VillageRank.ELDER);
    }
    
    public static boolean isWorldClaimable(final World world) {
        return !getDisabledClaimingWorlds().contains(world.getName());
    }
    
    public static List<String> getDisabledClaimingWorlds() {
        final FileConfiguration config = ConfigManager.getInstance().getConfig();
        if (config.contains("disabled-claiming-worlds")) {
            return (List<String>)config.getStringList("disabled-claiming-worlds");
        }
        final List<String> temp = new ArrayList<String>();
        config.set("disabled-claiming-worlds", (Object)temp);
        ConfigManager.getInstance().save(ConfigManager.getInstance().configf, config);
        return temp;
    }
    
    public static int getVillageCreationCost() {
        if (ConfigManager.getInstance().getConfig().contains("village-creation-cost")) {
            return ConfigManager.getInstance().getConfig().getInt("village-creation-cost");
        }
        return 0;
    }
    
    public static boolean playerIsChief(final Player player, final Village village) {
        return village != null && village.getChief().equals(player.getUniqueId());
    }
    
    public static boolean playerIsBuilder(final Player player) {
        return ResidentManager.getInstance().getResident(player).getVillageRank().equals(VillageRank.BUILDER);
    }
    
    public static boolean playerInSameVillage(final Village sender, final Village other) {
        return other != null && sender.getName().equals(other.getName());
    }
    
    public static boolean playerIsRecruiter(final Player player) {
        return ResidentManager.getInstance().getResident(player).getVillageRank().equals(VillageRank.RECRUITER);
    }
    
    public static boolean playerCanKick(final Player player, final Village village) {
        return playerIsElder(player) || playerIsRecruiter(player) || village.getChief().equals(player.getUniqueId());
    }
    
    public static boolean playerHasInvite(final Player player, final String joinVillageName) {
        return TownCache.getPendingTownInvites().containsKey(player.getUniqueId()) && ((String)TownCache.getPendingTownInvites().get(player.getUniqueId())).equals(joinVillageName);
    }
    
    private static int getChunkFromConfig(final String levelPath) {
        return ConfigManager.getInstance().getConfig().getInt(levelPath);
    }
    
    public static int getChunkMax(final int level, final VillageState state) {
        if (level == 1 && state == VillageState.VILLAGE) {
            return CHUNK_VILLAGE_LEVEL_ONE;
        }
        if (level == 2 && state == VillageState.VILLAGE) {
            return CHUNK_VILLAGE_LEVEL_TWO;
        }
        if (level == 3 && state == VillageState.VILLAGE) {
            return CHUNK_VILLAGE_LEVEL_THREE;
        }
        if (level == 4 && state == VillageState.VILLAGE) {
            return CHUNK_VILLAGE_LEVEL_FOUR;
        }
        if (level == 5 && state == VillageState.VILLAGE) {
            return CHUNK_VILLAGE_LEVEL_FIVE;
        }
        if (level == 1 && state == VillageState.KINGDOM) {
            return CHUNK_KINGDOM_LEVEL_ONE;
        }
        if (level == 2 && state == VillageState.KINGDOM) {
            return CHUNK_KINGDOM_LEVEL_TWO;
        }
        if (level == 3 && state == VillageState.KINGDOM) {
            return CHUNK_KINGDOM_LEVEL_THREE;
        }
        if (level == 4 && state == VillageState.KINGDOM) {
            return CHUNK_KINGDOM_LEVEL_FOUR;
        }
        if (level == 5 && state == VillageState.KINGDOM) {
            return CHUNK_KINGDOM_LEVEL_FIVE;
        }
        if (level == 6 && state == VillageState.KINGDOM) {
            return CHUNK_KINGDOM_LEVEL_SIX;
        }
        if (level == 7 && state == VillageState.KINGDOM) {
            return CHUNK_KINGDOM_LEVEL_SEVEN;
        }
        if (level == 8 && state == VillageState.KINGDOM) {
            return CHUNK_KINGDOM_LEVEL_EIGHT;
        }
        if (level == 9 && state == VillageState.KINGDOM) {
            return CHUNK_KINGDOM_LEVEL_NINE;
        }
        if (level == 10 && state == VillageState.KINGDOM) {
            return CHUNK_KINGDOM_LEVEL_TEN;
        }
        return 0;
    }
    
    public static void addDisabledWorld(final World world) {
        final FileConfiguration config = ConfigManager.getInstance().getConfig();
        final List<String> temp = getDisabledClaimingWorlds();
        if (!temp.contains(world.getName())) {
            temp.add(world.getName());
            config.set("disabled-claiming-worlds", (Object)temp);
            ConfigManager.getInstance().save(ConfigManager.getInstance().configf, config);
        }
    }
    
    public static void removeDisabledWorld(final World world) {
        final FileConfiguration config = ConfigManager.getInstance().getConfig();
        final List<String> temp = getDisabledClaimingWorlds();
        if (temp.contains(world.getName())) {
            temp.remove(world.getName());
            config.set("disabled-claiming-worlds", (Object)temp);
            ConfigManager.getInstance().save(ConfigManager.getInstance().configf, config);
        }
    }
    
    public static int getChunkMaxOutpost(final int level, final VillageState state) {
        if (level <= 3 && level >= 1 && state.equals(VillageState.VILLAGE)) {
            return 1;
        }
        if (level <= 5 && level >= 4 && state.equals(VillageState.VILLAGE)) {
            return 2;
        }
        if (level <= 2 && level >= 1 && state.equals(VillageState.KINGDOM)) {
            return 4;
        }
        if (level <= 5 && level >= 3 && state.equals(VillageState.KINGDOM)) {
            return 6;
        }
        if (level <= 8 && level >= 6 && state.equals(VillageState.KINGDOM)) {
            return 8;
        }
        if (level <= 10 && level >= 9 && state.equals(VillageState.KINGDOM)) {
            return 12;
        }
        return 4;
    }
}
