package me.crz.minecraft.kingdoms.village.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.rank.VillageRank;
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
        super("title");
        this.resident = resident;
        gui = new InventoryGui(KingdomsPlugin.getInstance(), getText("title",
                "state", resident.hasVillage() ? resident.getVillage().getStateTag() : "",
                "title", resident.getVillageTitle()
        ), SETUP);
        gui.setCloseAction(close -> false);
        gui.setFiller(BORDER_ITEM);

        GuiElementGroup group = new GuiElementGroup('g');

        if (resident.hasVillage()) {
            VillageRank rank = resident.getVillageRank();
            for (String title : resident.getVillage().getTitles(rank)) {
                group.addElement(new DynamicGuiElement('g', h -> {
                    ItemStack item = new ItemStack(Material.PAPER);
                    List<String> description = new ArrayList<>();
                    description.add(getText("entry.title",
                            "title", title,
                            "rank", rank.getTag(resident.getVillage())
                    ));
                    if (title.equals(resident.getVillageTitle())) {
                        ItemUtils.setGlowing(item, true);
                        description.add(getText("entry.current",
                                "title", title,
                                "rank", rank.getTag(resident.getVillage())
                        ));
                    } else {
                        description.add(getText("entry.select",
                                "title", title,
                                "rank", rank.getTag(resident.getVillage())
                        ));
                    }
                    return new StaticGuiElement('g',
                            item,
                            click -> {
                                if (resident.hasVillage() && resident.hasPermission("village.title") && rank == resident.getVillageRank() && resident.setVillageTitle(title)) {
                                    Utils.runAsync(() -> DatabaseManager.getInstance().saveResident(resident));
                                    gui.draw();
                                    Message message = Message.get("command.village.title.success." + resident.getVillage().getState().getKey() + "." + resident.getVillageRank().getKey());
                                    if (!message.exists()) {
                                        message = Message.get("command.village.title.success.default");
                                    }
                                    message.tell(resident.getVillage().getOnlineResidents(),
                                            "rank", resident.getVillageRank().getTag(resident.getVillage()),
                                            "title", resident.getVillageTitle(),
                                            "name", resident.getName(),
                                            "villagename", resident.getVillage().getName(),
                                            "villagestate", resident.getVillage().getState().getTag()
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

        if (resident.hasVillage() && resident.hasPermission("village.titles")) {
            gui.addElement(new StaticGuiElement('e', new ItemStack(Material.WRITABLE_BOOK), click -> {
                if (resident.hasVillage() && resident.hasPermission("village.titles")) {
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
