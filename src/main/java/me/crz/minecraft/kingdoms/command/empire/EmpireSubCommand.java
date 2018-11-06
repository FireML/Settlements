package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.village.VillageSubCommand;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.entity.Player;

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
    public boolean testAccess(Player player) {
        return testRank(player) && super.testAccess(player);
    }

    private boolean testRank(Player player) {
        if (!testParentRank(player)) {
            return false;
        }
        Resident resident = ResidentManager.getInstance().getResident(player);
        if (resident != null) {
            if (requiredRank == null) {
                if (resident.hasEmpire()) {
                    MessageUtils.tell(player, getError("no-empire-required"));
                    return false;
                }
                return true;
            } else if (resident.hasEmpire()) {
                if (resident.getEmpireRank().atLeast(requiredRank)) {
                    return true;
                }
                MessageUtils.tell(player, getError("rank-required"), "rank", requiredRank.getTag());
            } else {
                MessageUtils.tell(player, getError("empire-required"));
            }
        }
        return false;
    }

    private boolean testParentRank(Player player) {
        return !(getParent() instanceof EmpireSubCommand) || ((EmpireSubCommand) getParent()).testRank(player);
    }

    @Override
    public boolean hasAccess(Player player) {
        return super.hasAccess(player) && hasRank(player);
    }

    private boolean hasRank(Player player) {
        Resident resident = ResidentManager.getInstance().getResident(player);
        if (resident != null) {
            if (requiredRank == null) {
                return !resident.hasVillage();
            } else if (resident.hasVillage()) {
                return resident.getVillageRank().atLeast(requiredRank);
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
