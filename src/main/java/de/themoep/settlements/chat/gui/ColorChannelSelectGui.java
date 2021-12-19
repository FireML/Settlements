package de.themoep.settlements.chat.gui;

import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.chat.ChatChannel;
import de.themoep.settlements.chat.ChatColorType;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.ColorUtils;
import de.themoep.settlements.utils.ConfigurableGui;
import de.themoep.settlements.utils.HumanReadable;
import de.themoep.settlements.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ColorChannelSelectGui extends ConfigurableGui {

    public ColorChannelSelectGui(Resident resident) {
        super(resident, "chat.channel-select");

        GuiElementGroup group = createGroup('c');

        Player player = resident.getPlayer();

        for (ChatChannel channel : ChatChannel.values()) {
            if (!channel.canChat(player) && !channel.isPrivate()) {
                continue;
            }
            ChatColor resColor = resident.getChannelColor(channel, ChatColorType.MESSAGE,
                    Utils.getChannelColor(channel, ChatColorType.MESSAGE));
            group.addElement(new StaticGuiElement('c', ColorUtils.getColorHead(resColor), click -> {
                new ColorTypeSelectGui(resident, channel).open();
                return true;
            }, getText("channel-entry",
                    "color", resColor.toString(),
                    "color-name", HumanReadable.humanizedName(resColor),
                    "channel", channel.getTag()
            )));
        }

        gui.addElement(group);
    }

}
