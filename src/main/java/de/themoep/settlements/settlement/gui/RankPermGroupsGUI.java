package de.themoep.settlements.settlement.gui;

import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.rank.PermissionGroup;
import de.themoep.settlements.rank.SettlementRank;
import de.themoep.settlements.utils.ConfirmationGui;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
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

    public RankPermGroupsGUI(Resident res, SettlementRank rank) {
        super("rank-perm-groups");
        this.resident = res;
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title", "rank", rank.getTag(res.getSettlement())), SETUP);
        gui.setCloseAction(close -> false);
        gui.setFiller(BORDER_ITEM);

        GuiElementGroup group = new GuiElementGroup('g');

        if (res.hasSettlement()) {
            for (PermissionGroup permGroup : SettlementRank.getPermGroups()) {
                group.addElement(new StaticGuiElement('g', permGroup.getItem(), click -> {
                    Resident r = ResidentManager.getInstance().getResident((Player) click.getEvent().getWhoClicked());
                    if (r.hasSettlement() && r.hasPermission("settlement.ranks.perms")) {
                        new RankPermsGUI(r, rank, permGroup).open();
                    }
                    return true;
                }, getText("groups." + permGroup.getName(),
                        "rank", rank.getTag(),
                        "state", res.getSettlement().getStateTag()
                )));
            }

            if (res.hasSettlement() && res.hasPermission("settlement.ranks.perms.reset")) {
                gui.addElement(new StaticGuiElement('r', ItemUtils.toIcon(Material.ORANGE_WOOL), click -> {
                    Resident r = ResidentManager.getInstance().getResident((Player) click.getEvent().getWhoClicked());
                    if (r.hasSettlement() && r.hasPermission("settlement.ranks.perms.reset")) {
                        Settlement settlement = r.getSettlement();
                        new ConfirmationGui(RankPermGroupsGUI.this, "reset", () -> {
                            if (r.hasPermission("settlement.ranks.perms.reset")) {
                                settlement.getRanksPermissions().remove(rank.name().toLowerCase());
                                open();
                                DatabaseManager.getInstance().save(settlement);
                            }
                        }, this::open,
                                "state", settlement.getStateTag(),
                                "name", settlement.getName(),
                                "rank", rank.getTag(settlement)).open((Player) click.getEvent().getWhoClicked()
                        );
                    }
                    return true;
                }, getText("reset.title", "rank", rank.getTag(res.getSettlement()))));
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
