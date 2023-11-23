package de.themoep.settlements.command.empire;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.land.gui.RankPermsGUI;
import de.themoep.settlements.rank.EmpireRank;
import de.themoep.settlements.rank.SettlementRank;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.Utils;

import java.util.Map;
import java.util.Optional;

public class EmpireRanksCommand extends EmpireSubCommand {
    public EmpireRanksCommand(SubCommand parent) {
        super(parent, "ranks [<rank>|show|set|unset]", "rank");
        registerCommand(new EmpireSubCommand(this, "show <rank>", "get", "list") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                if (args.length < 1) {
                    return false;
                }

                EmpireRank rank = EmpireRank.get(args[0]);
                if (rank == null) {
                    getError("invalid-rank").tell(resident);
                    return false;
                }

                getMessage("head").tell(resident, "rank", rank.getTag());
                for (Map.Entry<String, Optional<Boolean>> e : resident.getSettlement().getEmpire().getEffectivePermissions(rank).entrySet()) {
                    getMessage("entry").tell(resident,
                            "rank", rank.name(),
                            "permission", e.getKey(),
                            "value", String.valueOf(e.getValue().orElse(false)),
                            "opposite", String.valueOf(!e.getValue().orElse(false)),
                            "color", e.getValue().orElse(false) ? "§a" : "§c"
                    );
                }
                getMessage("footer").tell(resident, "rank", rank.getTag());
                //TODO: Pagination for permissions? Inherited values?
                return true;
            }
        });
        registerCommand(new EmpireSubCommand(this, "set <rank> <permission> [true|false]", "give", "add", "grant") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                if (args.length < 2) {
                    return false;
                }

                EmpireRank rank = EmpireRank.get(args[0]);
                if (rank == null) {
                    getError("invalid-rank").tell(resident);
                    return false;
                }

                String permission = args[1].toLowerCase();
                if (!permission.startsWith("empire.")) {
                    permission = "empire." + permission;
                }

                if (rank != EmpireRank.getLeader() && rank.atLeast(resident.getEmpireRank())) {
                    getError("rank-required").tell(resident, "rank", rank.next().getTag());
                    return true;
                }
                if (rank == EmpireRank.getLeader()) {
                    if (resident.getEmpireRank() != rank) {
                        getError("rank-required").tell(resident, "rank", rank.getTag());
                        return true;
                    }
                    if (permission.startsWith("empire.perms")) {
                        getError("cant-edit-perms-permission");
                        return true;
                    }
                }

                if (!resident.hasPermission("settlements.command." + permission) || !EmpireRank.isToggleablePermission(permission)) {
                    getError("cant-edit-permission").tell(resident, "permission", permission);
                    return true;
                }

                boolean value = true;
                if (args.length > 2) {
                    try {
                        value = Utils.parseBoolean(args[2]);
                    } catch (IllegalArgumentException e) {
                        return false;
                    }
                }
                if (resident.getSettlement().getEmpire().setPermission(rank, permission, value)) {
                    resident.getSettlement().getEmpire().save();
                }
                getMessage("success").tell(resident,
                        "rank", rank.getTag(),
                        "permission", permission,
                        "value", String.valueOf(value)
                );
                return true;
            }
        });
        registerCommand(new EmpireSubCommand(this, "unset <rank> [<permission>]", "default", "reset") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                if (args.length < 2) {
                    return false;
                }

                EmpireRank rank = EmpireRank.get(args[0]);
                if (rank == null) {
                    getError("invalid-rank").tell(resident);
                    return false;
                }
                String permission = args[1].toLowerCase();
                if (permission.startsWith("group.") || permission.startsWith("empire.group.")) {
                    String group = permission.split("group\\.")[1];
                    if (SettlementRank.getPermGroup(group) == null) {
                        getError("unknown-permission-group").tell(resident, "input", permission);
                        return true;
                    }
                    permission = "empire.group." + group;
                } else if (!permission.startsWith("empire.")) {
                    if (SettlementRank.getPermGroup(permission) == null) {
                        permission = "empire." + permission;
                    } else {
                        permission = "empire.group." + permission;
                    }
                }

                if (rank != EmpireRank.getLeader() && rank.atLeast(resident.getEmpireRank())) {
                    getError("rank-required").tell(resident, "rank", rank.next().getTag());
                    return true;
                }
                if (rank == EmpireRank.getLeader()) {
                    if (resident.getEmpireRank() != rank) {
                        getError("rank-required").tell(resident, "rank", rank.getTag());
                        return true;
                    }
                    if (permission.startsWith("empire.perms")) {
                        getError("cant-edit-perms-permission");
                        return true;
                    }
                }

                if (!resident.hasPermission(permission) || !EmpireRank.isToggleablePermission(permission)) {
                    getError("cant-edit-permission").tell(resident, "permission", permission);
                    return true;
                }

                if (resident.getSettlement().getEmpire().setDefaultPermission(rank, permission)) {
                    resident.getSettlement().getEmpire().save();
                }
                Optional<Boolean> value = resident.getSettlement().getEmpire().getEffectivePermissionValue(rank, permission);
                getMessage("success").tell(resident,
                        "rank", rank.getTag(),
                        "permission", permission,
                        "value", String.valueOf(value.isPresent() && value.get())
                );
                return true;
            }
        });
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length == 0) {
            new RankPermsGUI<>("empire", resident, resident.getSettlement().getEmpire()).open();
            return true;
        }

        EmpireRank rank = EmpireRank.get(args[0]);
        if (rank == null) {
            getError("invalid-rank").tell(resident, "rank", args[0]);
        }
        return false;
    }
}
