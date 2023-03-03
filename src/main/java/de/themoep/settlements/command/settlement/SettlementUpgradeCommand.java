package de.themoep.settlements.command.settlement;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.land.LandownerLevel;
import de.themoep.settlements.land.gui.DepositGUI;
import de.themoep.settlements.land.gui.UpgradeGUI;
import de.themoep.settlements.leveling.LevelInfo;
import de.themoep.settlements.leveling.Levelable;
import de.themoep.settlements.npc.NpcData;
import de.themoep.settlements.npc.NpcType;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.ItemUtils;

import java.util.ArrayList;
import java.util.List;

public class SettlementUpgradeCommand extends SettlementSubCommand {
    public SettlementUpgradeCommand(SubCommand parent) {
        super(parent, "upgrade", "deposit", "materials");
        setVisible(false);
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        new DepositGUI<>(res.getSettlement().getKey(), res, getLevellable(res.getSettlement())).open();
        return true;
    }

    private Levelable<? extends LevelInfo> getLevellable(LandOwner<?, ? extends LandownerLevel> landOwner) {
        Levelable<? extends LevelInfo> selected = null;
        List<Levelable<? extends LevelInfo>> levelables = new ArrayList<>();
        levelables.add(landOwner);

        NpcType.AppliesTo landOwnerType = NpcType.AppliesTo.fromKey(landOwner.getKey());
        for (NpcType npcType : NpcType.values()) {
            if (npcType.appliesTo(landOwnerType)) {
                NpcData data = landOwner.getNpcData(npcType);
                if (data != null && data.isAvailable() && data.hasLevels()) {
                    levelables.add(data);
                }
            }
        }

        for (Levelable<? extends LevelInfo> levelable : levelables) {
            if (!levelable.isMaxLevel()) {
                selected = levelable;
                break;
            }
        }

        return selected;
    }
}
