package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.db.TownCache;
import org.bukkit.Bukkit;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageInviteCommand extends VillageSubCommand {
    public VillageInviteCommand(SubCommand parent) {
        super(parent, "invite <player>", VillageRank.RECRUITER);
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        Player invitedPlayer = Bukkit.getPlayerExact(args[0]);
        if (invitedPlayer == null) {
            res.tell(getError("player-not-found"), "name", args[0]);
            return true;
        }
        Village playerVillage = VillageManager.getInstance().getVillageFromPlayer(invitedPlayer);
        if (res.getVillage().equals(playerVillage)) {
            res.tell(getMessage("already-in-village"), "name", invitedPlayer.getName(), "state", res.getVillage().getState().getTag());
            return true;
        }
        TownCache.getPendingTownInvites().put(invitedPlayer.getUniqueId(), res.getVillage().getId());
        MessageUtils.tell(invitedPlayer, getMessage("received"), "name", res.getVillage().getName(), "state", res.getVillage().getState().getTag());
        MessageUtils.tellResidents(res.getVillage().getOnlineResidents(), getMessage("alert"), "player", invitedPlayer.getName());
        return true;
    }
}
