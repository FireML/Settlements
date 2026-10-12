package de.themoep.settlements.command.settlement;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.land.LandownerLevel;
import de.themoep.settlements.land.gui.DepositGUI;
import de.themoep.settlements.leveling.LevelInfo;
import de.themoep.settlements.leveling.Levelable;
import de.themoep.settlements.resident.Resident;

import java.util.ArrayList;
import java.util.List;

public class SettlementDepositCommand extends SettlementSubCommand {
    public SettlementDepositCommand(SubCommand parent) {
        super(parent, "deposit");
        setVisible(false);
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        new DepositGUI<>(res.getSettlement().getKey(), res, getLevellable(res.getSettlement())).open();
        return true;
    }

    public static Levelable<? extends LevelInfo> getLevellable(LandOwner<?, ? extends LandownerLevel> landOwner) {
        Levelable<? extends LevelInfo> selected = null;
        List<Levelable<? extends LevelInfo>> levelables = new ArrayList<>();
        levelables.add(landOwner);

        for (Levelable<? extends LevelInfo> levelable : levelables) {
            if (!levelable.isMaxLevel()) {
                selected = levelable;
                break;
            }
        }

        return selected;
    }
}
