package de.themoep.settlements.command.argument;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.themoep.settlements.land.BuildState;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import uk.firedev.daisylib.utils.CommonUtils;

import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class BuildStateArgument implements CustomArgumentType.Converted<BuildState, String> {

    private static final DynamicCommandExceptionType INVALID_STATE = new DynamicCommandExceptionType(
        obj -> new LiteralMessage("Invalid Build State: " + obj)
    );

    @Override
    public @NonNull BuildState convert(@NonNull String nativeType) throws CommandSyntaxException {
        BuildState state = CommonUtils.getEnumValue(BuildState.class, nativeType);
        if (state == null) {
            throw INVALID_STATE.create(nativeType);
        }
        return state;
    }

    @NonNull
    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.string();
    }

    @NonNull
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(@NonNull CommandContext<S> context, @NonNull SuggestionsBuilder builder) {
        if (!(context.getSource() instanceof CommandSourceStack source)) {
            return builder.buildFuture();
        }
        if (!(source.getSender() instanceof Player player)) {
            return builder.buildFuture();
        }
        OwnedLand land = LandManager.getInstance().getLand(player.getChunk());
        if (land == null) {
            return builder.buildFuture();
        }
        BuildState.AppliesTo appliesTo = land.isPlot() ? BuildState.AppliesTo.LAND : BuildState.AppliesTo.SETTLEMENT;
        Arrays.stream(BuildState.values())
            .filter(state -> state.appliesTo(appliesTo))
            .map(state -> state.name().toLowerCase(Locale.ROOT))
            .filter(name -> name.toLowerCase().startsWith(builder.getRemainingLowerCase()))
            .forEach(builder::suggest);
        return builder.buildFuture();
    }

}
