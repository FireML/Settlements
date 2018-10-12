package me.crz.minecraft.kingdoms.command.village;

import java.util.Map;

import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.vault.Levels;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageMaterialsCommand extends SubCommand {
    public VillageMaterialsCommand() {
        super("Shows you the materials required to upgrade", "materials");
        this.setPermission("kingdoms.user");
        this.setUsage("materials");
    }

    @Override
    public void run(Player player, String base, String alias, String... args) {
        Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, Message.get("not-in-current"), null, null);
            return;
        }
        if (village.getLevel() == 10) {
            return;
        }
        Map<String, Integer> nextLevelReqs = Levels.getLevelMapFromInt(village.getLevel() + 1, village.getState());
        StringBuilder sb = new StringBuilder("&8&l--------&r&c&lRequired Materials&r&8&l--------");
        if (nextLevelReqs != null) {
            for (String mat : nextLevelReqs.keySet()) {
                if (village.getVault().containsKey(mat)) {
                    sb.append("\n&e").append(nextLevelReqs.get(mat) - village.getVault().get(mat)).append(" &7").append(mat);
                } else {
                    sb.append("\n&e").append(nextLevelReqs.get(mat)).append(" &7").append(mat);
                }
            }
        }
        MessageUtils.tell(player, sb.toString(), null, null);
    }
}
