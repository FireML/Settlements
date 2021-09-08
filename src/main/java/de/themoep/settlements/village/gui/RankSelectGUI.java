package de.themoep.settlements.village.gui;

import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.rank.VillageRank;
import de.themoep.settlements.utils.ItemUtils;

import java.util.function.Consumer;

public class RankSelectGUI extends SettlementsGUI {
    private final static String[] SETUP = {
            "         ",
            "         ",
            "ggggggggg",
            "ggggggggg",
            "         ",
            "    b    "
    };
    private final Resident resident;
    private final InventoryGui gui;

    public RankSelectGUI(Resident resident, Consumer<VillageRank> rankConsumer) {
        super("rank-select");
        this.resident = resident;
        gui = new InventoryGui(SettlementsPlugin.getInstance(),
                getText("title", "state", resident.hasVillage() ? resident.getVillage().getStateTag() : ""),
                SETUP);
        gui.setCloseAction(close -> false);
        gui.setFiller(BORDER_ITEM);

        GuiElementGroup group = new GuiElementGroup('g');
        for (VillageRank rank : VillageRank.values()) {
            group.addElement(new StaticGuiElement('g',
                    ItemUtils.setGlowing(rank.getItem(), rank == resident.getVillageRank()),
                    click -> {
                        if (resident.hasVillage()) {
                            rankConsumer.accept(rank);
                        }
                        return true;
                    }, getText("entry",
                            "state", resident.hasVillage() ? resident.getVillage().getStateTag() : "",
                            "rank", resident.hasVillage() ? rank.getTag(resident.getVillage()) : rank.getTag()
                    )
            ));
        }
        gui.addElement(group);
    }

    public void open() {
        gui.show(resident.getPlayer());
    }
}
