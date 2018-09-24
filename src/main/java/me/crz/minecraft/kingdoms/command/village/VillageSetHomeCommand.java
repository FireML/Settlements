package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.land.OwnedLand;
import org.bukkit.Chunk;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageSetHomeCommand extends SubCommand
{
    public VillageSetHomeCommand() {
        super("Sets the home for the village", new String[] { "sethome" });
        this.setPermission("village.user");
        this.setUsage("sethome");
    }
    
    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        final Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, MessageUtils.NOT_IN_CURRENT_MESSAGE);
            return;
        }
        final Resident res = ResidentManager.getInstance().getResident(player);
        if (!VillageRank.hasRank(res.getVillageRank(), VillageRank.CHIEF)) {
            MessageUtils.tell(player, "&c&lYou require the rank &r&6&l" + VillageRank.CHIEF.getTag(village.getState()) + " or higher&r&c&l!");
            return;
        }
        final Chunk currentChunk = player.getLocation().getChunk();
        final OwnedLand land = LandManager.getInstance().getLand(currentChunk.getX(), currentChunk.getZ(), currentChunk.getWorld().getName());
        if (land == null || !land.getOwnerVillage().equals(village.getName())) {
            MessageUtils.tell(player, "&c&lYou do not own this land!");
            return;
        }
        MessageUtils.tellResidents(village.getResidents(), String.valueOf(res.getVillageRank().getTag(village.getState())) + " " + player.getName() + "&r&7&l has set the village home at &r&6&l" + String.valueOf(player.getLocation().getBlockX()) + "&r&7&l,&r&6&l" + String.valueOf(player.getLocation().getBlockY()) + "&r&7&l,&r&6&l" + String.valueOf(player.getLocation().getBlockZ()));
        village.setVillageHome(player.getLocation());
        VillageManager.getInstance().saveVillage(village);
    }
}
