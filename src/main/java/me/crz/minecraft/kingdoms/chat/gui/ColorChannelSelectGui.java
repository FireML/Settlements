package me.crz.minecraft.kingdoms.chat.gui;

import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.chat.ChatChannel;
import me.crz.minecraft.kingdoms.chat.ChatColorType;
import me.crz.minecraft.kingdoms.utils.ColorUtils;
import me.crz.minecraft.kingdoms.utils.HumanReadable;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ColorChannelSelectGui extends KingdomsGUI {
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
        InventoryGui gui = new InventoryGui(KingdomsPlugin.getInstance(), getText("title"), SETUP);
        gui.setCloseAction(close -> true);
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
