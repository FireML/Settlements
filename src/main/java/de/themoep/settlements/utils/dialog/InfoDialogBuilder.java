package de.themoep.settlements.utils.dialog;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.dialog.DialogLike;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;
import uk.firedev.daisylib.messages.message.ComponentMessage;
import uk.firedev.daisylib.messages.replacer.Replacer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InfoDialogBuilder {

    private final Replacer replacer = Replacer.replacer();
    private @NonNull Component title = Component.text("Information");
    private final @NonNull List<Object> content = new ArrayList<>();

    public InfoDialogBuilder() {}

    // Class Things

    public InfoDialogBuilder addReplacer(@NonNull Replacer replacer) {
        this.replacer.merge(replacer);
        return this;
    }

    public InfoDialogBuilder addReplacement(@NonNull String variable, @NonNull Object replacement) {
        this.replacer.addReplacement(variable, replacement);
        return this;
    }

    public InfoDialogBuilder addReplacements(@NonNull Map<String, Object> replacements) {
        this.replacer.addReplacements(replacements);
        return this;
    }

    public @NonNull Replacer getReplacer() {
        return this.replacer;
    }

    public InfoDialogBuilder withTitle(@NonNull Object title) {
        this.title = ComponentMessage.componentMessage(title).get();
        return this;
    }

    public InfoDialogBuilder withContent(@NonNull List<?> content) {
        this.content.clear();
        this.content.addAll(content);
        return this;
    }

    public InfoDialogBuilder addContent(@NonNull Object content) {
        this.content.add(content);
        return this;
    }

    public InfoDialogBuilder addContent(@NonNull List<?> content) {
        this.content.addAll(content);
        return this;
    }

    // Building

    public DialogLike build() {
        return Dialog.create(builder -> builder.empty()
            .base(
                DialogBase.builder(ComponentMessage.componentMessage(title).replace(replacer).get())
                    .canCloseWithEscape(true)
                    .afterAction(DialogBase.DialogAfterAction.CLOSE)
                    .body(getBodies())
                    .build()
            )
            .type(DialogType.notice(
                ActionButton.builder(Component.text("Exit")).build()
            ))
        );
    }

    public void open(@NonNull Audience audience) {
        audience.showDialog(build());
    }

    private List<? extends DialogBody> getBodies() {
        return ComponentMessage.componentMessage(content).replace(replacer).get().stream()
            .map(DialogBody::plainMessage)
            .toList();
    }

}