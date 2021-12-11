package de.themoep.settlements.land.gui;

import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.leveling.LevelInfo;
import de.themoep.settlements.rank.Rank;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.ItemUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.Consumer;

public class RankSelectGUI<L extends LandOwner<R, ? extends LevelInfo>, R extends Rank<L, R>> extends SettlementsGUI {
    private final Resident resident;
    private final InventoryGui gui;

    public RankSelectGUI(@NonNull String type, Resident resident, L landOwner, Consumer<R> rankConsumer) {
        super(type + ".rank-select");
        this.resident = resident;
        gui = new InventoryGui(SettlementsPlugin.getInstance(),
                getText("title", "state", landOwner.getStateTag()),
                getSetup());
        gui.setCloseAction(close -> false);
        gui.setFiller(BORDER_ITEM);

        GuiElementGroup group = createGroup('r');
        for (R rank : landOwner.getRanks()) {
            group.addElement(new StaticGuiElement('r',
                    ItemUtils.setGlowing(rank.getItem(), rank == landOwner.getRank(resident)),
                    click -> {
                        if (landOwner.isMember(resident)) {
                            rankConsumer.accept(rank);
                        }
                        return true;
                    }, getText("entry",
                            "state",  landOwner.getStateTag(),
                            "rank", rank.getTag(landOwner)
                    )
            ));
        }
        gui.addElement(group);
    }

    public void open() {
        gui.show(resident.getPlayer());
    }
}
