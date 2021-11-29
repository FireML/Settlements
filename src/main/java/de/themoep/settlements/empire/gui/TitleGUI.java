package de.themoep.settlements.empire.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.rank.EmpireRank;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.Message;
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
        super("empire.title");
        this.resident = resident;
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title",
                "state", resident.hasEmpire() ? resident.getSettlement().getEmpire().getStateTag() : "",
                "title", resident.getEmpireTitle()
        ), SETUP);
        gui.setCloseAction(close -> false);
        gui.setFiller(BORDER_ITEM);

        GuiElementGroup group = new GuiElementGroup('g');

        if (resident.hasEmpire()) {
            EmpireRank rank = resident.getEmpireRank();
            for (String title : resident.getSettlement().getEmpire().getTitles(rank)) {
                group.addElement(new DynamicGuiElement('g', h -> {
                    ItemStack item = ItemUtils.toIcon(Material.PAPER);
                    List<String> description = new ArrayList<>();
                    description.add(getText("entry.title",
                            "title", title,
                            "rank", rank.getTag(resident.getSettlement().getEmpire())
                    ));
                    if (title.equals(resident.getEmpireTitle())) {
                        ItemUtils.setGlowing(item, true);
                        description.add(getText("entry.current",
                                "title", title,
                                "rank", rank.getTag(resident.getSettlement().getEmpire())
                        ));
                    } else {
                        description.add(getText("entry.select",
                                "title", title,
                                "rank", rank.getTag(resident.getSettlement().getEmpire())
                        ));
                    }
                    return new StaticGuiElement('g',
                            item,
                            click -> {
                                if (resident.hasEmpire() && resident.hasPermission("empire.title") && rank == resident.getEmpireRank() && resident.setEmpireTitle(title)) {
                                    DatabaseManager.getInstance().saveResident(resident);
                                    gui.draw();
                                    Message message = Message.get("command.empire.title.success." + resident.getSettlement().getEmpire().getStateKey() + "." + resident.getEmpireRank().getKey());
                                    if (!message.exists()) {
                                        message = Message.get("command.empire.title.success.default");
                                    }
                                    message.tell(resident.getSettlement().getEmpire().getOnlineResidents(),
                                            "rank", resident.getEmpireRank().getTag(resident.getSettlement().getEmpire()),
                                            "title", resident.getEmpireTitle(),
                                            "name", resident.getName(),
                                            "empirename", resident.getSettlement().getEmpire().getName(),
                                            "empirestate", resident.getSettlement().getEmpire().getStateTag()
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

        if (resident.hasEmpire() && resident.hasPermission("empire.titles")) {
            gui.addElement(new StaticGuiElement('e', ItemUtils.toIcon(Material.WRITABLE_BOOK), click -> {
                if (resident.hasEmpire() && resident.hasPermission("empire.titles")) {
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
