package me.crz.minecraft.kingdoms.utils;

import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.GuiPageElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;
import java.util.function.Function;

public class PlayerSelectGUI {
    private final static String[] SETUP = {
            "         ",
            "ggggggggg",
            "ggggggggg",
            "ggggggggg",
            "ggggggggg",
            "p   b   n"
    };
    private final InventoryGui gui;

    public PlayerSelectGUI(ConfigMessages configMessages, String name, ItemStack filler, Function<Player, Boolean> playerFilter, Consumer<Player> selectedAction, String... replacements) {
        gui = new InventoryGui(KingdomsPlugin.getInstance(), configMessages.getText(name + ".title", replacements), SETUP);
        gui.setFiller(filler);

        GuiElementGroup group = new GuiElementGroup('g');

        Utils.runAsync(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (playerFilter.apply(player)) {
                    group.addElement(new StaticGuiElement('g', ItemUtils.getHead(player.getUniqueId(), player.getName()), click -> {
                        if (player.isOnline()) {
                            selectedAction.accept(player);
                            InventoryGui.goBack(click.getWhoClicked());
                        }
                        return true;
                    }, configMessages.getText(name + ".player", "name", player.getName(), "uuid", player.getUniqueId().toString())));
                }
            }
            Utils.runSync(gui::draw);
        });

        gui.addElement(group);

        gui.addElement(new GuiPageElement('p', ItemUtils.toIcon(Material.ARROW), GuiPageElement.PageAction.PREVIOUS, configMessages.getText(name + ".page.previous", "page", "%prevpage%")));

        gui.addElement(new GuiBackElement('b', ItemUtils.toIcon(Material.BARRIER), configMessages.getText(name + ".back")));

        gui.addElement(new GuiPageElement('n', ItemUtils.toIcon(Material.ARROW), GuiPageElement.PageAction.NEXT, configMessages.getText(name + ".page.next", "page", "%nextpage%")));
    }

    public void open(Player player) {
        gui.show(player);
    }
}
