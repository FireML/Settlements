package de.themoep.settlements.command.argument;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.themoep.settlements.land.UseFlag;
import de.themoep.settlements.land.UseFlagManager;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class UseFlagArgument implements CustomArgumentType.Converted<UseFlag, String> {

    private static final DynamicCommandExceptionType INVALID_FLAG = new DynamicCommandExceptionType(
        obj -> new LiteralMessage("Invalid Use Flag: " + obj)
    );

    @Override
    public @NonNull UseFlag convert(@NonNull String nativeType) throws CommandSyntaxException {
        UseFlag flag = UseFlagManager.getInstance().get(nativeType);
        if (flag == null) {
            throw INVALID_FLAG.create(nativeType);
        }
        return flag;
    }

    @NonNull
    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.string();
    }

    @NonNull
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(@NonNull CommandContext<S> context, @NonNull SuggestionsBuilder builder) {
        UseFlagManager.getInstance().getUseFlags().stream()
            .map(UseFlag::name)
            .filter(name -> name.toLowerCase().startsWith(builder.getRemainingLowerCase()))
            .forEach(builder::suggest);
        return builder.buildFuture();
    }

}
