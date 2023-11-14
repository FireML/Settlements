package de.themoep.settlements.chat.gui;

import de.themoep.settlements.chat.ChatChannel;
import de.themoep.settlements.chat.ChatColorType;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.ConfigurableGui;
import de.themoep.settlements.utils.Utils;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import uk.firedev.poleislib.libs.themoep.inventorygui.GuiElementGroup;
import uk.firedev.poleislib.libs.themoep.inventorygui.StaticGuiElement;
import uk.firedev.poleislib.utils.ColorUtils;

public class ColorChannelSelectGui extends ConfigurableGui {

    public ColorChannelSelectGui(Resident resident) {
        super(resident, "chat.channel-select");

        GuiElementGroup group = createGroup('c');

        Player player = resident.getPlayer();

        for (ChatChannel channel : ChatChannel.values()) {
            if (!channel.canChat(player) && !channel.isPrivate()) {
                continue;
            }
            TextColor resColor = resident.getChannelColor(channel, ChatColorType.MESSAGE,
                    Utils.getChannelColor(channel, ChatColorType.MESSAGE));
            group.addElement(new StaticGuiElement('c', ColorUtils.getColorHead(NamedTextColor.nearestTo(resColor)), click -> {
                new ColorTypeSelectGui(resident, channel).open();
                return true;
            }, getText("channel-entry",
                    "color", "&" + NamedTextColor.nearestTo(resColor).asHexString(),
                    "color-name", NamedTextColor.nearestTo(resColor).toString(),
                    "channel", channel.getTag()
            )));
        }

        gui.addElement(group);
    }

}
