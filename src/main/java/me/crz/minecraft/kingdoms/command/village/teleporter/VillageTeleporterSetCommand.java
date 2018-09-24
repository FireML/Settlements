package me.crz.minecraft.kingdoms.command.village.teleporter;

import me.crz.minecraft.kingdoms.land.OwnedLand;
import org.bukkit.Chunk;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageTeleporterSetCommand extends SubCommand {
    public VillageTeleporterSetCommand() {
        super("sets the teleporter location", "set");
        this.setPermission("village.user");
        this.setUsage("set <here|none>");
    }

    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        if (args.length < 1) {
            MessageUtils.tell(player, this.getUsage(base), null, null);
            return;
        }
        final Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        final Resident res = ResidentManager.getInstance().getResident(player);
        if (args[0].equalsIgnoreCase("none")) {
            MessageUtils.tellResidents(village.getResidents(), String.valueOf(res.getVillageRank().getTag(village.getState())) + " " + player.getName() + "&r&7&l has &r&6&lremoved&r&7&l the teleport claim", null, null);
            village.resetTeleporterClaim();
            VillageManager.getInstance().saveVillage(village);
            return;
        }
        if (!args[0].equalsIgnoreCase("here")) {
            MessageUtils.tell(player, this.getUsage(base), null, null);
            return;
        }
        final Chunk currentChunk = player.getLocation().getChunk();
        final OwnedLand land = LandManager.getInstance().getLand(currentChunk.getX(), currentChunk.getZ(), currentChunk.getWorld().getName());
        if (land == null) {
            MessageUtils.tell(player, "&c&lYou do not own this land!", null, null);
            return;
        }
        MessageUtils.tellResidents(village.getResidents(), String.valueOf(res.getVillageRank().getTag(village.getState())) + " " + player.getName() + "&r&7&l has set the teleport claim at &r&6&l" + String.valueOf(player.getLocation().getBlockX()) + "&r&7&l,&r&6&l" + String.valueOf(player.getLocation().getBlockY()) + "&r&7&l,&r&6&l" + String.valueOf(player.getLocation().getBlockZ()), null, null);
        village.setTeleporterClaim(player.getLocation());
        VillageManager.getInstance().saveVillage(village);
    }
}
