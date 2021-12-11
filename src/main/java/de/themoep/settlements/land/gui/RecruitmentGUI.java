package de.themoep.settlements.land.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.LandAccess;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.JoinType;
import de.themoep.settlements.utils.Message;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

public class RecruitmentGUI extends SettlementsGUI {
    private final InventoryGui gui;

    public RecruitmentGUI(@NonNull String type, LandOwner<?, ?> landOwner) {
        super(type + ".recruitment");
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title"), getSetup());
        gui.setCloseAction(close -> false);
        gui.setFiller(getIcon("filler"));

        GuiElementGroup group = new GuiElementGroup('j');
        group.setAlignment(getAlignment("j"));

        for (JoinType joinType : JoinType.values()) {
            group.addElement(new DynamicGuiElement('j', (h) -> {
                Resident resident = ResidentManager.getInstance().getResident((Player) h);
                if (landOwner.isMember(resident)) {
                    if (landOwner.getJoinType() == joinType) {
                        return new StaticGuiElement('j',
                                getIcon("selected"),
                                getText("selected", "jointype", joinType.getTag()),
                                getText("access." + joinType.getKey(), "state", landOwner.getStateTag())
                        );
                    }
                    return new StaticGuiElement('j',
                            getIcon("available"),
                            click -> {
                                if (landOwner.hasPermission(resident, type + ".recruitment")) {
                                    landOwner.setJoinType(joinType);
                                    Message.get("command." + type + ".recruitment.success").tell(resident, "jointype", joinType.getTag());
                                    landOwner.save();
                                    gui.draw();
                                }
                                return true;
                            },
                            getText("available", "jointype", joinType.getTag()),
                            getText("jointype." + joinType.getKey(), "state", landOwner.getStateTag())
                    );
                }
                return null;
            }));
        }

        gui.addElement(group);

        gui.addElement(new GuiBackElement('n', getIcon("back"), true, getText("back")));
    }

    public void open(final Player p) {
        gui.show(p);
    }
}
