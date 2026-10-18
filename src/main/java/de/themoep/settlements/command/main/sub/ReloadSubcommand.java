package de.themoep.settlements.command.main.sub;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.themoep.settlements.integration.IntegrationManager;
import de.themoep.settlements.utils.config.ConfigManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jspecify.annotations.NonNull;

public class ReloadSubcommand {

    public static @NonNull LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("reload")
            .executes(ctx -> {
                ConfigManager.getInstance().getDataConfig().saveConfig();
                ConfigManager.getInstance().reload(true);
                IntegrationManager.getInstance().reload();
                ctx.getSource().getSender().sendPlainMessage("Successfully reloaded Settlements.");
                return 1;
            });
    }

}
