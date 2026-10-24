package de.themoep.settlements.command.plot.sub;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.LandUtils;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.message.ConfigMessage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import uk.firedev.daisylib.external.vault.VaultWrapper;
import uk.firedev.daisylib.utils.CooldownHelper;

import java.time.Duration;
import java.util.UUID;

public class BuySubcommand {

    private static final CooldownHelper TAX_CONFIRMATION = CooldownHelper.cooldownHelper();

    public static @NonNull LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("buy")
            .executes(ctx -> {
                Player player = ctx.getSource().getPlayerOrThrow();
                Resident resident = ResidentManager.getInstance().getResident(player);
                OwnedLand land = LandManager.getInstance().getLand(player.getChunk());
                if (land == null) {
                    ConfigMessage.getError("not-a-claim").send(player);
                    return 1;
                } else if (!(land.isEmbassy() || land.isOwnedBy(resident))) {
                    ConfigMessage.getError("not-your-settlement")
                        .replace("{state}", land.getSettlement().getStateTag())
                        .send(player);
                    return 1;
                } else if (!land.isForSale() || land.getSettlement().isAbandoned()) {
                    ConfigMessage.getError("not-for-sale").send(player);
                    return 1;
                }

                if (land.isEmbassy() && land.isOwnedBy(resident)) {
                    ConfigMessage.getError("cant-buy-embassy")
                        .replace("{state}", resident.getSettlement().getStateTag())
                        .send(player);
                    return 1;
                }
                if (land.getTax() > 0) {
                    UUID uuid = player.getUniqueId();
                    if (!TAX_CONFIRMATION.has(uuid)) {
                        TAX_CONFIRMATION.apply(uuid, Duration.ofMinutes(1));
                        ConfigMessage.get("command.plot.buy.tax-warning")
                            .replace("{tax}", land.getTax())
                            .send(player);
                        return 1;
                    }
                    TAX_CONFIRMATION.remove(uuid);
                }
                if (land.getPrice() > 0) {
                    if (!VaultWrapper.get().getEconomy().has(player, land.getPrice())) {
                        ConfigMessage.getError("not-enough-money")
                            .replace("{amount}", land.getPrice())
                            .send(player);
                        return 1;
                    }
                    VaultWrapper.get().getEconomy().withdrawPlayer(player, land.getPrice());
                    land.getSettlement().setBank(land.getSettlement().getBank() + land.getPrice());
                    Utils.runAsync(() -> land.getSettlement().save());
                }
                land.setForSale(false);
                ConfigMessage.get("command.plot.buy.purchased")
                    .replace("{price}", land.getPrice())
                    .send(player);
                LandManager.getInstance().setPlotOwner(land, player.getUniqueId());
                return 1;
            });

    }

}
