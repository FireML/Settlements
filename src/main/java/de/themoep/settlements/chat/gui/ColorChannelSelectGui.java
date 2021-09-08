package de.themoep.settlements.chat.gui;

import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.ColorUtils;
import de.themoep.settlements.utils.HumanReadable;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.chat.ChatChannel;
import de.themoep.settlements.chat.ChatColorType;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ColorChannelSelectGui extends SettlementsGUI {
    private final static String[] SETUP = {
            "         ",
            "         ",
            "ggggggggg",
            "ggggggggg",
            "         ",
            "         "
    };

    public ColorChannelSelectGui(Player p) {
        super("chat.channel-select");
        InventoryGui gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title"), SETUP);
        gui.setCloseAction(close -> false);
        gui.setFiller(BORDER_ITEM);

        GuiElementGroup group = new GuiElementGroup('g');

        for (ChatChannel channel : ChatChannel.values()) {
            if (!channel.canChat(p) && !channel.isPrivate()) {
                continue;
            }
            Resident resident = ResidentManager.getInstance().getResident(p);
            ChatColor resColor = resident.getChannelColor(channel, ChatColorType.MESSAGE,
                    Utils.getChannelColor(channel, ChatColorType.MESSAGE));
            group.addElement(new StaticGuiElement('g', ColorUtils.getColorHead(resColor), click -> {
                new ColorTypeSelectGui(channel).open(p);
                return true;
            }, getText("channel-entry",
                    "color", resColor.toString(),
                    "color-name", HumanReadable.humanizedName(resColor),
                    "channel", channel.getTag()
            )));
        }

        gui.addElement(group);

        gui.show(p);
    }

}
