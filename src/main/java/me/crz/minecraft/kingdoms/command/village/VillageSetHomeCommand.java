package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.Utils;
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

public class VillageSetHomeCommand extends SubCommand {
    public VillageSetHomeCommand() {
        super("Sets the home for the village", "sethome");
        this.setPermission("village.user");
        this.setUsage("sethome");
    }

    @Override
    public void run(Player player, String base, String alias, String... args) {
        Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, MessageUtils.NOT_IN_CURRENT_MESSAGE);
            return;
        }
        Resident res = ResidentManager.getInstance().getResident(player);
        if (!VillageRank.hasRank(res.getVillageRank(), VillageRank.CHIEF)) {
            MessageUtils.tell(player, "&c&lYou require the rank &r&6&l" + VillageRank.CHIEF.getTag(village.getState()) + " or higher&r&c&l!");
            return;
        }
        Chunk currentChunk = player.getLocation().getChunk();
        OwnedLand land = LandManager.getInstance().getLand(currentChunk);
        if (land == null || !land.getOwnerVillage().equals(village.getName())) {
            MessageUtils.tell(player, "&c&lYou do not own this land!");
            return;
        }
        MessageUtils.tellResidents(village.getOnlineResidents(), String.valueOf(res.getVillageRank().getTag(village.getState())) + " " + player.getName() + "&r&7&l has set the village home at &r&6&l" + String.valueOf(player.getLocation().getBlockX()) + "&r&7&l,&r&6&l" + String.valueOf(player.getLocation().getBlockY()) + "&r&7&l,&r&6&l" + String.valueOf(player.getLocation().getBlockZ()));
        village.setVillageHome(player.getLocation());
        Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(village));
    }
}
