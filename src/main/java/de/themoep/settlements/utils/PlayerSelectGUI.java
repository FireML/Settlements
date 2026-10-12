package de.themoep.settlements.utils;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.resident.Resident;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;
import java.util.function.Function;

public class PlayerSelectGUI extends ConfigurableGui {

    public PlayerSelectGUI(Resident resident, String name, Function<Player, Boolean> playerFilter, Consumer<Player> selectedAction) {
        super(resident, name);

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
                    }, getText("player", "name", player.getName(), "uuid", player.getUniqueId().toString())));
                }
            }
            Utils.runSync(gui::draw);
        });

        gui.addElement(group);

        GuiElementGroup pages = new GuiElementGroup('p');
        gui.addElement(pages);

        for (int i = 0; i < gui.getPageAmount(resident.getPlayer()) && i < pages.getSlots().length; i++) {
            int page = i;
            pages.addElement(new DynamicGuiElement('p', who -> {
                ItemStack icon = getIcon("page");
                if (page == gui.getPageNumber(who)) {
                    ItemUtils.setGlowing(icon, true);
                }
                return new StaticGuiElement('p', icon, page + 1, click -> {
                    gui.setPageNumber(click.getWhoClicked(), page);
                    return true;
                }, getText("page", "page", String.valueOf(page + 1), "pages", String.valueOf(gui.getPageAmount(who))));
            }));
        }

        gui.addElement(new GuiBackElement('b', getIcon("back"), true, getText("back")));
    }
}
