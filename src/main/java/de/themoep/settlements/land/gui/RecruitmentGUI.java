package de.themoep.settlements.land.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.StaticGuiElement;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.JoinType;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.ConfigurableGui;
import de.themoep.settlements.utils.Message;
import org.bukkit.entity.Player;

public class RecruitmentGUI extends ConfigurableGui {

    public RecruitmentGUI(Resident res, Settlement landOwner) {
        super(res, landOwner.getKey() + ".recruitment");

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
                                getText("jointype." + joinType.getKey(), "state", landOwner.getStateTag())
                        );
                    }
                    return new StaticGuiElement('j',
                            getIcon("available"),
                            click -> {
                                if (landOwner.hasPermission(resident, "recruitment")) {
                                    landOwner.setJoinType(joinType);
                                    Message.get("command." + landOwner.getKey() + ".recruitment.success").tell(resident, "jointype", joinType.getTag());
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
    }
}
