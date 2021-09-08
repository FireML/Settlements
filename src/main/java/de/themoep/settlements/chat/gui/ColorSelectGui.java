package de.themoep.settlements.chat.gui;

import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.ColorUtils;
import de.themoep.settlements.utils.HumanReadable;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.chat.ChatChannel;
import de.themoep.settlements.chat.ChatColorType;
import de.themoep.settlements.db.DatabaseManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ColorSelectGui extends SettlementsGUI {
    private final static String[] SETUP = {
            "         ",
            "         ",
            "ggggggggg",
            "ggggggggg",
            "         ",
            "    b    "
    };

    public ColorSelectGui(ChatChannel channel, Player p, ChatColorType type) {
        super("chat.color-select");

        InventoryGui gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title", "channel", channel.getTag()), SETUP);
        gui.setCloseAction(close -> false);
        gui.setFiller(BORDER_ITEM);

        GuiElementGroup group = new GuiElementGroup('g');

        Resident resident = ResidentManager.getInstance().getResident(p);
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
                        Resident r = ResidentManager.getInstance().getResident((Player) click.getEvent().getWhoClicked());
                        r.setChannelColor(channel, type, chatColor);
                        DatabaseManager.getInstance().saveResident(r);
                        Utils.runLater(() -> {
                            gui.close();
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

        gui.addElement(new StaticGuiElement('b', ItemUtils.toIcon(Material.BARRIER), click -> {
            new ColorTypeSelectGui(channel).open((Player) click.getEvent().getWhoClicked());
            return true;
        }, getText("back")));
        gui.show(p);
    }
}
