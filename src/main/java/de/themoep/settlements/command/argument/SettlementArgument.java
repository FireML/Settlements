package de.themoep.settlements.command.argument;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class SettlementArgument implements CustomArgumentType.Converted<Settlement, String> {

    private static final DynamicCommandExceptionType NOT_A_SETTLEMENT = new DynamicCommandExceptionType(
        obj -> new LiteralMessage(obj + " is not a Settlement!")
    );

    @Override
    public @NonNull Settlement convert(@NonNull String nativeType) throws CommandSyntaxException {
        Settlement settlement = SettlementManager.getInstance().getFromName(nativeType);
        if (settlement == null) {
            throw NOT_A_SETTLEMENT.create(nativeType);
        }
        return settlement;
    }

    @NonNull
    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.string();
    }

    @NonNull
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(@NonNull CommandContext<S> context, @NonNull SuggestionsBuilder builder) {
        SettlementManager.getInstance().getSettlements().stream()
            .map(Settlement::getName)
            .filter(name -> name.toLowerCase().startsWith(builder.getRemainingLowerCase()))
            .forEach(builder::suggest);
        return builder.buildFuture();
    }

}
