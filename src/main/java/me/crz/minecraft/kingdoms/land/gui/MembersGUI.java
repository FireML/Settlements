package me.crz.minecraft.kingdoms.land.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.GuiPageElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.resident.ResidentManager;
import me.crz.minecraft.kingdoms.utils.ItemUtils;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.PlayerSelectGUI;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class MembersGUI extends PlotGUI {
    private final static String[] SETUP = {
            "         ",
            "mmmmmmmmm",
            "mmmmmmmmm",
            "mmmmmmmmm",
            "mmmmmmmma",
            "p   b   n"
    };
    private final InventoryGui gui;
    private final SimpleChunkLocation landLocation;

    public MembersGUI(OwnedLand editedLand) {
        super("members");
        landLocation = editedLand.getLocation().copy();
        gui = new InventoryGui(KingdomsPlugin.getInstance(), getText("title"), SETUP);
        gui.setCloseAction(close -> false);
        gui.setFiller(BORDER_ITEM);

        gui.addElement(new DynamicGuiElement('m', viewer -> {
            GuiElementGroup group = new GuiElementGroup('m');
            OwnedLand land = LandManager.getInstance().getLand(landLocation);

            if (land != null) {
                for (UUID memberId : land.getPlotMembers()) {
                    String memberName = Utils.getPlayerName(memberId);
                    group.addElement(new StaticGuiElement('m', ItemUtils.getHead(memberId, memberName), click -> {
                        OwnedLand currentLand = LandManager.getInstance().getLand(landLocation);
                        if (currentLand != null && (currentLand.isPlotOwner((Player) click.getWhoClicked()) || currentLand.canAdmin(ResidentManager.getInstance().getResident((Player) click.getWhoClicked())))) {
                            if (currentLand.getPlotMembers().remove(memberId)) {
                                LandManager.getInstance().saveLand(currentLand);
                                Message.get("command.plot.members.remove.success").tell(click.getWhoClicked(), "name", memberName, "uuid", memberId.toString());
                                gui.draw();
                            }
                        }
                        return true;
                    }, getText("element",
                            "name", memberName,
                            "uuid", memberId.toString()
                    )));
                }
            }
            return group;
        }));

        gui.addElement(new StaticGuiElement('a', new ItemStack(Material.PLAYER_HEAD), click -> {
            new PlayerSelectGUI(this, "players",
                    BORDER_ITEM,
                    player -> player != click.getWhoClicked() && !editedLand.isPlotOwner(player) && !editedLand.getPlotMembers().contains(player.getUniqueId()),
                    player -> {
                OwnedLand currentLand = LandManager.getInstance().getLand(landLocation);
                if (currentLand != null && (currentLand.isPlotOwner((Player) click.getWhoClicked()) || currentLand.canAdmin(ResidentManager.getInstance().getResident((Player) click.getWhoClicked())))) {
                    if (currentLand.getPlotMembers().add(player.getUniqueId())) {
                        LandManager.getInstance().saveLand(currentLand);
                        getMessage("command.plot.members.add.success").tell(click.getWhoClicked(), "name", player.getName(), "uuid", player.getUniqueId().toString());
                    }
                }
            }, editedLand.getReplacements()).open((Player) click.getWhoClicked());
            return true;
        }, getText("add")));

        gui.addElement(new GuiPageElement('p', ItemUtils.toIcon(Material.ARROW), GuiPageElement.PageAction.PREVIOUS, getText("page.previous", "page", "%prevpage%")));

        gui.addElement(new GuiBackElement('b', ItemUtils.toIcon(Material.BARRIER), getText("back")));

        gui.addElement(new GuiPageElement('n', ItemUtils.toIcon(Material.ARROW), GuiPageElement.PageAction.NEXT, getText("page.next", "page", "%nextpage%")));
    }

    public void open(final Player p) {
        // One last check if we can really open this GUI
        OwnedLand currentLand = LandManager.getInstance().getLand(landLocation);
        if (currentLand != null && (currentLand.isPlotOwner(p) || currentLand.canAdmin(ResidentManager.getInstance().getResident(p)))) {
            gui.show(p);
        } else {
            getError("not-your-plot").tell(p);
        }
    }
}
