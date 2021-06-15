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
                        MissionData currentData = resident.getVillage().getMissionData(missionData.getMission().getId());
                        if (!currentData.isFinished() && click.getEvent().getCursor() != null
                                && currentData.requires(click.getEvent().getCursor().getType().name())) {
                            if (click.getType().isShiftClick()) {
                                String type = click.getEvent().getCursor().getType().name();
                                long left = currentData.getMission().getRequirement(type) - currentData.getProgress(type);
                                if (left < click.getEvent().getCursor().getAmount()) {
                                    currentData.addProgress(type, left);
                                    click.getEvent().getCursor().setAmount((int) (click.getEvent().getCursor().getAmount() - left));
                                } else {
                                    currentData.addProgress(type, click.getEvent().getCursor().getAmount());
                                    click.getEvent().setCursor(null);
                                }
                            } else {
                                click.getEvent().getCursor().setAmount(click.getEvent().getCursor().getAmount() - 1);
                                currentData.addProgress(click.getEvent().getCursor().getType().name(), 1);
                            }
                            if (currentData.isFinished()) {
                                missionData.getMission().finish(resident);
                            }
                            DatabaseManager.getInstance().saveVillage(resident.getVillage());
                            click.getGui().draw();
                        }
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
