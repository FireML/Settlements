package de.themoep.settlements.chat.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.ColorUtils;
import de.themoep.settlements.utils.HumanReadable;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.chat.ChatChannel;
import de.themoep.settlements.chat.ChatColorType;
import de.themoep.settlements.db.DatabaseManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public class ColorTypeSelectGui extends SettlementsGUI {
    private final static String[] SETUP = {
            "         ",
            "         ",
            "ggggggggg",
            "ggggggggg",
            "         ",
            "    b    "
    };

    private InventoryGui gui;

    public ColorTypeSelectGui(ChatChannel channel) {
        super("chat.color-type-select");

        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title", "channel", channel.getTag()), SETUP);
        gui.setCloseAction(close -> false);
        gui.setFiller(BORDER_ITEM);

        GuiElementGroup group = new GuiElementGroup('g');

        for (ChatColorType type : ChatColorType.values()) {
            group.addElement(new DynamicGuiElement('g', h -> {
                Resident resident = ResidentManager.getInstance().getResident((Player) h);
                ChatColor chatColor = resident.getChannelColor(channel, type,
                        Utils.getChannelColor(channel, type));
                return new StaticGuiElement('g', ColorUtils.getColorHead(chatColor), click -> {
                        if (click.getType() == ClickType.MIDDLE) {
                            resident.removeChannelColor(channel, type);
                            ChatColor defaultColor = Utils.getChannelColor(channel, type);
                            DatabaseManager.getInstance().saveResident(resident);
                            Utils.runLater(() -> {
                                gui.draw();
                                getMessage("color-reset").tell(resident,
                                        "channel", channel.getTag(),
                                        "type", type.getTag(),
                                        "color", defaultColor.toString(),
                                        "color-name", HumanReadable.humanizedName(defaultColor)
                                );
                            }, 1);
                            return true;
                        }
                        new ColorSelectGui(channel, resident.getPlayer(), type);
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

        gui.addElement(new StaticGuiElement('b', ItemUtils.toIcon(Material.BARRIER), click -> {
            new ColorChannelSelectGui((Player) click.getEvent().getWhoClicked());
            return true;
        }, getText("back")));
    }

    public void open(Player p) {
        gui.show(p);
    }

}
