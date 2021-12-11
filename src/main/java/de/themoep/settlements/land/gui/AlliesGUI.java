package de.themoep.settlements.land.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.land.Ally;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.Utils;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

public class AlliesGUI extends SettlementsGUI {

    private final InventoryGui gui;
    private final Resident resident;
    private Ally.Sort sort = Ally.Sort.ALLY_DATE;

    public AlliesGUI(@NonNull String type, Resident resident, LandOwner<?, ?> landOwner) {
        super(type + ".allies");
        this.resident = resident;
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title"), getSetup());
        gui.setCloseAction(close -> false);
        gui.setFiller(getIcon("filler"));
        gui.addElements(getConfiguredElements());

        DynamicGuiElement sortedGroup = new DynamicGuiElement('a', who -> {
            GuiElementGroup group = new GuiElementGroup('a');
            landOwner.getAllies().stream()
                    .filter(a -> a.getLandOwner() != null)
                    .sorted((a1, a2) -> a1.compareTo(sort, a2))
                    .forEach(a -> group.addElement(
                            new StaticGuiElement('a', ItemUtils.getHead(resident.getUUID(), resident.getName()), click -> {
                                new AllyGUI(type, resident, a).open();
                                return true;
                            }, getText("entry." + a.getType(), a.getLandOwner().getReplacements(this, "allydate", Utils.DATE_FORMAT.format(a.getCreationDate()))))
                    ));
            return group;
        });

        gui.addElement(sortedGroup);

        gui.addElement(getCycleElement('s', () -> sort, click -> {
            sort = sort.next();
            click.getGui().draw();
            return true;
        }, "sort"));

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

        gui.addElement(new GuiBackElement('b', getIcon("back"), true));
    }

    public void open() {
        gui.show(resident.getPlayer());
    }
}
