package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.vault.VaultInventory;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageDepositCommand extends SubCommand {
    public VillageDepositCommand() {
        super("Deposit an item into the vault", "deposit");
        this.setPermission("village.user");
        this.setUsage("deposit");
    }

    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        final Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, MessageUtils.Message.NOT_IN_CURRENT, null, null);
            return;
        }
        if (village.getLevel() == 10) {
            return;
        }
        final VaultInventory inv = new VaultInventory(player);
        player.openInventory(inv.getInventory());
    }
}
