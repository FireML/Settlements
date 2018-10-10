package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.VillageState;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.ChatColor;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageLeaveMessageCommand extends SubCommand {
    public VillageLeaveMessageCommand() {
        super("Changes what players see when leaving your claims", "leavemessage");
        this.setPermission("kingdoms.user");
        this.setUsage("leavemessage <message>");
    }

    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        final Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, MessageUtils.Message.NOT_IN_CURRENT, null, null);
            return;
        }
        if (!this.canChange(village, player)) {
            MessageUtils.tell(player, "&c&lVillage state of &r&6&lkingdom &r&c&lrequired", null, null);
            return;
        }
        final Resident res = ResidentManager.getInstance().getResident(player);
        if (res.getVillageRank().below(VillageRank.ELDER)) {
            MessageUtils.tell(player, MessageUtils.Message.RANK_REQUIRED, "rank", VillageRank.ELDER.getTag(village.getState()));
            return;
        }
        if (args.length <= 2) {
            player.sendMessage(ChatColor.RED + this.getUsage(base));
            return;
        }
        String message = this.getMessage(args);
        if (!message.contains("{owner}")) {
            MessageUtils.tell(player, ChatColor.RED + "Message must contain " + ChatColor.YELLOW + "{owner}" + ChatColor.RED + " variable.", null, null);
            return;
        }
        message = message.replace("{owner}", village.getName());
        message = ChatColor.translateAlternateColorCodes('&', message);
        village.setLeaveMessage(message);
        Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(village));
        MessageUtils.tell(player, MessageUtils.Message.SUCCESS_CHANGE_LEAVE, null, null);
    }

    private String getMessage(final String[] args) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < args.length; ++i) {
            builder.append(String.valueOf(args[i])).append(" ");
        }
        return builder.toString();
    }

    private boolean canChange(final Village village, final Player player) {
        return village.getState() == VillageState.KINGDOM;
    }
}
