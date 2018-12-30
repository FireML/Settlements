package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.ResidentSubCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.Resident;

public abstract class VillageSubCommand extends ResidentSubCommand {
    private VillageRank requiredRank = null;

    public VillageSubCommand(SubCommand parent, String usage, VillageRank requiredRank, String... aliases) {
        this(parent, usage, aliases);
        this.requiredRank = requiredRank;
    }

    public VillageSubCommand(SubCommand parent, String usage, String... aliases) {
        super(parent, usage, aliases);
    }

    private boolean testRank(Resident resident) {
        if (!testParentRank(resident)) {
            return false;
        }
        if (resident != null) {
            if (requiredRank == null) {
                if (resident.hasVillage()) {
                    getError("free-required").tell(resident);
                    return false;
                }
                return true;
            } else if (resident.hasVillage()) {
                if (resident.getVillage().getId().isNull()) {
                    getError("missing-village-id").tell(resident);
                    return true;
                }
                if (resident.getVillageRank().atLeast(requiredRank)) {
                    return true;
                }
                getError("rank-required").tell(resident, "rank", requiredRank.getTag(resident.getVillage()));
            } else {
                getError("village-required").tell(resident);
            }
        }
        return false;
    }

    private boolean testParentRank(Resident resident) {
        return !(getParent() instanceof VillageSubCommand) || ((VillageSubCommand) getParent()).testRank(resident);
    }

    private boolean hasRank(Resident resident) {
        if (resident != null) {
            if (requiredRank == null) {
                return !resident.hasVillage();
            } else if (resident.hasVillage()) {
                return resident.getVillageRank().atLeast(requiredRank);
            }
        }
        return false;
    }

    public void setRequiredRank(VillageRank rank) {
        this.requiredRank = rank;
    }

    public VillageRank getRequiredRank() {
        return requiredRank;
    }

    public void registerCommand(VillageSubCommand subCommand) {
        if (subCommand.getParent() instanceof VillageSubCommand) {
            if (((VillageSubCommand) subCommand.getParent()).getRequiredRank() != null
                    && ((VillageSubCommand) subCommand.getParent()).getRequiredRank().above(subCommand.getRequiredRank())) {
                subCommand.setRequiredRank(((VillageSubCommand) subCommand.getParent()).getRequiredRank());
            } else if (((VillageSubCommand) subCommand.getParent()).getRequiredRank() == null) {
                subCommand.setRequiredRank(null);
            }
        }
        super.registerCommand(subCommand);
    }

    public int ordinal() {
        return requiredRank != null ? requiredRank.ordinal() : super.ordinal();
    }
}
