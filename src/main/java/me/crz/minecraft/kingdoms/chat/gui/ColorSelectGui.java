package me.crz.minecraft.kingdoms.chat.gui;

import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.chat.ChatChannel;
import me.crz.minecraft.kingdoms.chat.ChatColorType;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.ColorUtils;
import me.crz.minecraft.kingdoms.utils.HumanReadable;
import me.crz.minecraft.kingdoms.utils.ItemUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ColorSelectGui extends KingdomsGUI {
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

        InventoryGui gui = new InventoryGui(KingdomsPlugin.getInstance(), getText("title", "channel", channel.getTag()), SETUP);
        gui.setCloseAction(close -> true);
        gui.setFiller(BORDER_ITEM);

        GuiElementGroup group = new GuiElementGroup('g');

        Resident resident = ResidentManager.getInstance().getResident(p);
        ChatColor residentColor = resident.getChannelColor(channel, type,
                Utils.getChannelColor(channel, type));

        for (String color : Utils.getChatColors()) {
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
                        Utils.runAsync(() -> DatabaseManager.getInstance().saveResident(r));
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
