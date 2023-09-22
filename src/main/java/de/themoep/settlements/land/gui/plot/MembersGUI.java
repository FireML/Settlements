package de.themoep.settlements.land.gui.plot;

import uk.firedev.poleislib.Utils.ItemUtils;
import uk.firedev.poleislib.libs.themoep.inventorygui.DynamicGuiElement;
import uk.firedev.poleislib.libs.themoep.inventorygui.GuiBackElement;
import uk.firedev.poleislib.libs.themoep.inventorygui.GuiElementGroup;
import uk.firedev.poleislib.libs.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.PlayerSelectGUI;
import de.themoep.settlements.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class MembersGUI extends PlotGUI {

    public MembersGUI(Resident resident, OwnedLand editedLand) {
        super(resident, editedLand, "members");

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
                                LandManager.getInstance().syncAndSave(currentLand);
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
            new PlayerSelectGUI(resident, name() + ".players",
                    player -> player != click.getWhoClicked() && !editedLand.isPlotOwner(player) && !editedLand.getPlotMembers().contains(player.getUniqueId()),
                    player -> {
                OwnedLand currentLand = LandManager.getInstance().getLand(landLocation);
                if (currentLand != null && (currentLand.isPlotOwner((Player) click.getWhoClicked()) || currentLand.canAdmin(ResidentManager.getInstance().getResident((Player) click.getWhoClicked())))) {
                    if (currentLand.getPlotMembers().add(player.getUniqueId())) {
                        LandManager.getInstance().syncAndSave(currentLand);
                        getMessage("command.plot.members.add.success").tell(click.getWhoClicked(), "name", player.getName(), "uuid", player.getUniqueId().toString());
                    }
                }
            }).open();
            return true;
        }, getText("add")));

        GuiElementGroup pages = new GuiElementGroup('p');
        gui.addElement(pages);

        for (int i = 0; i < gui.getPageAmount(resident.getPlayer()) && i < pages.getSlots().length; i++) {
            int page = i;
            pages.addElement(new DynamicGuiElement('p', who -> {
                ItemStack icon = getIcon("page");
                if (page == gui.getPageNumber(who)) {
                    ItemUtils.setGlowing(icon, true);
                }
                return new StaticGuiElement('p', icon, page + 1, click -> {
                    gui.setPageNumber(click.getWhoClicked(), page);
                    return true;
                }, getText("page", "page", String.valueOf(page + 1), "pages", String.valueOf(gui.getPageAmount(who))));
            }));
        }

        gui.addElement(new GuiBackElement('b', getIcon("back"), true, getText("back")));
    }
}
