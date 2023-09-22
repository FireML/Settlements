package de.themoep.settlements.land.gui;

import uk.firedev.poleislib.Utils.ItemUtils;
import uk.firedev.poleislib.libs.themoep.inventorygui.*;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.settlement.SettlementTitlesCommand;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.leveling.LevelInfo;
import de.themoep.settlements.rank.Rank;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
import uk.firedev.poleislib.libs.wesjd.anvilgui.AnvilGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;

public class TitlesGUI<L extends LandOwner<R, ? extends LevelInfo>, R extends Rank<L, R>> extends SettlementsGUI {
    private final Resident resident;
    private final InventoryGui gui;

    public TitlesGUI(@NonNull String type, Resident resident, L landOwner, R rank) {
        super(type + ".titles");
        this.resident = resident;
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title",
                "state", landOwner.getStateTag(),
                "rank", rank.getTag(landOwner)
        ), getSetup());
        gui.setCloseAction(close -> false);
        gui.setFiller(getIcon("filler"));
        gui.addElements(getConfiguredElements());

        if (landOwner.isMember(resident)) {
            GuiElementGroup group = createGroup('t');

            for (String title : landOwner.getTitles(rank)) {
                group.addElement(new DynamicGuiElement('t', h -> {
                    ItemStack item = getIcon("title");
                    List<String> description = new ArrayList<>();
                    description.add(getText("entry.title",
                            "title", title,
                            "rank", rank.getTag(landOwner)
                    ));
                    if (title.equals(landOwner.getDefaultTitle(rank))) {
                        description.add(getText("entry.default",
                                "title", title,
                                "rank", rank.getTag(landOwner)
                        ));
                        ItemUtils.setGlowing(item, true);
                    } else if (resident.hasPermission(type + ".titles.set") && !rank.above(landOwner.getRank(resident))) {
                        description.add(getText("entry.set",
                                "title", title,
                                "rank", rank.getTag(landOwner)
                        ));
                    }
                    if (resident.hasPermission(type + ".titles.remove") && !rank.above(landOwner.getRank(resident))) {
                        description.add(getText("entry.remove",
                                "title", title,
                                "rank", rank.getTag(landOwner)
                        ));
                    }
                    return new StaticGuiElement('t', item, click -> {
                        if (!rank.above(landOwner.getRank(resident))) {
                            switch (click.getType()) {
                                case LEFT, SHIFT_LEFT -> {
                                    if (resident.hasPermission(type + ".titles.set")) {
                                        String oldRankName = rank.getTag(landOwner);
                                        landOwner.setDefaultTitle(rank, title);
                                        Message.get("command." + type + ".titles.set.success").tell(resident, "title", title, "rank", oldRankName);
                                        gui.draw();
                                    }
                                    return true;
                                }
                                case RIGHT, SHIFT_RIGHT -> {
                                    if (resident.hasPermission(type + ".titles.remove")) {
                                        landOwner.removeTitle(rank, title);
                                        Message.get("command." + type + ".titles.remove.success").tell(resident,
                                                "title", title,
                                                "rank", rank.getTag(landOwner)
                                        );
                                        new TitlesGUI<>(type, resident, landOwner, rank).open();
                                    }
                                    return true;
                                }
                            }
                        }
                        return true;
                    }, description.toArray(new String[0]));
                }));
            }

            gui.addElement(group);

            if (resident.hasPermission(type + ".titles.add")
                    && !rank.above(landOwner.getRank(resident))
                    && landOwner.getTitles(rank).size() < landOwner.getTitlesPerRank()) {
                gui.addElement(new StaticGuiElement('a', getIcon("title"), click -> {
                    if (resident.hasPermission(type + ".titles.add")) {
                        new AnvilGUI.Builder()
                                .itemLeft(ItemUtils.toIcon(Material.WRITABLE_BOOK))
                                .title(getMessage("add.anvil.title").getReplacedText(
                                        "rank", rank.getTag(landOwner),
                                        "state", landOwner.getStateTag()
                                ))
                                .text(getMessage("add.anvil.placeholder").getReplacedText(
                                        "rank", rank.getTag(landOwner),
                                        "state", landOwner.getStateTag()
                                ))
                                .onClose(player -> Utils.runLater(() -> new TitlesGUI<>(type, resident, landOwner, rank).open(), 1))
                                .onClick((slot, stateSnapshot) -> {
                                    if (slot != AnvilGUI.Slot.OUTPUT) {
                                        return null;
                                    }
                                    String text = stateSnapshot.getText();
                                    text = ChatColor.stripColor(text);
                                    if (rank.above(landOwner.getRank(resident))) {
                                        return AnvilGUI.Response.text(getError("cant-edit-higher").getReplacedText("rank", rank.getTag(landOwner)));
                                    } else if (landOwner.getTitles(rank).size() >= landOwner.getTitlesPerRank()) {
                                        return AnvilGUI.Response.text(getError("too-many").getReplacedText(
                                                "rank", landOwner.getRank(resident).getTag(),
                                                "max", String.valueOf(landOwner.getTitlesPerRank())
                                        ));
                                    } else if (text.length() > 16) {
                                        return AnvilGUI.Response.text(getError("too-long").getReplacedText("input", text, "amount", "16"));
                                    } else if (!SettlementTitlesCommand.TITLE_PATTERN.matcher(text).matches()) {
                                        return AnvilGUI.Response.text(getError("invalid-characters").getReplacedText("input", text));
                                    } else if (Config.Titles.BLOCK_CURSES && Utils.containsAnyCurses(text)) {
                                        return AnvilGUI.Response.text(getError("prohibited-words").getReplacedText("input", text));
                                    } else if (landOwner.addTitle(rank, text)) {
                                        Message.get("command." + type + ".titles.add.success").tell(resident, "title", text, "rank", rank.getTag(landOwner));
                                        return AnvilGUI.Response.close();
                                    } else {
                                        return AnvilGUI.Response.text(getError("already-exists").getReplacedText("input", text, "rank", rank.getTag(landOwner)));
                                    }
                                })
                                .plugin(SettlementsPlugin.getInstance())
                                .open((Player) click.getWhoClicked());
                    }
                    return true;
                }, getText("add.title",
                        "rank", rank.getTag(landOwner),
                        "state", landOwner.getStateTag()
                )));
            }
        }

        gui.addElement(new GuiBackElement('b', getIcon("back"), true, getText("back")));
    }

    public void open() {
        gui.show(resident.getPlayer());
    }
}
