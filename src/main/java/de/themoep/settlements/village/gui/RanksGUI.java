package de.themoep.settlements.village.gui;

import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.rank.SettlementRank;
import de.themoep.settlements.utils.ConfirmationGui;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.village.Settlement;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class RanksGUI extends SettlementsGUI {
    private final static String[] SETUP = {
            "         ",
            "         ",
            "ggggggggg",
            "ggggggggr",
            "         ",
            "    b    "
    };
    private final Player player;

    private final InventoryGui gui;

    public RanksGUI(Player player) {
        super("ranks");
        this.player = player;
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title"), SETUP);
        gui.setCloseAction(close -> false);
        gui.setFiller(BORDER_ITEM);

        GuiElementGroup group = new GuiElementGroup('g');

        Resident res = ResidentManager.getInstance().getResident(player);
        if (res.hasSettlement()) {
            for (SettlementRank rank : SettlementRank.values()) {
                List<String> text = new ArrayList<>();
                text.add(getText("entry.title",
                        "rank", rank.getTag(res.getSettlement())
                ));
                if (res.getSettlementRank().above(rank) || res.getSettlementRank() == SettlementRank.getChief()) {
                    text.add(getText("entry.edit",
                            "rank", rank.getTag(res.getSettlement())
                    ));
                }
                group.addElement(new StaticGuiElement('g',
                        ItemUtils.setGlowing(rank.getItem(), rank == res.getSettlementRank()),
                        click -> {
                            Resident r = ResidentManager.getInstance().getResident((Player) click.getEvent().getWhoClicked());
                            if (r.hasSettlement() && r.hasPermission("settlement.ranks.perms")) {
                                new RankPermGroupsGUI(r, rank).open();
                            }
                            return true;
                        },
                        text.toArray(new String[0])
                ));
            }

            if (res.hasPermission("settlement.ranks.perms.reset")) {
                gui.addElement(new StaticGuiElement('r', ItemUtils.toIcon(Material.ORANGE_WOOL), click -> {
                    Resident r = ResidentManager.getInstance().getResident((Player) click.getEvent().getWhoClicked());
                    if (r.hasSettlement() && r.hasPermission("settlement.ranks.perms.reset")) {
                        Settlement settlement = r.getSettlement();
                        new ConfirmationGui(RanksGUI.this, "reset", () -> {
                            if (r.hasPermission("settlement.ranks.perms.reset")) {
                                settlement.getRanksPermissions().clear();
                                open();
                                settlement.save();
                            }
                        }, this::open,
                                "state", settlement.getStateTag(),
                                "name", settlement.getName()).open((Player) click.getEvent().getWhoClicked());
                    }
                    return true;
                }, getText("reset.title")));
            }
        }

        gui.addElement(group);

        gui.addElement(new StaticGuiElement('b', ItemUtils.toIcon(Material.BARRIER), click -> {
            new SettingsGUI(res).open();
            return true;
        }, getText("back")));
    }

    public void open() {
        gui.show(player);
    }
}
