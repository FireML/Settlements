package de.themoep.settlements.chat.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.chat.ChatChannel;
import de.themoep.settlements.chat.ChatColorType;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.ColorUtils;
import de.themoep.settlements.utils.ConfigurableGui;
import de.themoep.settlements.utils.Utils;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.event.inventory.ClickType;

public class ColorTypeSelectGui extends ConfigurableGui {

    public ColorTypeSelectGui(Resident resident, ChatChannel channel) {
        super(resident, "chat.color-type-select");

        GuiElementGroup group = createGroup('c');

        for (ChatColorType type : ChatColorType.values()) {
            group.addElement(new DynamicGuiElement('c', h -> {
                TextColor textColor = resident.getChannelColor(channel, type,
                        Utils.getChannelColor(channel, type));
                return new StaticGuiElement('c', ColorUtils.getColorHead(NamedTextColor.nearestTo(textColor)), click -> {
                        if (click.getType() == ClickType.MIDDLE) {
                            resident.removeChannelColor(channel, type);
                            TextColor defaultColor = Utils.getChannelColor(channel, type);
                            DatabaseManager.getInstance().saveResident(resident);
                            click.getGui().draw();
                            getMessage("color-reset").tell(resident,
                                    "channel", channel.getTag(),
                                    "type", type.getTag(),
                                    "color", "&" + NamedTextColor.nearestTo(defaultColor).asHexString(),
                                    "color-name", NamedTextColor.nearestTo(defaultColor).toString()
                            );
                            return true;
                        }
                        new ColorSelectGui(channel, resident, type).open();
                        return true;
                    }, getText("entry",
                            "color", "&#" + NamedTextColor.nearestTo(textColor).asHexString(),
                            "type", type.getTag(),
                            "name", NamedTextColor.nearestTo(textColor).toString()
                    )
                );
            }));
        }

        gui.addElement(group);

        gui.addElement(new GuiBackElement('b', getIcon("back"), true, getText("back")));
    }

}
