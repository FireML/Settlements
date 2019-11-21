package me.crz.minecraft.kingdoms.chat.gui;

import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.chat.ChatChannel;
import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.utils.ColorUtils;
import me.crz.minecraft.kingdoms.utils.HumanReadable;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class ColorChannelSelectGui extends KingdomsGUI {

    private CInventory gui;

    public ColorChannelSelectGui(Player p) {
        super("chat.channel-select");
        gui = new CInventory(getText("title"), InventorySize.ONE) {};

        for (ChatChannel channel : ChatChannel.values()) {
            if (!channel.canChat(p) && !channel.isPrivate()) {
                continue;
            }
            gui.addItem(new ClickableItem() {
                @Override
                public ItemStack getItem(Player p) {
                    Resident resident = ResidentManager.getInstance().getResident(p);
                    ChatColor resColor = resident.getChannelColor(channel, Utils.getChannelColor(channel));
                    return new CItem(ColorUtils.getColorHead(resColor))
                            .setName(getMessage("channel-entry")
                                    .getReplacedText(
                                            "color", resColor.toString(),
                                            "color-name", HumanReadable.humanizedName(resColor),
                                            "channel", channel.getTag()))
                            .build();
                }

                @Override
                public boolean click(Player p, ClickType c) {
                    ColorSelectGui.getInstance(channel).open(p);
                    return true;
                }
            });
        }
        gui.build(p);
    }

}
