package me.crz.minecraft.kingdoms.command.village;

import java.util.Iterator;
import java.util.HashMap;
import me.crz.minecraft.kingdoms.village.Village;
import org.bson.Document;
import me.crz.minecraft.kingdoms.vault.Levels;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageMaterialsCommand extends SubCommand
{
    public VillageMaterialsCommand() {
        super("Shows you the materials required to upgrade", new String[] { "materials" });
        this.setPermission("village.user");
        this.setUsage("materials");
    }
    
    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        final Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, MessageUtils.NOT_IN_CURRENT_MESSAGE, null, null);
            return;
        }
        if (village.getLevel() == 10) {
            return;
        }
        final HashMap<String, Integer> nextLevelReqs = Levels.getLevelMapFromInt(village.getLevel() + 1, village.getState());
        Document doc = (Document)village.getDocument().get((Object)"vault");
        if (doc == null) {
            doc = new Document();
            village.set("vault", doc);
            VillageManager.getInstance().saveVillage(village);
        }
        final StringBuilder sb = new StringBuilder("&8&l--------&r&c&lRequired Materials&r&8&l--------");
        for (final String mat : nextLevelReqs.keySet()) {
            if (doc.containsKey((Object)mat)) {
                sb.append("\n&e" + ((int)nextLevelReqs.get(mat) - (int)doc.getInteger((Object)mat)) + " &7" + mat);
            }
            else {
                sb.append("\n&e" + nextLevelReqs.get(mat) + " &7" + mat);
            }
        }
        MessageUtils.tell(player, sb.toString(), null, null);
    }
}
