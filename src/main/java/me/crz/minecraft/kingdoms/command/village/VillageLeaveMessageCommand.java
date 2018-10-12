package me.crz.minecraft.kingdoms.command.village;

/*
 * Kingdoms - kingdoms
 * Copyright (c) 2018 Max Lee aka Phoenix616 (mail@moep.tv)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Message;
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
            MessageUtils.tell(player, Message.get("not-in-current"), null, null);
            return;
        }
        if (!this.canChange(village, player)) {
            MessageUtils.tell(player, "&c&lVillage state of &r&6&lkingdom &r&c&lrequired", null, null);
            return;
        }
        final Resident res = ResidentManager.getInstance().getResident(player);
        if (res.getVillageRank().below(VillageRank.ELDER)) {
            MessageUtils.tell(player, Message.get("rank-required"), "rank", VillageRank.ELDER.getTag(village.getState()));
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
        MessageUtils.tell(player, Message.get("success-change-leave"), null, null);
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
