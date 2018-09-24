package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import me.crz.minecraft.kingdoms.db.TownCache;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageAllyCommand extends SubCommand
{
    public VillageAllyCommand() {
        super("Ally another village", new String[] { "ally" });
        this.setPermission("village.user");
        this.setUsage("ally <name>");
    }
    
    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        if (args.length < 1) {
            MessageUtils.tell(player, this.getUsage(base));
            return;
        }
        final Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, MessageUtils.NOT_IN_CURRENT_MESSAGE);
            return;
        }
        if (!this.canAlly(ResidentManager.getInstance().getResident(player))) {
            MessageUtils.tell(player, "&c&lYou require the rank &r&6&l" + VillageRank.ELDER.getTag(village.getState()) + " or higher&r&c&l!");
            return;
        }
        final Village invitedVillage = VillageManager.getInstance().getVillageFromName(args[0].toLowerCase());
        if (invitedVillage == null) {
            MessageUtils.tell(player, "&c&lVillage &r&6&l" + args[0] + "&r&c&l doesn't exist!");
            return;
        }
        if (invitedVillage.getName().equals(village.getName())) {
            MessageUtils.tell(player, "&c&lYou can't ally yourself!");
            return;
        }
        if (village.isAlly(invitedVillage)) {
            MessageUtils.tell(player, "&c&lYou are already allied to Village &r&6&l" + args[0]);
            return;
        }
        if (TownCache.getPendingTownAlly().containsKey(village.getName())) {
            final List<String> invites = (List<String>)TownCache.getPendingTownAlly().get(village.getName());
            if (invites.contains(invitedVillage.getName())) {
                MessageUtils.tellResidents(invitedVillage.getResidents(), "&7&lYou are now allied to &r&6&l" + village.getName());
                MessageUtils.tellResidents(village.getResidents(), "&7&lYou are now allied to &r&6&l" + invitedVillage.getName());
                invitedVillage.addAlly(village.getName());
                village.addAlly(invitedVillage.getName());
                VillageManager.getInstance().saveVillage(invitedVillage);
                VillageManager.getInstance().saveVillage(village);
            }
        }
        else {
            if (TownCache.getPendingTownAlly().containsKey(invitedVillage.getName())) {
                final List<String> iInvites = (List<String>)TownCache.getPendingTownAlly().get(invitedVillage.getName());
                iInvites.add(village.getName());
                MessageUtils.tellResidents(invitedVillage.getResidents(), "&r&6&l" + village.getName() + " &r&7&lwants to ally!");
                MessageUtils.tellResidents(invitedVillage.getResidents(), "&r&6&l/s ally " + village.getName() + " &r&7&lto ally them!");
            }
            else {
                TownCache.getPendingTownAlly().put(invitedVillage.getName(), new ArrayList<String>((Collection<? extends String>)Arrays.<String>asList(village.getName())));
                MessageUtils.tellResidents(invitedVillage.getResidents(), "&r&6&l" + village.getName() + " &r&7&lwants to ally!");
            }
            MessageUtils.tell(player, "&7&lAlly request sent to &r&6&l" + invitedVillage.getName());
        }
    }
    
    private boolean canAlly(final Resident res) {
        return VillageRank.hasRank(res.getVillageRank(), VillageRank.ELDER);
    }
}
