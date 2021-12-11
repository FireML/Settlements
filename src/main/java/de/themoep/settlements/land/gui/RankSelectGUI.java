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
        this(type, resident, landOwner, rankConsumer, rankConsumer);
    }

    public RankSelectGUI(@NonNull String type, Resident resident, L landOwner, Consumer<R> rankConsumer, Consumer<R> chiefRankConsumer) {
        super(type + ".rank-select");
        this.resident = resident;
        gui = new InventoryGui(SettlementsPlugin.getInstance(),
                getText("title", "state", landOwner.getStateTag()),
                getSetup());
        gui.setCloseAction(close -> false);
        gui.setFiller(BORDER_ITEM);

        GuiElementGroup group = createGroup('r');
        for (R rank : landOwner.getRanks()) {
            if (rank == landOwner.getHighestRank()) {
                continue;
            }
            group.addElement(new StaticGuiElement('r',
                    ItemUtils.setGlowing(rank.getItem(), rank == landOwner.getRank(resident)),
                    click -> {
                        if (landOwner.isMember(resident) && landOwner.getRank(resident).atLeast(rank)) {
                            rankConsumer.accept(rank);
                        } else {
                            InventoryGui.goBack(click.getWhoClicked());
                        }
                        return true;
                    }, getText("entry" + (landOwner.getRank(resident).below(rank) ? "-not-selectable" : ""),
                            "state",  landOwner.getStateTag(),
                            "rank", rank.getTag(landOwner)
                    )
            ));
        }

        gui.addElement(new StaticGuiElement('c', ItemUtils.setGlowing(landOwner.getHighestRank().getItem(),
                landOwner.getHighestRank() == landOwner.getRank(resident)),
                click -> {
                    if (landOwner.isMember(resident) && landOwner.getRank(resident).atLeast(landOwner.getHighestRank())) {
                        chiefRankConsumer.accept(landOwner.getHighestRank());
                    } else {
                        InventoryGui.goBack(click.getWhoClicked());
                    }
                    return true;
                }, getText("entry-chief" + (landOwner.getRank(resident).below(landOwner.getHighestRank()) ? "-not-selectable" : ""),
                        "state",  landOwner.getStateTag(),
                        "rank", landOwner.getHighestRank().getTag(landOwner)
                )
        ));

        gui.addElement(group);
    }

    public void open() {
        gui.show(resident.getPlayer());
    }
}
