package me.crz.minecraft.kingdoms.command;

import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.entity.Player;

public abstract class VillageSubCommand extends ResidentSubCommand {
    private VillageRank requiredRank = null;

    public VillageSubCommand(SubCommand parent, String usage, VillageRank requiredRank, String... aliases) {
        this(parent, usage, aliases);
        this.requiredRank = requiredRank;
    }

    public VillageSubCommand(SubCommand parent, String usage, String... aliases) {
        super(parent, usage, aliases);
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
                if (resident.hasVillage()) {
                    MessageUtils.tell(player, getError("free-required"));
                    return false;
                }
                return true;
            } else if (resident.hasVillage()) {
                if (resident.getVillageRank().atLeast(requiredRank)) {
                    return true;
                }
                MessageUtils.tell(player, getError("rank-required"), "rank", requiredRank.getTag(resident.getVillage()));
            } else {
                MessageUtils.tell(player, getError("village-required"));
            }
        }
        return false;
    }

    private boolean testParentRank(Player player) {
        return !(getParent() instanceof VillageSubCommand) || ((VillageSubCommand) getParent()).testRank(player);
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
}
