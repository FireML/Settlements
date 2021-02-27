package me.crz.minecraft.kingdoms.empire.gui;

import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.rank.PermissionGroup;
import me.crz.minecraft.kingdoms.utils.ConfirmationGui;
import me.crz.minecraft.kingdoms.utils.ItemUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.resident.ResidentManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class RankPermGroupsGUI extends KingdomsGUI {
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
        gui = new InventoryGui(KingdomsPlugin.getInstance(), getText("title",
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
            new PermsGUI((Player) click.getEvent().getWhoClicked()).open();
            return true;
        }, getText("back")));
    }

    public void open() {
        gui.show(resident.getPlayer());
    }
}
