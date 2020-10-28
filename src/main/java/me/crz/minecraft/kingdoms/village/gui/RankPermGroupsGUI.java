package me.crz.minecraft.kingdoms.village.gui;

import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.rank.PermissionGroup;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.ConfirmationGui;
import me.crz.minecraft.kingdoms.utils.ItemUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
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

    public RankPermGroupsGUI(Resident res, VillageRank rank) {
        super("rank-perm-groups");
        this.resident = res;
        gui = new InventoryGui(KingdomsPlugin.getInstance(), getText("title", "rank", rank.getTag(res.getVillage())), SETUP);
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
                }));
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
                                Utils.runAsync(() -> DatabaseManager.getInstance().save(village));
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
