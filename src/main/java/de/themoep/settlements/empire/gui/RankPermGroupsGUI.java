package de.themoep.settlements.empire.gui;

import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.rank.EmpireRank;
import de.themoep.settlements.rank.PermissionGroup;
import de.themoep.settlements.utils.ConfirmationGui;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class RankPermGroupsGUI extends SettlementsGUI {
    private final static String[] SETUP = {
            "         ",
            "         ",
            "ggggggggg",
            "ggggggggr",
            "         ",
            "    b    "
    };
    private final Resident resident;
    private final InventoryGui gui;

    public RankPermGroupsGUI(Resident res, EmpireRank rank) {
        super("empire.rank-perm-groups");
        this.resident = res;
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title",
                "rank", rank.getTag(res.getVillage().getEmpire())
        ), SETUP);
        gui.setCloseAction(close -> false);
        gui.setFiller(BORDER_ITEM);

        GuiElementGroup group = new GuiElementGroup('g');

        if (res.hasEmpire()) {
            for (PermissionGroup permGroup : EmpireRank.getPermGroups()) {
                group.addElement(new StaticGuiElement('g', permGroup.getItem(), click -> {
                    Resident r = ResidentManager.getInstance().getResident((Player) click.getEvent().getWhoClicked());
                    if (r.hasEmpire() && r.hasPermission("empire.perms")) {
                        new RankPermsGUI(r, rank, permGroup).open();
                    }
                    return true;
                }, getText("groups." + permGroup.getName(),
                        "rank", rank.getTag(),
                        "state", res.getVillage().getStateTag()
                )));
            }

            if (res.hasVillage() && res.hasPermission("empire.perms.reset")) {
                gui.addElement(new StaticGuiElement('r', ItemUtils.toIcon(Material.ORANGE_WOOL), click -> {
                    Resident r = ResidentManager.getInstance().getResident((Player) click.getEvent().getWhoClicked());
                    if (r.hasEmpire() && r.hasPermission("empire.perms.reset")) {
                        Empire empire = r.getVillage().getEmpire();
                        new ConfirmationGui(RankPermGroupsGUI.this, "reset", () -> {
                            if (r.hasPermission("empire.perms.reset")) {
                                empire.getRanksPermissions().remove(rank.name().toLowerCase());
                                open();
                                Utils.runAsync(() -> DatabaseManager.getInstance().save(empire));
                            }
                        }, this::open,
                                "state", empire.getStateTag(),
                                "name", empire.getName(),
                                "rank", rank.getTag(empire)).open((Player) click.getEvent().getWhoClicked()
                        );
                    }
                    return true;
                }, getText("reset.title", "rank", rank.getTag(res.getVillage().getEmpire()))));
            }
        }

        gui.addElement(group);

        gui.addElement(new StaticGuiElement('b', ItemUtils.toIcon(Material.BARRIER), click -> {
            new RanksGUI((Player) click.getEvent().getWhoClicked()).open();
            return true;
        }, getText("back")));
    }

    public void open() {
        gui.show(resident.getPlayer());
    }
}
