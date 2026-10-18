package de.themoep.settlements.command.argument;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.themoep.settlements.rank.SettlementRank;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class SettlementRankArgument implements CustomArgumentType.Converted<SettlementRank, String> {

    private static final DynamicCommandExceptionType INVALID_RANK = new DynamicCommandExceptionType(
        obj -> new LiteralMessage("Invalid Settlement Rank: " + obj)
    );

    @Override
    public @NonNull SettlementRank convert(@NonNull String nativeType) throws CommandSyntaxException {
        SettlementRank rank = SettlementRank.get(nativeType);
        if (rank == null) {
            throw INVALID_RANK.create(nativeType);
        }
        return rank;
    }

    @NonNull
    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.string();
    }

    @NonNull
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(@NonNull CommandContext<S> context, @NonNull SuggestionsBuilder builder) {
        Arrays.stream(SettlementRank.values())
            .map(SettlementRank::name)
            .filter(name -> name.toLowerCase().startsWith(builder.getRemainingLowerCase()))
            .forEach(builder::suggest);
        return builder.buildFuture();
    }

}
