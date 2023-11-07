package de.themoep.settlements.land.gui;

import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.leveling.LevelInfo;
import de.themoep.settlements.rank.Rank;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.ConfigurableGui;
import de.themoep.settlements.utils.Message;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import uk.firedev.poleislib.utils.ItemUtils;
import uk.firedev.poleislib.libs.themoep.inventorygui.DynamicGuiElement;
import uk.firedev.poleislib.libs.themoep.inventorygui.GuiElementGroup;
import uk.firedev.poleislib.libs.themoep.inventorygui.StaticGuiElement;

import java.util.ArrayList;
import java.util.List;

public class TitleGUI<L extends LandOwner<R, ? extends LevelInfo>, R extends Rank<L, R>> extends ConfigurableGui {

    public TitleGUI(@NonNull String type, Resident resident, Resident target, L landOwner) {
        super(resident, type + ".title",
                "state", landOwner.getStateTag(),
                "resident", target.getName(),
                "rank", landOwner.getRank(target).getTag(landOwner));
        R rank = landOwner.getRank(target);

        if (resident.hasSettlement()) {
            GuiElementGroup group = createGroup('t');

            for (String title : landOwner.getTitles(rank)) {
                group.addElement(new DynamicGuiElement('t', h -> {
                    ItemStack item = getIcon("title");
                    List<String> description = new ArrayList<>();
                    description.add(getText("entry.title",
                            "title", title,
                            "rank", rank.getTag(landOwner)
                    ));
                    if (title.equals(landOwner.getTitle(target))) {
                        ItemUtils.setGlowing(item, true);
                        description.add(getText("entry.current",
                                "title", title,
                                "rank", rank.getTag(landOwner)
                        ));
                    }
                    if (title.equals(landOwner.getDefaultTitle(rank))) {
                        description.add(getText("entry.default",
                                "title", title,
                                "rank", rank.getTag(landOwner)
                        ));
                    }
                    return new StaticGuiElement('t', item, click -> {
                        if (!rank.above(landOwner.getRank(resident))
                                && resident.hasPermission(type + ".title") && (resident.equals(target) || resident.hasPermission(type + ".settitle"))
                                && landOwner.setTitle(target, title)) {
                            DatabaseManager.getInstance().saveResident(target);
                            gui.draw();
                            Message message = Message.get("command." + type + ".title.success." + landOwner.getStateKey() + "." + landOwner.getRank(resident).getKey());
                            if (!message.exists()) {
                                message = Message.get("command." + type + ".title.success.default");
                            }
                            message.tell(landOwner.getOnlineResidents(),
                                    "rank", landOwner.getRank(target).getTag(landOwner),
                                    "title", landOwner.getTitle(target),
                                    "name", target.getName(),
                                    "settlementname", landOwner.getName(),
                                    "settlementstate", landOwner.getStateTag()
                            );
                        }
                        return true;
                    }, description.toArray(new String[0]));
                }));
            }

            gui.addElement(group);

            if ((landOwner.hasPermission(resident, "titles.add") || landOwner.hasPermission(resident, "titles.remove"))
                    && !rank.above(landOwner.getRank(resident))
                    && landOwner.getTitles(rank).size() < landOwner.getTitlesPerRank()) {
                gui.addElement(new StaticGuiElement('a', getIcon("title"), click -> {
                    if (landOwner.hasPermission(resident, "titles.add") || landOwner.hasPermission(resident, "titles.remove")) {
                        new TitlesGUI<>(type, resident, landOwner, rank).open();
                    }
                    return true;
                }, getText("edit",
                        "rank", rank.getTag(landOwner),
                        "state", landOwner.getStateTag()
                )));
            }
        }
    }

    public void open() {
        gui.show(resident.getPlayer());
    }
}
