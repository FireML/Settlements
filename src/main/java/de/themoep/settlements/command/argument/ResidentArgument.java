package de.themoep.settlements.command.argument;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class ResidentArgument implements CustomArgumentType.Converted<Resident, String> {

    private static final DynamicCommandExceptionType NOT_FOUND = new DynamicCommandExceptionType(
        obj -> new LiteralMessage("Resident " + obj + " not found.")
    );

    @Override
    public @NonNull Resident convert(@NonNull String nativeType) throws CommandSyntaxException {
        Resident resident = ResidentManager.getInstance().getResident(nativeType);
        if (resident == null) {
            throw NOT_FOUND.create(nativeType);
        }
        return resident;
    }

    @NonNull
    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.string();
    }

    @NonNull
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(@NonNull CommandContext<S> context, @NonNull SuggestionsBuilder builder) {
        Bukkit.getOnlinePlayers().stream()
            .map(Player::getName)
            .filter(name -> name.toLowerCase().startsWith(builder.getRemainingLowerCase()))
            .forEach(builder::suggest);
        return builder.buildFuture();
    }

}
