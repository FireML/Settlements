package me.crz.minecraft.kingdoms.village.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.GuiPageElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.BuildState;
import me.crz.minecraft.kingdoms.land.UseFlag;
import me.crz.minecraft.kingdoms.land.UseFlagManager;
import me.crz.minecraft.kingdoms.utils.ItemUtils;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.resident.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class UseGui extends KingdomsGUI {
    private static final String[] SETUP = {
            "         ",
            "uuuuuuuuu",
            "uuuuuuuuu",
            "uuuuuuuuu",
            "aaaaaaaaa",
            "p   b   n",
    };
    private final InventoryGui gui;

    public UseGui() {
        super("use");
        gui = new InventoryGui(KingdomsPlugin.getInstance(), getText("title"), SETUP);
        gui.setCloseAction(close -> false);
        gui.setFiller(BORDER_ITEM);

        GuiElementGroup useGroup = new GuiElementGroup('u');
        for (UseFlag u : UseFlagManager.getInstance().getUseFlags()) {
            useGroup.addElement(new DynamicGuiElement('u', h -> {
                Village village = VillageManager.getInstance().getVillageFromPlayer((Player) h);
                if (village != null) {
                    BuildState state = village.getUseState(u);
                    if (state == null) {
                        state = village.getDefaultBuildState();
                    }
                    return new StaticGuiElement('u', u.getItem(), click -> {
                        Resident res = ResidentManager.getInstance().getResident((Player) click.getEvent().getWhoClicked());
                        if (res.hasVillage() && res.hasPermission("village.use")) {
                            BuildState currentState = res.getVillage().getUseState(u);
                            if (currentState == null) {
                                currentState = res.getVillage().getDefaultBuildState();
                            }
                            BuildState changeTo = click.getType().isLeftClick()
                                    ? currentState.next(BuildState.AppliesTo.SETTLEMENT)
                                    : currentState.previous(BuildState.AppliesTo.SETTLEMENT);
                            res.getVillage().setUseState(u, changeTo);
                            Message.get("command.village.use.success").tell(click.getEvent().getWhoClicked(),
                                    "flag", u.getTag(),
                                    "state", changeTo.getTag()
                            );
                            Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(res.getVillage()));
                            click.getGui().draw();
                        }
                        return true;
                    },getMessage("entry").getReplacedText(
                            "flag", u.getTag(),
                            "state", state.getTag()
                    ));
                }
                return null;
            }));
        }
        gui.addElement(useGroup);

        GuiElementGroup allGroup = new GuiElementGroup('a');
        for (BuildState state : BuildState.values(BuildState.AppliesTo.SETTLEMENT)) {
            allGroup.addElement(new StaticGuiElement('a', state.getItem(), click -> {
                Resident res = ResidentManager.getInstance().getResident((Player) click.getEvent().getWhoClicked());
                if (res.hasVillage() && res.hasPermission("village.use")) {
                    for (UseFlag u : UseFlagManager.getInstance().getUseFlags()) {
                        res.getVillage().setUseState(u, state);
                    }
                    Message.get("command.village.use.success-all").tell(click.getEvent().getWhoClicked(),
                            "state", state.getTag()
                    );
                    Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(res.getVillage()));
                    click.getGui().draw();
                }
                return true;
            }, getMessage("toggle-all").getReplacedText("state", state.getTag())));
        }
        gui.addElement(allGroup);

        gui.addElement(new GuiPageElement('p', ItemUtils.toIcon(Material.ARROW), GuiPageElement.PageAction.PREVIOUS, getText("page.previous", "page", "%prevpage%")));

        gui.addElement(new GuiBackElement('b', ItemUtils.toIcon(Material.BARRIER), getText("back")));

        gui.addElement(new GuiPageElement('n', ItemUtils.toIcon(Material.ARROW), GuiPageElement.PageAction.NEXT, getText("page.next", "page", "%nextpage%")));
    }

    public void open(Player p) {
        gui.show(p);
    }
}
