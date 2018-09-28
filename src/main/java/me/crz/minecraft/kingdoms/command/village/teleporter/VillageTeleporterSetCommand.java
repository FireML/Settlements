package me.crz.minecraft.kingdoms.command.village.teleporter;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.Utils;
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
    public void run(Player player, String base, String alias, String... args) {
        if (args.length < 1) {
            MessageUtils.tell(player, this.getUsage(base), null, null);
            return;
        }
        Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        Resident res = ResidentManager.getInstance().getResident(player);
        if (args[0].equalsIgnoreCase("none")) {
            MessageUtils.tellResidents(village.getOnlineResidents(), String.valueOf(res.getVillageRank().getTag(village.getState())) + " " + player.getName() + "&r&7&l has &r&6&lremoved&r&7&l the teleport claim", null, null);
            village.resetTeleporter();
            Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(village));
        } else if (args[0].equalsIgnoreCase("here")) {
            Chunk currentChunk = player.getLocation().getChunk();
            OwnedLand land = LandManager.getInstance().getLand(currentChunk);
            if (land == null) {
                MessageUtils.tell(player, "&c&lYou do not own this land!", null, null);
                return;
            }
            MessageUtils.tellResidents(village.getOnlineResidents(), String.valueOf(res.getVillageRank().getTag(village.getState())) + " " + player.getName() + "&r&7&l has set the teleport claim at &r&6&l" + String.valueOf(player.getLocation().getBlockX()) + "&r&7&l,&r&6&l" + String.valueOf(player.getLocation().getBlockY()) + "&r&7&l,&r&6&l" + String.valueOf(player.getLocation().getBlockZ()), null, null);
            village.setTeleporter(player.getLocation());
            Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(village));
        } else {
            MessageUtils.tell(player, this.getUsage(base), null, null);
        }
    }
}
