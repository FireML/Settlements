package de.themoep.settlements.chat.gui;

import de.themoep.settlements.chat.ChatChannel;
import de.themoep.settlements.chat.ChatColorType;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.ColorUtils;
import de.themoep.settlements.utils.ConfigurableGui;
import de.themoep.settlements.utils.HumanReadable;
import de.themoep.settlements.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.event.inventory.ClickType;
import uk.firedev.poleislib.libs.themoep.inventorygui.DynamicGuiElement;
import uk.firedev.poleislib.libs.themoep.inventorygui.GuiBackElement;
import uk.firedev.poleislib.libs.themoep.inventorygui.GuiElementGroup;
import uk.firedev.poleislib.libs.themoep.inventorygui.StaticGuiElement;

public class ColorTypeSelectGui extends ConfigurableGui {

    public ColorTypeSelectGui(Resident resident, ChatChannel channel) {
        super(resident, "chat.color-type-select");

        GuiElementGroup group = createGroup('c');

        for (ChatColorType type : ChatColorType.values()) {
            group.addElement(new DynamicGuiElement('c', h -> {
                ChatColor chatColor = resident.getChannelColor(channel, type,
                        Utils.getChannelColor(channel, type));
                return new StaticGuiElement('c', ColorUtils.getColorHead(chatColor), click -> {
                        if (click.getType() == ClickType.MIDDLE) {
                            resident.removeChannelColor(channel, type);
                            ChatColor defaultColor = Utils.getChannelColor(channel, type);
                            DatabaseManager.getInstance().saveResident(resident);
                            click.getGui().draw();
                            getMessage("color-reset").tell(resident,
                                    "channel", channel.getTag(),
                                    "type", type.getTag(),
                                    "color", defaultColor.toString(),
                                    "color-name", HumanReadable.humanizedName(defaultColor)
                            );
                            return true;
                        }
                        new ColorSelectGui(channel, resident, type).open();
                        return true;
                    }, getText("entry",
                            "color", chatColor.toString(),
                            "type", type.getTag(),
                            "name", HumanReadable.humanizedName(chatColor)
                    )
                );
            }));
        }

        gui.addElement(group);

        gui.addElement(new GuiBackElement('b', getIcon("back"), true, getText("back")));
    }

}
