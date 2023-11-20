package de.themoep.settlements.chat.gui;

import de.themoep.settlements.chat.ChatChannel;
import de.themoep.settlements.chat.ChatColorType;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.ConfigurableGui;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import uk.firedev.poleislib.libs.themoep.inventorygui.GuiBackElement;
import uk.firedev.poleislib.libs.themoep.inventorygui.GuiElementGroup;
import uk.firedev.poleislib.libs.themoep.inventorygui.StaticGuiElement;
import uk.firedev.poleislib.utils.ColorUtils;
import uk.firedev.poleislib.utils.ItemUtils;

public class ColorSelectGui extends ConfigurableGui {

    public ColorSelectGui(ChatChannel channel, Resident resident, ChatColorType type) {
        super(resident, "chat.color-select");

        GuiElementGroup group = createGroup('c');

        TextColor residentColor = resident.getChannelColor(channel, type,
                Utils.getChannelColor(channel, type));

        for (String color : Config.Chat.SELECTABLE_COLORS) {
            TextColor chatColor;
            try {
                chatColor = ColorUtils.getColor(color.toLowerCase(), NamedTextColor.WHITE);
            } catch (IllegalArgumentException e) {
                continue;
            }

            group.addElement(new StaticGuiElement(
                    'g',
                    ItemUtils.setGlowing(ColorUtils.getColorHead(NamedTextColor.nearestTo(chatColor)), residentColor == chatColor),
                    click -> {
                        Resident r = ResidentManager.getInstance().getResident((Player) click.getWhoClicked());
                        r.setChannelColor(channel, type, chatColor);
                        DatabaseManager.getInstance().saveResident(r);
                        Utils.runLater(() -> {
                            click.getGui().close();
                            getMessage("color-changed").tell(r,
                                    "channel", channel.getTag(),
                                    "type", type.getTag(),
                                    "color", "&" + NamedTextColor.nearestTo(chatColor).asHexString(),
                                    "color-name", NamedTextColor.nearestTo(chatColor).toString()
                            );
                        }, 1);
                        return true;
                    },
                    getText((residentColor == chatColor ? "selected-" : "") + "color-entry",
                            "color", ColorUtils.hexToLegacy("&" + NamedTextColor.nearestTo(chatColor).asHexString()),
                            "name", NamedTextColor.nearestTo(chatColor).toString())
            ));
        }

        gui.addElement(group);

        gui.addElement(new GuiBackElement('b', getIcon("back"), true, getText("back")));
    }
}
