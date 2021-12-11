package de.themoep.settlements.settlement.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.NameTag;
import de.themoep.settlements.utils.Next;
import org.bukkit.inventory.ItemStack;

public class ResListGUI extends SettlementsGUI {

    private final InventoryGui gui;
    private final Resident resident;
    private Sort sort = Sort.RANK;

    public ResListGUI(Resident resident, Settlement settlement) {
        super("settlement.reslist");
        this.resident = resident;
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title"), getSetup());
        gui.setCloseAction(close -> false);
        gui.setFiller(getIcon("filler"));
        gui.addElements(getConfiguredElements());

        DynamicGuiElement sortedGroup = new DynamicGuiElement('r', who -> {
            GuiElementGroup group = new GuiElementGroup('r');
            settlement.getResidents().stream().sorted((r1, r2) -> {
                switch (sort) {
                    case RANK:
                        return r1.getSettlementRank().compareTo(r2.getSettlementRank());
                    case LAST_LOGIN:
                        return Long.compare(r2.getLastLogin(), r1.getLastLogin());
                }
                return 0;
            }).forEach(r -> group.addElement(
                    new StaticGuiElement('r', ItemUtils.getHead(resident.getUUID(), resident.getName()), click -> {
                        new ResidentGUI(resident, r).open();
                        return true;
                    }, getText("entry", r.getReplacements()))
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

    private enum Sort implements NameTag, Next<Sort> {
        RANK,
        LAST_LOGIN;

        @Override
        public Sort[] getValues() {
            return values();
        }
    }
}
