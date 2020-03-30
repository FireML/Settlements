package me.crz.minecraft.kingdoms.chat.gui;

import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.chat.ChatChannel;
import me.crz.minecraft.kingdoms.chat.ChatColorType;
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

public class ColorTypeSelectGui extends KingdomsGUI {

    private static final Map<ChatChannel, ColorTypeSelectGui> instances = new HashMap<>();

    private CInventory gui;

    private ColorTypeSelectGui(ChatChannel channel) {
        super("chat.color-type-select");

        gui = new CInventory(getText("title", "channel", channel.getTag()), InventorySize.ONE) {};

        for (ChatColorType type : ChatColorType.values()) {
            gui.addItem(new ClickableItem() {
                @Override
                public ItemStack getItem(Player p) {
                    Resident resident = ResidentManager.getInstance().getResident(p);
                    ChatColor chatColor = resident.getChannelColor(channel, type, Utils.getChannelColor(channel, type));
                    return new CItem(ColorUtils.getColorHead(chatColor))
                            .setName(getMessage("entry")
                                    .getReplacedText(
                                        "color", chatColor.toString(),
                                        "type", type.getTag(),
                                        "name", HumanReadable.humanizedName(chatColor)))
                            .build();
                }

                @Override
                public boolean click(Player p, ClickType c) {
                    if (c == ClickType.MIDDLE) {
                        Resident resident = ResidentManager.getInstance().getResident(p);
                        resident.removeChannelColor(channel, type);
                        ChatColor defaultColor = Utils.getChannelColor(channel, type);
                        Utils.runAsync(() -> DatabaseManager.getInstance().saveResident(resident));
                        Utils.runLater(() -> {
                            gui.build(p);
                            getMessage("color-reset").tell(p,
                                    "channel", channel.getTag(),
                                    "type", type.getTag(),
                                    "color", defaultColor.toString(),
                                    "color-name", HumanReadable.humanizedName(defaultColor)
                            );
                        }, 1);
                        return true;
                    }
                    new ColorSelectGui(channel, p, type);
                    return true;
                }
            });
        }
        gui.addItem((gui.getMaxSlot() / 9) * 9 + 8, new ClickableItem() {
            public ItemStack getItem(final Player p) {
                return new CItem(Material.BARRIER).setName(getText("back")).build();
            }

            public boolean click(final Player p, final ClickType clickType) {
                new ColorChannelSelectGui(p);
                return true;
            }
        });
    }

    public void open(Player p) {
        gui.build(p);
    }

    synchronized public static ColorTypeSelectGui getInstance(ChatChannel channel) {
        return instances.computeIfAbsent(channel, ColorTypeSelectGui::new);
    }

}
