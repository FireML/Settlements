package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.village.VillageSubCommand;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.Resident;

public abstract class EmpireSubCommand extends VillageSubCommand {
    private EmpireRank requiredRank = null;

    public EmpireSubCommand(SubCommand parent, String usage, EmpireRank requiredRank, VillageRank requiredVillageRank, String... aliases) {
        super(parent, usage, requiredVillageRank, aliases);
        this.requiredRank = requiredRank;
    }

    public EmpireSubCommand(SubCommand parent, String usage, VillageRank requiredRank, String... aliases) {
        this(parent, usage, null, requiredRank, aliases);
    }

    public EmpireSubCommand(SubCommand parent, String usage, EmpireRank requiredRank, String... aliases) {
        this(parent, usage, requiredRank, VillageRank.RESIDENT, aliases);
    }

    public EmpireSubCommand(SubCommand parent, String usage, String... aliases) {
        this(parent, usage, VillageRank.RESIDENT, aliases);
    }

    @Override
    public boolean testAccess(Resident resident) {
        if (!resident.hasEmpire()) {
            MessageUtils.tell(resident, getError("empire-required"));
            return false;
        }
        return super.testAccess(resident);
    }

    @Override
    public boolean hasAccess(Resident resident) {
        return resident.hasEmpire() && super.hasAccess(resident);
    }

    private boolean testRank(Resident resident) {
        if (!testParentRank(resident)) {
            return false;
        }
        if (resident != null) {
            if (requiredRank == null) {
                if (resident.hasEmpire()) {
                    MessageUtils.tell(resident, getError("no-empire-required"));
                    return false;
                }
                return true;
            } else if (resident.hasEmpire()) {
                if (resident.getEmpireRank().atLeast(requiredRank)) {
                    return true;
                }
                MessageUtils.tell(resident, getError("rank-required"), "rank", requiredRank.getTag());
            } else {
                MessageUtils.tell(resident, getError("empire-required"));
            }
        }
        return false;
    }

    private boolean testParentRank(Resident resident) {
        return !(getParent() instanceof EmpireSubCommand) || ((EmpireSubCommand) getParent()).testRank(resident);
    }

    private boolean hasRank(Resident resident) {
        if (resident != null) {
            if (requiredRank == null) {
                return !resident.hasEmpire();
            } else if (resident.hasEmpire()) {
                return resident.getEmpireRank().atLeast(requiredRank);
            }
        }
        return false;
    }

    public void setRequiredEmpireRank(EmpireRank rank) {
        this.requiredRank = rank;
    }

    public EmpireRank getRequiredEmpireRank() {
        return requiredRank;
    }

    public void registerCommand(EmpireSubCommand subCommand) {
        if (subCommand.getParent() instanceof EmpireSubCommand) {
            if (((EmpireSubCommand) subCommand.getParent()).getRequiredEmpireRank() != null
                    && ((EmpireSubCommand) subCommand.getParent()).getRequiredEmpireRank().above(subCommand.getRequiredEmpireRank())) {
                subCommand.setRequiredEmpireRank(((EmpireSubCommand) subCommand.getParent()).getRequiredEmpireRank());
            } else if (((EmpireSubCommand) subCommand.getParent()).getRequiredEmpireRank() == null) {
                subCommand.setRequiredEmpireRank(null);
            }
        }
        super.registerCommand(subCommand);
    }

    public int ordinal() {
        return requiredRank != null ? requiredRank.ordinal() : super.ordinal();
    }
}
