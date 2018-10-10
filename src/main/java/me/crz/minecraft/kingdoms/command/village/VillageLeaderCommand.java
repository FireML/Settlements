package me.crz.minecraft.kingdoms.command.village;

import java.util.Arrays;

import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageLeaderCommand extends SubCommand {
    public VillageLeaderCommand() {
        super("Shows the leader commands", "leader");
        this.setPermission("kingdoms.user");
        this.setUsage("leader help");
    }

    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        if (args.length < 1) {
            MessageUtils.tell(player, this.getUsage(base), null, null);
            return;
        }
        if (ResidentManager.getInstance().getResident(player).getVillageRank().below(VillageRank.ELDER)) {
            MessageUtils.tell(player, "&c&lYou require the rank &r&6&l" + VillageRank.ELDER.getTag(VillageManager.getInstance().getVillageFromPlayer(player).getState()) + " or higher&r&c&l!");
            return;
        }
        MessageUtils.tellList(player, Arrays.asList(
                "&8&l--------&r&c&lLeader Commands&r&8&l--------",
                "&8[&9*&8] &b/s mode &7- changes the mode of the claimed lands",
                "&8[&9*&8] &b/s setaccess &7- changes the access to your settlement",
                "&8[&9*&8] &b/s invite <name> &7- sends an invite to a player",
                "&8[&9*&8] &b/s claim &7- claims the current chunk you are in",
                "&8[&9*&8] &b/s unclaim &7- unclaims the owned land you are standing in",
                "&8[&9*&8] &b/s setrank &7- changes the rank of a player inside settlement",
                "&8[&9*&8] &b/s disband &7- deletes the settlement",
                "&8[&9*&8] &b/s kick <player> &7- kicks a player out of the settlement",
                "&8[&9*&8] &b/s sethome &7- Sets the home for the village",
                "&8[&9*&8] &b/s ally <name> &7- invites an alliance to a settlement",
                "&8[&9*&8] &b/s bank &7- anything related about money in the settlement",
                "&8[&9*&8] &b/s plot &7- plot related commands",
                "&8[&9*&8] &b/s rename <name> &7- Renames the current village to something else",
                "&8[&9*&8] &b/s transfer <name> &7- transfers the leadership of a settlement to someone else",
                "&8[&9*&8] &b/s outpost &7- claims a piece of land away from your original settlement",
                "&8[&9*&8] &b/s exitmessage &7- sets the message shown when exiting claimed land",
                "&8[&9*&8] &b/s entermessage &7- sets the message shown when entering claimed land",
                "&8[&9*&8] &b/s build &7- specifies who can build on your land",
                "&8[&9*&8] &b/s banish &7- banish someonem from your land"));
    }
}
