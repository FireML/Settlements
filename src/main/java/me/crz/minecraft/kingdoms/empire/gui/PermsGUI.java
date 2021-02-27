package me.crz.minecraft.kingdoms.empire.gui;

import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.utils.ConfirmationGui;
import me.crz.minecraft.kingdoms.utils.ItemUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.resident.ResidentManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PermsGUI extends KingdomsGUI {
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

    public PermsGUI(Player player) {
        super("empire.perms");
        this.player = player;
        gui = new InventoryGui(KingdomsPlugin.getInstance(), getText("title"), SETUP);
        gui.setCloseAction(close -> false);
        gui.setFiller(BORDER_ITEM);

        GuiElementGroup group = new GuiElementGroup('g');

        Resident res = ResidentManager.getInstance().getResident(player);
        if (res.hasEmpire()) {
            for (EmpireRank rank : EmpireRank.values()) {
                List<String> text = new ArrayList<>();
                text.add(getText("entry.title",
                        "rank", rank.getTag(res.getVillage().getEmpire())
                ));
                if (res.getEmpireRank().above(rank) || res.getEmpireRank() == EmpireRank.getLeader()) {
                    text.add(getText("entry.edit",
                            "rank", rank.getTag(res.getVillage().getEmpire())
                    ));
                }
                group.addElement(new StaticGuiElement('g',
                        ItemUtils.setGlowing(rank.getItem(), rank == res.getEmpireRank()),
                        click -> {
                            Resident r = ResidentManager.getInstance().getResident((Player) click.getEvent().getWhoClicked());
                            if (r.hasEmpire() && r.hasPermission("empire.perms")) {
                                new RankPermGroupsGUI(r, rank).open();
                            }
                            return true;
                        },
                        text.toArray(new String[0])
                ));
            }

            if (res.hasPermission("empire.perms.reset")) {
                gui.addElement(new StaticGuiElement('r', ItemUtils.toIcon(Material.ORANGE_WOOL), click -> {
                    Resident r = ResidentManager.getInstance().getResident((Player) click.getEvent().getWhoClicked());
                    if (r.hasEmpire() && r.hasPermission("empire.perms.reset")) {
                        Empire empire = r.getVillage().getEmpire();
                        new ConfirmationGui(PermsGUI.this, "reset", () -> {
                            if (r.hasPermission("empire.perms.reset")) {
                                empire.getRanksPermissions().clear();
                                open();
                                Utils.runAsync(() -> DatabaseManager.getInstance().save(empire));
                            }
                        }, this::open,
                                "state", empire.getStateTag(),
                                "name", empire.getName()).open((Player) click.getEvent().getWhoClicked());
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
