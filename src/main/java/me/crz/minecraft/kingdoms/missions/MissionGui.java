package me.crz.minecraft.kingdoms.missions;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.empire.EmpireMission;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.utils.ItemUtils;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

public class MissionGui extends KingdomsGUI {

    private static final String[] SETUP = new String[] {
            "bmmmmmmmm",
            "mmmmmmmmm",
            "mmmmmmmmm"
    };
    private final InventoryGui gui;
    private final Resident resident;

    public MissionGui(Resident resident) {
        super("missions");
        this.resident = resident;
        gui = new InventoryGui(KingdomsPlugin.getInstance(), getText("title"), SETUP);

        gui.addElement(new StaticGuiElement('b', ItemUtils.toIcon(Material.ORANGE_BANNER), getText("info")));

        GuiElementGroup group = new GuiElementGroup('m');
        for (MissionData missionData : resident.getVillage().getMissionData()) {
            group.addElement(new DynamicGuiElement('m', who -> {
                StaticGuiElement element = new StaticGuiElement('m', missionData.getMission().getIcon(), getText("entry",
                        "title", missionData.getMission().getTag(),
                        "requirements", missionData.getMission().getRequirements().entrySet().stream().map(e ->
                                getText("requirement." + (missionData.getProgress(e.getKey()) < e.getValue() ? "todo" : "done"),
                                        "progress", String.valueOf(missionData.getProgress(e.getKey())),
                                        "required", String.valueOf(e.getValue()),
                                        "type", WordUtils.capitalizeFully(e.getKey().replace('_', ' '))
                        )).collect(Collectors.joining("\n")),
                        "action", missionData.isFinished()
                                ? getText("finished")
                                : (missionData.getMission() instanceof EmpireMission && ((EmpireMission) missionData.getMission()).onlyInEmpireWorld()
                                        ? getText("only-in-empire", "empire", resident.getVillage().getEmpire().getName())
                                        : getText("types." + missionData.getMission().getType().getKey())
                                )
                ));
                if (missionData.getMission().getType() == Mission.Type.TURN_IN && !missionData.isFinished()) {
                    element.setAction(click -> {
                        new TurnInGUI(resident, missionData).open();
                        return true;
                    });
                }
                return element;
            }));
        }
        gui.addElement(group);
    }

    public void open() {
        gui.show(resident.getPlayer());
    }
}
