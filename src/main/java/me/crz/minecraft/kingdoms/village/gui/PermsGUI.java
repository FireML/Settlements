package me.crz.minecraft.kingdoms.village.gui;

import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.ConfirmationGui;
import me.crz.minecraft.kingdoms.utils.ItemUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
        super("perms");
        this.player = player;
        gui = new InventoryGui(KingdomsPlugin.getInstance(), getText("title"), SETUP);
        gui.setCloseAction(close -> false);
        gui.setFiller(BORDER_ITEM);

        GuiElementGroup group = new GuiElementGroup('g');

        Resident res = ResidentManager.getInstance().getResident(player);
        if (res.hasVillage()) {
            for (VillageRank rank : VillageRank.values()) {
                List<String> text = new ArrayList<>();
                text.add(getText("entry.title",
                        "rank", rank.getTag(res.getVillage())
                ));
                if (res.getVillageRank().above(rank) || res.getVillageRank() == VillageRank.getChief()) {
                    text.add(getText("entry.edit",
                            "rank", rank.getTag(res.getVillage())
                    ));
                }
                group.addElement(new StaticGuiElement('g',
                        ItemUtils.setGlowing(rank.getItem(), rank == res.getVillageRank()),
                        click -> {
                            Resident r = ResidentManager.getInstance().getResident((Player) click.getEvent().getWhoClicked());
                            if (r.hasVillage() && r.hasPermission("village.perms")) {
                                new RankPermGroupsGUI(r, rank).open();
                            }
                            return true;
                        },
                        text.toArray(new String[0])
                ));
            }

            if (res.hasPermission("village.perms.reset")) {
                gui.addElement(new StaticGuiElement('r', new ItemStack(Material.ORANGE_WOOL), click -> {
                    Resident r = ResidentManager.getInstance().getResident((Player) click.getEvent().getWhoClicked());
                    if (r.hasVillage() && r.hasPermission("village.perms.reset")) {
                        Village village = r.getVillage();
                        new ConfirmationGui(PermsGUI.this, "reset", () -> {
                            if (r.hasPermission("village.perms.reset")) {
                                village.getRanksPermissions().clear();
                                open();
                                Utils.runAsync(() -> DatabaseManager.getInstance().save(village));
                            }
                        }, this::open,
                                "state", village.getStateTag(),
                                "name", village.getName()).open((Player) click.getEvent().getWhoClicked());
                    }
                    return true;
                }, getText("reset.title")));
            }
        }

        gui.addElement(group);

        gui.addElement(new StaticGuiElement('b', new ItemStack(Material.BARRIER), click -> {
            new SettingsGUI(res).open();
            return true;
        }, getText("back")));
    }

    public void open() {
        gui.show(player);
    }
}
