package me.crz.minecraft.kingdoms.command.village.teleporter;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageTeleporterCostCommand extends SubCommand {
    public VillageTeleporterCostCommand() {
        super("view or set the teleporter cost", "cost");
        this.setPermission("village.user");
        this.setUsage("cost <view|set> <value>");
    }

    @Override
    public void run(Player player, String base, String alias, String... args) {
        if (args.length < 1) {
            MessageUtils.tell(player, this.getUsage(base), null, null);
            return;
        }
        Village village = VillageManager.getInstance().getVillageFromPlayer(player);
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
        Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(village));
        MessageUtils.tell(player, "&2&lVillage teleport cost set to: &r&6&l" + village.getTeleportCost());
    }

    public boolean isInt(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
