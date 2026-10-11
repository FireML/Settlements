package de.themoep.settlements.land.gui;

import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.leveling.LevelInfo;
import de.themoep.settlements.rank.Rank;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.ConfigurableGui;
import org.checkerframework.checker.nullness.qual.NonNull;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.utils.ItemUtils;

import java.util.Comparator;
import java.util.function.Consumer;

public class RankSelectGUI<L extends LandOwner<R, ? extends LevelInfo>, R extends Rank<L, R>> extends ConfigurableGui {

    public RankSelectGUI(@NonNull String type, Resident resident, Resident target, L landOwner, Consumer<R> rankConsumer) {
        this(type, resident, target, landOwner, rankConsumer, rankConsumer);
    }

    public RankSelectGUI(@NonNull String type, Resident resident, Resident target, L landOwner, Consumer<R> rankConsumer, Consumer<R> chiefRankConsumer) {
        super(resident, type + ".rank-select", "state", landOwner.getStateTag());

        GuiElementGroup group = createGroup('r');
        landOwner.getRanks().stream().sorted(Comparator.reverseOrder()).forEachOrdered(rank -> {
            if (rank == landOwner.getHighestRank()) {
                return;
            } else if (landOwner.useGuestRank() && rank == landOwner.getDefaultRank()) {
                return;
            }
            group.addElement(new StaticGuiElement('r',
                    ItemUtils.setGlowing(rank.getItem(), rank == landOwner.getRank(target)),
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
        });

        gui.addElement(new StaticGuiElement('c', ItemUtils.setGlowing(landOwner.getHighestRank().getItem(),
                landOwner.getHighestRank() == landOwner.getRank(target)),
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
}
