package me.crz.minecraft.kingdoms.command.village;

import java.util.List;

import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;

import java.util.ArrayList;

import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageModeCommand extends SubCommand {
    public VillageModeCommand() {
        super("Changes settings for your village claims", "mode");
        this.setPermission("kingdoms.user");
        this.setUsage("mode <" + String.join(" | ", Mode.modes()) + ">");
    }

    @Override
    public void run(Player player, String base, String alias, String... args) {
        if (args.length < 1) {
            MessageUtils.tell(player, this.getUsage(base));
            return;
        }
        Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, Message.get("not-in-current"));
            return;
        }
        Resident res = ResidentManager.getInstance().getResident(player);
        if (res.getVillageRank().below(VillageRank.ELDER)) {
            MessageUtils.tell(player, Message.get("rank-required"), "rank", VillageRank.ELDER.getTag(village.getState()));
            return;
        }
        if (Mode.modes().contains(args[0].toLowerCase())) {
            Mode m = Mode.valueOf(args[0]);
            List<String> optionStrings = new ArrayList<>(m.getOptions().keySet());
            if (args.length > 1) {
                if (m.getOptions().containsKey(args[1].toLowerCase())) {
                    m.set(m.getOptions().get(args[1].toLowerCase()), village);
                    MessageUtils.tell(player, "&7Mode &6" + m.name() + "&7 set to &6" + args[1]);
                } else {
                    MessageUtils.tell(player, String.valueOf(this.getUsage(base)) + " <" + String.join(" | ", optionStrings) + ">");
                }
            } else {
                MessageUtils.tell(player, String.valueOf(this.getUsage(base)) + " <" + optionStrings.get(0) + " | " + optionStrings.get(1) + ">");
            }
        } else {
            MessageUtils.tell(player, this.getUsage(base));
        }
    }
}
