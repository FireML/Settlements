package me.crz.minecraft.kingdoms.empire.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.utils.ItemUtils;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TitleGUI extends KingdomsGUI {
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
        gui = new InventoryGui(KingdomsPlugin.getInstance(), getText("title",
                "state", resident.hasVillage() ? resident.getVillage().getStateTag() : "",
                "title", resident.getVillageTitle()
        ), SETUP);
        gui.setCloseAction(close -> false);
        gui.setFiller(BORDER_ITEM);

        GuiElementGroup group = new GuiElementGroup('g');

        if (resident.hasEmpire()) {
            EmpireRank rank = resident.getEmpireRank();
            for (String title : resident.getVillage().getEmpire().getTitles(rank)) {
                group.addElement(new DynamicGuiElement('g', h -> {
                    ItemStack item = new ItemStack(Material.PAPER);
                    List<String> description = new ArrayList<>();
                    description.add(getText("entry.title",
                            "title", title,
                            "rank", rank.getTag(resident.getVillage().getEmpire())
                    ));
                    if (title.equals(resident.getEmpireTitle())) {
                        ItemUtils.setGlowing(item, true);
                        description.add(getText("entry.current",
                                "title", title,
                                "rank", rank.getTag(resident.getVillage().getEmpire())
                        ));
                    } else {
                        description.add(getText("entry.select",
                                "title", title,
                                "rank", rank.getTag(resident.getVillage().getEmpire())
                        ));
                    }
                    return new StaticGuiElement('g',
                            item,
                            click -> {
                                if (resident.hasEmpire() && resident.hasPermission("empire.title") && rank == resident.getEmpireRank() && resident.setEmpireTitle(title)) {
                                    Utils.runAsync(() -> DatabaseManager.getInstance().saveResident(resident));
                                    gui.draw();
                                    Message message = Message.get("command.empire.title.success." + resident.getVillage().getEmpire().getStateKey() + "." + resident.getEmpireRank().getKey());
                                    if (!message.exists()) {
                                        message = Message.get("command.empire.title.success.default");
                                    }
                                    message.tell(resident.getVillage().getEmpire().getOnlineResidents(),
                                            "rank", resident.getVillageRank().getTag(resident.getVillage()),
                                            "title", resident.getVillageTitle(),
                                            "name", resident.getName(),
                                            "empirename", resident.getVillage().getEmpire().getName(),
                                            "empirestate", resident.getVillage().getEmpire().getStateTag()
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
            gui.addElement(new StaticGuiElement('e', new ItemStack(Material.WRITABLE_BOOK), click -> {
                if (resident.hasEmpire() && resident.hasPermission("empire.titles")) {
                    new RankSelectGui(resident, r -> new TitlesGUI(resident, r).open()).open();
                }
                return true;
            }, getText("edit")));
        }

        gui.addElement(new StaticGuiElement('b', new ItemStack(Material.BARRIER), click -> {
            new SettingsGUI(resident).open();
            return true;
        }, getText("back")));
    }

    public void open() {
        gui.show(resident.getPlayer());
    }
}
