package de.themoep.settlements.command.plot.sub;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.message.ConfigMessage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import uk.firedev.daisylib.messages.message.Message;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class InfoSubcommand {

    public static @NonNull LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("info")
            .executes(ctx -> {
                Player player = ctx.getSource().getPlayerOrThrow();
                OwnedLand land = LandManager.getInstance().getLand(player.getChunk());
                if (land == null) {
                    ConfigMessage.getError("not-a-claim").send(player);
                    return 1;
                }
                getMessage("general").toListMessage()
                    .replace(Utils.toParsedMap(land.getReplacements()))
                    .replaceWithListInsertion(getInsertionReplacements(land))
                    .send(player);
                return 1;
            });
    }

    private static Map<String, ?> getInsertionReplacements(@NonNull OwnedLand land) {
        // {owner}
        Message<?, ?> owner = land.isPlot()
            ? getMessage("owner").replace("{owner}", land.getPlotOwnerResident().getName())
            : ConfigMessage.emptyListMessage();
        // {plotprice}
        Message<?, ?> price = land.isForSale()
            ? getMessage("price").replace("{price}", land.getPrice())
            : ConfigMessage.emptyListMessage();
        // {plottax}
        Message<?, ?> tax = land.isForSale() || land.isPlot()
            ? getMessage("tax").replace("{tax}", land.getTax())
            : ConfigMessage.emptyListMessage();
        return Map.of(
            "{owner}", owner,
            "{plotprice}", price,
            "{plottax}", tax
        );
    }

    private static Message<?, ?> getMessage(@NonNull String key) {
        return ConfigMessage.get("command.plot.info." + key);
    }

}
