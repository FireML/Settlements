package de.themoep.settlements.village.gui;

import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.rank.PermissionGroup;
import de.themoep.settlements.rank.VillageRank;
import de.themoep.settlements.utils.ConfirmationGui;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.village.Village;
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

    public RankPermGroupsGUI(Resident res, VillageRank rank) {
        super("rank-perm-groups");
        this.resident = res;
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title", "rank", rank.getTag(res.getVillage())), SETUP);
        gui.setCloseAction(close -> false);
        gui.setFiller(BORDER_ITEM);

        GuiElementGroup group = new GuiElementGroup('g');

        if (res.hasVillage()) {
            for (PermissionGroup permGroup : VillageRank.getPermGroups()) {
                group.addElement(new StaticGuiElement('g', permGroup.getItem(), click -> {
                    Resident r = ResidentManager.getInstance().getResident((Player) click.getEvent().getWhoClicked());
                    if (r.hasVillage() && r.hasPermission("village.perms")) {
                        new RankPermsGUI(r, rank, permGroup).open();
                    }
                    return true;
                }, getText("groups." + permGroup.getName(),
                        "rank", rank.getTag(),
                        "state", res.getVillage().getStateTag()
                )));
            }

            if (res.hasVillage() && res.hasPermission("village.perms.reset")) {
                gui.addElement(new StaticGuiElement('r', ItemUtils.toIcon(Material.ORANGE_WOOL), click -> {
                    Resident r = ResidentManager.getInstance().getResident((Player) click.getEvent().getWhoClicked());
                    if (r.hasVillage() && r.hasPermission("village.perms.reset")) {
                        Village village = r.getVillage();
                        new ConfirmationGui(RankPermGroupsGUI.this, "reset", () -> {
                            if (r.hasPermission("village.perms.reset")) {
                                village.getRanksPermissions().remove(rank.name().toLowerCase());
                                open();
                                DatabaseManager.getInstance().save(village);
                            }
                        }, this::open,
                                "state", village.getStateTag(),
                                "name", village.getName(),
                                "rank", rank.getTag(village)).open((Player) click.getEvent().getWhoClicked()
                        );
                    }
                    return true;
                }, getText("reset.title", "rank", rank.getTag(res.getVillage()))));
            }
        }

        gui.addElement(group);

        gui.addElement(new StaticGuiElement('b', ItemUtils.toIcon(Material.BARRIER), click -> {
            new PermsGUI((Player) click.getEvent().getWhoClicked()).open();
            return true;
        }, getText("back")));
    }

    public void open() {
        gui.show(resident.getPlayer());
    }
}
