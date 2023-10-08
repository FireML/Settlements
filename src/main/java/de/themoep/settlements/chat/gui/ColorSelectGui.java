package de.themoep.settlements.chat.gui;

import de.themoep.settlements.chat.ChatChannel;
import de.themoep.settlements.chat.ChatColorType;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.ColorUtils;
import de.themoep.settlements.utils.ConfigurableGui;
import de.themoep.settlements.utils.HumanReadable;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import uk.firedev.poleislib.Utils.ItemUtils;
import uk.firedev.poleislib.libs.themoep.inventorygui.GuiBackElement;
import uk.firedev.poleislib.libs.themoep.inventorygui.GuiElementGroup;
import uk.firedev.poleislib.libs.themoep.inventorygui.StaticGuiElement;

public class ColorSelectGui extends ConfigurableGui {

    public ColorSelectGui(ChatChannel channel, Resident resident, ChatColorType type) {
        super(resident, "chat.color-select");

        GuiElementGroup group = createGroup('c');

        ChatColor residentColor = resident.getChannelColor(channel, type,
                Utils.getChannelColor(channel, type));

        for (String color : Config.Chat.SELECTABLE_COLORS) {
            ChatColor chatColor;
            try {
                chatColor = ChatColor.valueOf(color.toUpperCase());
            } catch (IllegalArgumentException e) {
                continue;
            }

            group.addElement(new StaticGuiElement(
                    'g',
                    ItemUtils.setGlowing(ColorUtils.getColorHead(chatColor), residentColor == chatColor),
                    click -> {
                        Resident r = ResidentManager.getInstance().getResident((Player) click.getWhoClicked());
                        r.setChannelColor(channel, type, chatColor);
                        DatabaseManager.getInstance().saveResident(r);
                        Utils.runLater(() -> {
                            click.getGui().close();
                            getMessage("color-changed").tell(r,
                                    "channel", channel.getTag(),
                                    "type", type.getTag(),
                                    "color", chatColor.toString(),
                                    "color-name", HumanReadable.humanizedName(chatColor)
                            );
                        }, 1);
                        return true;
                    },
                    getText((residentColor == chatColor ? "selected-" : "") + "color-entry",
                            "color", chatColor.toString(),
                            "name", HumanReadable.humanizedName(chatColor))
            ));
        }

        gui.addElement(group);

        gui.addElement(new GuiBackElement('b', getIcon("back"), true, getText("back")));
    }
}
