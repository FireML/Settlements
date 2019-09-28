package me.crz.minecraft.kingdoms.chat.gui;

import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.chat.ChatChannel;
import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
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

import java.util.HashMap;
import java.util.Map;

public class ColorSelectGui extends KingdomsGUI {

    private static final Map<ChatChannel, ColorSelectGui> instances = new HashMap<>();

    private CInventory gui;

    private ColorSelectGui(ChatChannel channel) {
        super("chat.color-select");

        gui = new CInventory(getText("title", "channel", channel.getTag()), InventorySize.ONE) {};

        for (String color : Utils.getChatColors()) {
            ChatColor chatColor;
            try {
                chatColor = ChatColor.valueOf(color.toUpperCase());
            } catch (IllegalArgumentException e) {
                continue;
            }
            gui.addItem(new ClickableItem() {
                @Override
                public ItemStack getItem(Player p) {
                    Resident resident = ResidentManager.getInstance().getResident(p);
                    CItem item = new CItem(ColorUtils.getColorHead(chatColor));
                    if (resident.getChannelColor(channel, Utils.getChannelColor(channel)) == chatColor) {
                        item.glow(true);
                        item.setName(getMessage("selected-color-entry")
                                .getReplacedText(
                                        "color", chatColor.toString(),
                                        "name", HumanReadable.humanizedName(chatColor)));
                    } else {
                        item.setName(getMessage("color-entry")
                                .getReplacedText(
                                        "color", chatColor.toString(),
                                        "name", HumanReadable.humanizedName(chatColor)));
                    }
                    return item.build();
                }

                @Override
                public boolean click(Player p, ClickType c) {
                    Resident resident = ResidentManager.getInstance().getResident(p);
                    resident.setChannelColor(channel, chatColor);
                    Utils.runAsync(() -> DatabaseManager.getInstance().saveResident(resident));
                    Utils.runLater(() -> {
                        p.closeInventory();
                        getMessage("color-changed").tell(p,
                                "channel", channel.getTag(),
                                "color", chatColor.toString(),
                                "color-name", HumanReadable.humanizedName(chatColor)
                        );
                    }, 1);
                    return true;
                }
            });
        }
        gui.addItem((gui.getMaxSlot() / 9) * 9 + 8, new ClickableItem() {
            public ItemStack getItem(final Player p) {
                return new CItem(Material.BARRIER).setName(getText("back")).build();
            }

            public boolean click(final Player p, final ClickType clickType) {
                ColorChannelSelectGui.getInstance().open(p);
                return true;
            }
        });
    }

    public void open(Player p) {
        gui.build(p);
    }

    synchronized public static ColorSelectGui getInstance(ChatChannel channel) {
        return instances.computeIfAbsent(channel, ColorSelectGui::new);
    }
}
