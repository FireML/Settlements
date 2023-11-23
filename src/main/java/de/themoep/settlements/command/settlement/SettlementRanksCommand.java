package de.themoep.settlements.command.settlement;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.land.gui.RankPermsGUI;
import de.themoep.settlements.rank.SettlementRank;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.Utils;

import java.util.Map;
import java.util.Optional;

public class SettlementRanksCommand extends SettlementSubCommand {
    public SettlementRanksCommand(SubCommand parent) {
        super(parent, "ranks [<rank>|show|set|unset]", "rank", "");
        registerCommand(new SettlementSubCommand(this, "show <rank>", "get", "list") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                if (args.length < 1) {
                    return false;
                }

                SettlementRank rank = SettlementRank.get(args[0]);
                if (rank == null) {
                    getError("invalid-rank").tell(resident);
                    return false;
                }

                getMessage("head").tell(resident, "rank", rank.getTag(resident.getSettlement()));
                for (Map.Entry<String, Optional<Boolean>> e : resident.getSettlement().getEffectivePermissions(rank).entrySet()) {
                    getMessage("entry").tell(resident,
                            "rank", rank.name(),
                            "permission", e.getKey(),
                            "value", String.valueOf(e.getValue().orElse(false)),
                            "opposite", String.valueOf(!e.getValue().orElse(false)),
                            "color",  String.valueOf(e.getValue().orElse(false) ? "§a" : "§c")
                    );
                }
                getMessage("footer").tell(resident, "rank", rank.getTag(resident.getSettlement()));
                //TODO: Pagination for permissions? Inherited values?
                return true;
            }
        });
        registerCommand(new SettlementSubCommand(this, "set <rank> <permission> [true|false]", "give", "add", "grant") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                if (args.length < 2) {
                    return false;
                }

                SettlementRank rank = SettlementRank.get(args[0]);
                if (rank == null) {
                    getError("invalid-rank").tell(resident);
                    return false;
                }

                String permission = args[1].toLowerCase();
                if (permission.startsWith("group.") || permission.startsWith("settlement.group.")) {
                    String group = permission.split("group\\.")[1];
                    if (SettlementRank.getPermGroup(group) == null) {
                        getError("unknown-permission-group").tell(resident, "input", permission);
                        return true;
                    }
                    permission = "settlement.group." + group;
                } else if (!permission.startsWith("settlement.") && !permission.startsWith("empire.")) {
                    if (SettlementRank.getPermGroup(permission) == null) {
                        permission = "settlement." + permission;
                    } else {
                        permission = "settlement.group." + permission;
                    }
                }

                if (rank != SettlementRank.getChief() && rank.atLeast(resident.getSettlementRank())) {
                    getError("rank-required").tell(resident, "rank", rank.next().getTag(resident.getSettlement()));
                    return true;
                }
                if (rank == SettlementRank.getChief()) {
                    if (resident.getSettlementRank() != rank) {
                        getError("rank-required").tell(resident, "rank", rank.getTag(resident.getSettlement()));
                        return true;
                    }
                    if (permission.startsWith("settlement.ranks")) {
                        getError("cant-edit-perms-permission");
                        return true;
                    }
                }

                if (!resident.hasPermission(permission) || !SettlementRank.isToggleablePermission(permission)) {
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
                if (resident.getSettlement().setPermission(rank, permission, value)) {
                    resident.getSettlement().save();
                }
                getMessage("success").tell(resident,
                        "rank", rank.getTag(resident.getSettlement()),
                        "permission", permission,
                        "value", String.valueOf(value)
                );
                return true;
            }
        });
        registerCommand(new SettlementSubCommand(this, "unset <rank> [<permission>]", "remove", "default", "reset") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                if (args.length < 2) {
                    return false;
                }
                SettlementRank rank = SettlementRank.get(args[0]);
                if (rank == null) {
                    getError("invalid-rank").tell(resident);
                    return false;
                }
                String permission = args[1].toLowerCase();
                if (!permission.startsWith("settlement.") && !permission.startsWith("empire.")) {
                    permission = "settlement." + permission;
                }

                if (rank != SettlementRank.getChief() && rank.atLeast(resident.getSettlementRank())) {
                    getError("rank-required").tell(resident, "rank", rank.next().getTag(resident.getSettlement()));
                    return true;
                }
                if (rank == SettlementRank.getChief()) {
                    if (resident.getSettlementRank() != rank) {
                        getError("rank-required").tell(resident, "rank", rank.getTag(resident.getSettlement()));
                        return true;
                    }
                    if (permission.startsWith("settlement.ranks")) {
                        getError("cant-edit-perms-permission");
                        return true;
                    }
                }

                if (!resident.hasPermission("settlements.command." + permission) || !SettlementRank.isToggleablePermission(permission)) {
                    getError("cant-edit-permission").tell(resident, "permission", permission);
                    return true;
                }

                if (resident.getSettlement().setDefaultPermission(rank, permission)) {
                    resident.getSettlement().save();
                }

                Optional<Boolean> value = resident.getSettlement().getEffectivePermissionValue(rank, permission);
                getMessage("success").tell(resident,
                        "rank", rank.getTag(resident.getSettlement()),
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
            new RankPermsGUI<>("settlement", resident, resident.getSettlement()).open();
            return true;
        }

        SettlementRank rank = SettlementRank.get(args[0]);
        if (rank == null) {
            getError("invalid-rank").tell(resident, "rank", args[0]);
        }
        return false;
    }
}
