package me.crz.minecraft.kingdoms.empire.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.landflags.BuildState;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class BuildGui extends KingdomsGUI {
    private final static String[] SETUP = {
            "         ",
            "         ",
            "ggggggggg",
            "ggggggggg",
            "         ",
            "    b    "
    };

    private final InventoryGui gui;

    public BuildGui() {
        super("empire.build");
        gui = new InventoryGui(KingdomsPlugin.getInstance(), getText("title"), SETUP);
        gui.setCloseAction(close -> false);
        gui.setFiller(BORDER_ITEM);

        GuiElementGroup group = new GuiElementGroup('g');

        for (BuildState state : BuildState.values(BuildState.AppliesTo.SETTLEMENT)) {
            group.addElement(new DynamicGuiElement('g', (h) -> {
                Resident resident = ResidentManager.getInstance().getResident((Player) h);
                if (resident.hasEmpire()) {
                    Map<String, String> replacements = Utils.toMap(
                            "state", state.getTag(),
                            "description", getMessage("build." + state.getKey()).getReplacedText(
                                    "state", resident.getVillage().getEmpire().getStateTag())
                    );
                    if (resident.getVillage().getEmpire().getBuildState() == state) {
                        return new StaticGuiElement('g',
                                new ItemStack(Material.LIME_WOOL),
                                getMessage("selected-state").getReplacedText(replacements),
                                getMessage("description").getReplacedText(replacements)
                        );
                    }
                    return new StaticGuiElement('g',
                            new ItemStack(Material.LIGHT_GRAY_WOOL),
                            click -> {
                                if (resident.hasEmpire() && resident.hasPermission("empire.build")) {
                                    resident.getVillage().getEmpire().setBuildState(state);
                                    Message.get("command.empire.build.success").tell(resident, "state", state.getTag());
                                    Utils.runAsync(() -> DatabaseManager.getInstance().saveEmpire(resident.getVillage().getEmpire()));
                                    gui.draw();
                                }
                                return true;
                            },
                            getMessage("set-state").getReplacedText(replacements),
                            getMessage("description").getReplacedText(replacements)
                    );
                }
                return null;
            }));
        }

        gui.addElement(group);

        gui.addElement(new StaticGuiElement('b', new ItemStack(Material.BARRIER), click -> {
            new SettingsGUI((Player) click.getEvent().getWhoClicked()).open();
            return true;
        }, getText("back")));
    }

    public void open(final Player p) {
        gui.show(p);
    }
}
