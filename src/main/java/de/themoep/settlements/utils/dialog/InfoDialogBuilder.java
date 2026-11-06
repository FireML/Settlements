package de.themoep.settlements.utils.dialog;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.config.message.ConfigMessage;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.dialog.DialogLike;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import uk.firedev.daisylib.messages.message.ComponentMessage;
import uk.firedev.daisylib.messages.replacer.Replacer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@NullMarked
public class InfoDialogBuilder {

    private final Replacer replacer = Replacer.component();
    private Component title = Component.text("Information");
    private final List<Object> content = new ArrayList<>();

    public InfoDialogBuilder() {}

    public static InfoDialogBuilder players(List<Player> players, Function<Player, Component> func) {
        if (players.isEmpty()) {
            return new InfoDialogBuilder();
        }
        ComponentMessage<?, ?> entry = ConfigMessage.get("dialog.info.player");
        InfoDialogBuilder builder = new InfoDialogBuilder();
        players.forEach(player -> {
            Replacer replacer = Replacer.component()
                .addReplacement("{icon}", Component.object(player.asObjectContents()))
                .addReplacement("{name}", player.name())
                .addReplacement("{data}", func.apply(player));
            builder.addContent(entry.replace(replacer));
        });
        return builder;
    }

    public static InfoDialogBuilder players(List<Player> players) {
        if (players.isEmpty()) {
            return new InfoDialogBuilder();
        }
        InfoDialogBuilder builder = new InfoDialogBuilder();
        players.forEach(player -> {
            TextComponent.Builder info = Component.text();
            info.append(Component.object(player.asObjectContents()));
            info.appendSpace();
            info.append(player.name());
            builder.addContent(info.build());
        });
        return builder;
    }

    public static InfoDialogBuilder residents(List<Resident> residents, Function<Resident, Component> func) {
        if (residents.isEmpty()) {
            return new InfoDialogBuilder();
        }
        ComponentMessage<?, ?> entry = ConfigMessage.get("dialog.info.player");
        InfoDialogBuilder builder = new InfoDialogBuilder();
        residents.forEach(resident -> {
            Replacer replacer = Replacer.component()
                .addReplacement("{icon}", Component.object(resident.asObjectContents()))
                .addReplacement("{name}", resident.getName())
                .addReplacement("{data}", func.apply(resident));
            builder.addContent(entry.replace(replacer));
        });
        return builder;
    }

    public static InfoDialogBuilder residents(List<Resident> residents) {
        if (residents.isEmpty()) {
            return new InfoDialogBuilder();
        }
        InfoDialogBuilder builder = new InfoDialogBuilder();
        residents.forEach(resident -> {
            TextComponent.Builder info = Component.text();
            info.append(Component.object(resident.asObjectContents()));
            info.appendSpace();
            info.append(Component.text(resident.getName()));
            builder.addContent(info.build());
        });
        return builder;
    }

    // Class Things

    public InfoDialogBuilder addReplacer(Replacer replacer) {
        this.replacer.merge(replacer);
        return this;
    }

    public InfoDialogBuilder addReplacement(String variable, Object replacement) {
        this.replacer.addReplacement(variable, replacement);
        return this;
    }

    public InfoDialogBuilder addReplacements(Map<String, Object> replacements) {
        this.replacer.addReplacements(replacements);
        return this;
    }

    public Replacer getReplacer() {
        return this.replacer;
    }

    public InfoDialogBuilder withTitle(Object title) {
        this.title = ComponentMessage.componentMessage(title).get();
        return this;
    }

    public InfoDialogBuilder withContent(List<?> content) {
        this.content.clear();
        this.content.addAll(content);
        return this;
    }

    public InfoDialogBuilder addContent(Object content) {
        this.content.add(content);
        return this;
    }

    public InfoDialogBuilder addContent(List<?> content) {
        this.content.addAll(content);
        return this;
    }

    // Building

    public DialogLike build() {
        return Dialog.create(builder -> builder.empty()
            .base(
                DialogBase.builder(ComponentMessage.componentMessage(title).replace(replacer).get())
                    .canCloseWithEscape(true)
                    .afterAction(DialogBase.DialogAfterAction.NONE)
                    .body(getBodies())
                    .pause(false)
                    .build()
            )
            .type(DialogType.notice(
                ActionButton.builder(Component.text("Exit")).build()
            ))
        );
    }

    public void open(Audience audience) {
        audience.showDialog(build());
    }

    private List<? extends DialogBody> getBodies() {
        return ComponentMessage.componentMessage(content).replace(replacer).get().stream()
            .map(DialogBody::plainMessage)
            .toList();
    }

}