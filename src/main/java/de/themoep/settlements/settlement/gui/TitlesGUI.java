package de.themoep.settlements.settlement.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.settlement.SettlementTitlesCommand;
import de.themoep.settlements.rank.SettlementRank;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.resident.Resident;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TitlesGUI extends SettlementsGUI {
    private static final String[] SETUP = {
            "         ",
            "         ",
            "ggggggggg",
            "ggggggggg",
            "         ",
            "    b   a"
    };
    private final Resident resident;
    private final InventoryGui gui;

    public TitlesGUI(Resident resident, SettlementRank rank) {
        super("titles");
        this.resident = resident;
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title",
                "state", resident.hasSettlement() ? resident.getSettlement().getStateTag() : "",
                "rank", rank.getTag(resident.getSettlement())
        ), SETUP);
        gui.setCloseAction(close -> false);
        gui.setFiller(BORDER_ITEM);

        if (resident.hasSettlement()) {
            GuiElementGroup group = new GuiElementGroup('g');

            for (String title : resident.getSettlement().getTitles(rank)) {
                group.addElement(new DynamicGuiElement('g', h -> {
                    ItemStack item = ItemUtils.toIcon(Material.PAPER);
                    List<String> description = new ArrayList<>();
                    description.add(getText("entry.title",
                            "title", title,
                            "rank", rank.getTag(resident.getSettlement())
                    ));
                    if (title.equals(resident.getSettlement().getDefaultTitle(rank))) {
                        description.add(getText("entry.default",
                                "title", title,
                                "rank", rank.getTag(resident.getSettlement())
                        ));
                        ItemUtils.setGlowing(item, true);
                    } else if (resident.hasPermission("settlement.titles.set")) {
                        description.add(getText("entry.set",
                                "title", title,
                                "rank", rank.getTag(resident.getSettlement())
                        ));
                    }
                    if (resident.hasPermission("settlement.titles.remove") && !rank.above(resident.getSettlementRank())) {
                        description.add(getText("entry.remove",
                                "title", title,
                                "rank", rank.getTag(resident.getSettlement())
                        ));
                    }
                    return new StaticGuiElement('g', item, click -> {
                        if (resident.hasSettlement()) {
                            switch (click.getType()) {
                                case LEFT:
                                case SHIFT_LEFT:
                                    if (resident.hasPermission("settlement.titles.set")) {
                                        String oldRankName = rank.getTag(resident.getSettlement());
                                        resident.getSettlement().setDefaultTitle(rank, title);
                                        Message.get("command.settlement.titles.set.success").tell(resident, "title", title, "rank", oldRankName);
                                        gui.draw();
                                    }
                                    return true;
                                case RIGHT:
                                case SHIFT_RIGHT:
                                    if (resident.hasPermission("settlement.titles.remove")) {
                                        resident.getSettlement().removeTitle(rank, title);
                                        Message.get("command.settlement.titles.remove.success").tell(resident,
                                                "title", title,
                                                "rank", rank.getTag(resident.getSettlement())
                                        );
                                        new TitlesGUI(resident, rank).open();
                                    }
                                    return true;
                            }
                        }
                        return true;
                    }, description.toArray(new String[0]));
                }));
            }

            gui.addElement(group);

            if (resident.hasPermission("settlement.titles.add")
                    && !rank.above(resident.getSettlementRank())
                    && resident.getSettlement().getTitles(rank).size() < Config.Settlement.TITLES_PER_RANK) {
                gui.addElement(new StaticGuiElement('a', ItemUtils.toIcon(Material.WRITABLE_BOOK), click -> {
                    if (resident.hasPermission("settlement.titles.add")) {
                        new AnvilGUI.Builder()
                                .item(ItemUtils.toIcon(Material.WRITABLE_BOOK))
                                .title(getMessage("add.anvil.title").getReplacedText(
                                        "rank", rank.getTag(resident.getSettlement()),
                                        "state", resident.getSettlement().getStateTag()
                                ))
                                .text(getMessage("add.anvil.placeholder").getReplacedText(
                                        "rank", rank.getTag(resident.getSettlement()),
                                        "state", resident.getSettlement().getStateTag()
                                ))
                                .onClose(player -> Utils.runLater(() -> new TitlesGUI(resident, rank).open(), 1))
                                .onComplete((player, text) -> {
                                    text = ChatColor.stripColor(text);
                                    if (rank.above(resident.getSettlementRank())) {
                                        return AnvilGUI.Response.text(getError("cant-edit-higher").getReplacedText("rank", resident.getSettlementRank().getTag(resident.getSettlement())));
                                    } else if (resident.getSettlement().getTitles(rank).size() >= Config.Settlement.TITLES_PER_RANK) {
                                        return AnvilGUI.Response.text(getError("too-many").getReplacedText(
                                                "rank", resident.getSettlementRank().getTag(),
                                                "max", String.valueOf(Config.Settlement.TITLES_PER_RANK)
                                        ));
                                    } else if (text.length() > 16) {
                                        return AnvilGUI.Response.text(getError("too-long").getReplacedText("input", text, "amount", "16"));
                                    } else if (!SettlementTitlesCommand.TITLE_PATTERN.matcher(text).matches()) {
                                        return AnvilGUI.Response.text(getError("invalid-characters").getReplacedText("input", text));
                                    } else if (Utils.containsAnyCurses(text)) {
                                        return AnvilGUI.Response.text(getError("prohibited-words").getReplacedText("input", text));
                                    } else if (resident.getSettlement().addTitle(rank, text)) {
                                        Message.get("command.settlement.titles.add.success").tell(resident, "title", text, "rank", rank.getTag(resident.getSettlement()));
                                        return AnvilGUI.Response.close();
                                    } else {
                                        return AnvilGUI.Response.text(getError("already-exists").getReplacedText("input", text, "rank", rank.getTag(resident.getSettlement())));
                                    }
                                })
                                .plugin(SettlementsPlugin.getInstance())
                                .open((Player) click.getEvent().getWhoClicked());
                    }
                    return true;
                }, getText("add.title",
                        "rank", rank.getTag(resident.getSettlement()),
                        "state", resident.getSettlement().getStateTag()
                )));
            }
        }

        gui.addElement(new StaticGuiElement('b', ItemUtils.toIcon(Material.BARRIER), click -> {
            new TitleGUI(resident).open();
            return true;
        }, getText("back")));
    }

    public void open() {
        gui.show(resident.getPlayer());
    }
}
