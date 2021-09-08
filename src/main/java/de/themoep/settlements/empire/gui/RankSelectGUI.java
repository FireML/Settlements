package de.themoep.settlements.empire.gui;

import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.rank.EmpireRank;
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

    public RankSelectGUI(Resident resident, Consumer<EmpireRank> rankConsumer) {
        super("empire.rank-select");
        this.resident = resident;
        gui = new InventoryGui(SettlementsPlugin.getInstance(),
                getText("title", "state", resident.hasEmpire() ? resident.getVillage().getEmpire().getStateTag() : ""),
                SETUP);
        gui.setCloseAction(close -> false);
        gui.setFiller(BORDER_ITEM);

        GuiElementGroup group = new GuiElementGroup('g');
        for (EmpireRank rank : EmpireRank.values()) {
            group.addElement(new StaticGuiElement('g',
                    ItemUtils.setGlowing(rank.getItem(), rank == resident.getEmpireRank()),
                    click -> {
                        if (resident.hasEmpire()) {
                            rankConsumer.accept(rank);
                        }
                        return true;
                    },  getText("entry",
                            "state", resident.hasEmpire() ? resident.getVillage().getEmpire().getStateTag() : "",
                            "rank", resident.hasEmpire() ? rank.getTag(resident.getVillage().getEmpire()) : rank.getTag()
                    )
            ));
        }
        gui.addElement(group);
    }

    public void open() {
        gui.show(resident.getPlayer());
    }
}
