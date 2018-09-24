package me.crz.minecraft.kingdoms.command.village.teleporter;

import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageTeleporterCostCommand extends SubCommand
{
    public VillageTeleporterCostCommand() {
        super("view or set the teleporter cost", new String[] { "cost" });
        this.setPermission("village.user");
        this.setUsage("cost <view|set> <value>");
    }
    
    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        if (args.length < 1) {
            MessageUtils.tell(player, this.getUsage(base), null, null);
            return;
        }
        final Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (args[0].equalsIgnoreCase("view")) {
            MessageUtils.tell(player, "&2&lVillage teleport cost: &r&6&l" + village.getTeleportCost());
            return;
        }
        if (!args[0].equalsIgnoreCase("set")) {
            MessageUtils.tell(player, this.getUsage(base), null, null);
            return;
        }
        if (args.length < 2 || !this.isInt(args[1])) {
            MessageUtils.tell(player, "&c&lPlease specify an amount to set the teleport cost to!");
            return;
        }
        village.setTeleporterCost(Integer.parseInt(args[1]));
        VillageManager.getInstance().saveVillage(village);
        MessageUtils.tell(player, "&2&lVillage teleport cost set to: &r&6&l" + village.getTeleportCost());
    }
    
    public boolean isInt(final String input) {
        try {
            Integer.parseInt(input);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }
}
