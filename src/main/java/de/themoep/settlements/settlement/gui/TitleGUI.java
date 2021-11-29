package de.themoep.settlements.settlement.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.rank.SettlementRank;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.resident.Resident;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TitleGUI extends SettlementsGUI {
    private final static String[] SETUP = {
            "         ",
            "         ",
            "ggggggggg",
            "ggggggggg",
            "         ",
            "    b   e"
    };
    private final Resident resident;
    private final InventoryGui gui;

    public TitleGUI(Resident resident) {
        super("title");
        this.resident = resident;
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title",
                "state", resident.hasSettlement() ? resident.getSettlement().getStateTag() : "",
                "title", resident.getSettlementTitle()
        ), SETUP);
        gui.setCloseAction(close -> false);
        gui.setFiller(BORDER_ITEM);

        GuiElementGroup group = new GuiElementGroup('g');

        if (resident.hasSettlement()) {
            SettlementRank rank = resident.getSettlementRank();
            for (String title : resident.getSettlement().getTitles(rank)) {
                group.addElement(new DynamicGuiElement('g', h -> {
                    ItemStack item = ItemUtils.toIcon(Material.PAPER);
                    List<String> description = new ArrayList<>();
                    description.add(getText("entry.title",
                            "title", title,
                            "rank", rank.getTag(resident.getSettlement())
                    ));
                    if (title.equals(resident.getSettlementTitle())) {
                        ItemUtils.setGlowing(item, true);
                        description.add(getText("entry.current",
                                "title", title,
                                "rank", rank.getTag(resident.getSettlement())
                        ));
                    } else {
                        description.add(getText("entry.select",
                                "title", title,
                                "rank", rank.getTag(resident.getSettlement())
                        ));
                    }
                    return new StaticGuiElement('g',
                            item,
                            click -> {
                                if (resident.hasSettlement() && resident.hasPermission("settlement.title") && rank == resident.getSettlementRank() && resident.setSettlementTitle(title)) {
                                    DatabaseManager.getInstance().saveResident(resident);
                                    gui.draw();
                                    Message message = Message.get("command.settlement.title.success." + resident.getSettlement().getState().getKey() + "." + resident.getSettlementRank().getKey());
                                    if (!message.exists()) {
                                        message = Message.get("command.settlement.title.success.default");
                                    }
                                    message.tell(resident.getSettlement().getOnlineResidents(),
                                            "rank", resident.getSettlementRank().getTag(resident.getSettlement()),
                                            "title", resident.getSettlementTitle(),
                                            "name", resident.getName(),
                                            "settlementname", resident.getSettlement().getName(),
                                            "settlementstate", resident.getSettlement().getState().getTag()
                                    );
                                }
                                return true;
                            },
                            description.toArray(new String[0])
                    );
                }));
            }
        }

        gui.addElement(group);

        if (resident.hasSettlement() && resident.hasPermission("settlement.titles")) {
            gui.addElement(new StaticGuiElement('e', ItemUtils.toIcon(Material.WRITABLE_BOOK), click -> {
                if (resident.hasSettlement() && resident.hasPermission("settlement.titles")) {
                    new RankSelectGUI(resident, r -> new TitlesGUI(resident, r).open()).open();
                }
                return true;
            }, getText("edit")));
        }

        gui.addElement(new StaticGuiElement('b', ItemUtils.toIcon(Material.BARRIER), click -> {
            new SettingsGUI(resident).open();
            return true;
        }, getText("back")));
    }

    public void open() {
        gui.show(resident.getPlayer());
    }
}
