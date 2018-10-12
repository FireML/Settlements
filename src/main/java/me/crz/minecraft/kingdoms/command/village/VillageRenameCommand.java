package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageRenameCommand extends SubCommand {
    String allowed;

    public VillageRenameCommand() {
        super("Renames the current village to something else", "rename");
        allowed = "abcdefghijklmnopqrstuvwxyz0123456789_-";
        this.setPermission("kingdoms.user");
        this.setUsage("rename <name>");
    }

    @Override
    public void run(Player player, String base, String alias, String... args) {
        Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, Message.get("not-in-current"));
            return;
        }
        Resident res = ResidentManager.getInstance().getResident(player);
        if (res.getVillageRank().below(VillageRank.CHIEF)) {
            MessageUtils.tell(player, Message.get("rank-required"), "rank", VillageRank.CHIEF.getTag(village.getState()));
            return;
        }
        if (args.length == 0) {
            MessageUtils.tell(player, this.getUsage(base));
            return;
        }
        if (VillageManager.getInstance().getVillageFromName(args[0]) != null) {
            MessageUtils.tell(player, Message.get("error-village-exists"), "name", args[0]);
            return;
        }
        boolean invalid = false;
        String[] split;
        for (int length = (split = args[0].split("")).length, i = 0; i < length; ++i) {
            String s = split[i];
            if (!allowed.contains(s.toLowerCase())) {
                invalid = true;
                break;
            }
        }
        if (invalid) {
            MessageUtils.tell(player, "&cThat name contains invalid charecters", null, null);
            return;
        }
        Utils.runAsync(() -> {
            village.setName(args[0]);
            village.setEnterMessage(Message.get("default-enter").getReplaced("owner", village.getName()));
            village.setLeaveMessage(Message.get("default-leave").getReplaced("owner", village.getName()));
            DatabaseManager.getInstance().saveVillage(village);
            MessageUtils.tellResidents(village.getOnlineResidents(), Message.get("village-renamed"), "name", village.getName());
        });
    }
}
