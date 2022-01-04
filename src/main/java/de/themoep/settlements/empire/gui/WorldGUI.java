package de.themoep.settlements.empire.gui;

import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.command.CommandManager;
import de.themoep.settlements.command.empire.EmpireHomeCommand;
import de.themoep.settlements.command.empire.EmpireSetHomeCommand;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.npc.NpcType;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.ConfigurableGui;

public class WorldGUI extends ConfigurableGui {

    public WorldGUI(Resident resident, Empire empire) {
        super(resident, "empire.world", "type", empire.getNpcData(NpcType.TRAVELING_MERCHANT).getTag());

        if (empire.hasWorld()) {
            gui.addElement(new StaticGuiElement('w', getIcon("world"), click -> {
                if (empire.canAccess(resident)) {
                    if (click.getType().isShiftClick()) {
                        if (resident.hasPermission("empire.setworldspawn")) {
                            click.getGui().close();
                            ((EmpireSetHomeCommand) CommandManager.getInstance().getCommand("empire").getSubCommands().get("setworldspawn")).run(resident, "empire", "setworldspawn");
                        }
                    } else if (click.getType().isLeftClick()) {
                        click.getGui().close();
                        ((EmpireHomeCommand) CommandManager.getInstance().getCommand("empire").getSubCommands().get("world")).run(resident, "empire", "world");
                    }
                }
                return true;
            }, getText("world",
                    "name", empire.getName(),
                    "action", getText(empire.canAccess(resident) ? "unlockables.unlocked.teleport" : "unlcokables.locked.permission")
            ), resident.hasPermission("empire.setworldspawn") ? getText("setworldspawn") : null));
        } else {
            gui.addElement(new StaticGuiElement('w', getIcon("world"), getText("no-world", "name", empire.getName())));
        }

        gui.addElement(new GuiBackElement('b', getIcon("back"), true, getText("close")));
    }
}
