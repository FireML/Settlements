package de.themoep.settlements.command.village;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.village.gui.RanksGUI;
import de.themoep.settlements.village.gui.RankPermGroupsGUI;
import de.themoep.settlements.village.gui.RankPermsGUI;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.rank.PermissionGroup;
import de.themoep.settlements.rank.SettlementRank;
import de.themoep.settlements.utils.Utils;
import org.bukkit.ChatColor;

import java.util.Map;
import java.util.Optional;

public class VillageRanksCommand extends VillageSubCommand {
    public VillageRanksCommand(SubCommand parent) {
        super(parent, "ranks [<rank>|show|set|unset]", "rank", "");
        registerCommand(new VillageSubCommand(this, "show <rank>", "get", "list") {
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
                            "color",  String.valueOf(e.getValue().orElse(false) ? ChatColor.GREEN : ChatColor.RED)
                    );
                }
                getMessage("footer").tell(resident, "rank", rank.getTag(resident.getSettlement()));
                //TODO: Pagination for permissions? Inherited values?
                return true;
            }
        });
        registerCommand(new VillageSubCommand(this, "set <rank> <permission> [true|false]", "give", "add", "grant") {
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

                boolean value = true;
                if (args.length > 2) {
                    try {
                        value = Utils.parseBoolean(args[2]);
                    } catch (IllegalArgumentException e) {
                        return false;
                    }
                }
                resident.getSettlement().setPermission(rank, permission, value);
                Utils.runAsync(() -> DatabaseManager.getInstance().save(resident.getSettlement()));
                getMessage("success").tell(resident,
                        "rank", rank.getTag(resident.getSettlement()),
                        "permission", permission,
                        "value", String.valueOf(value)
                );
                return true;
            }
        });
        registerCommand(new VillageSubCommand(this, "unset <rank> [<permission>]", "remove", "default", "reset") {
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

                resident.getSettlement().setDefaultPermission(rank, permission);
                Optional<Boolean> value = resident.getSettlement().getEffectivePermissionValue(rank, permission);
                Utils.runAsync(() -> DatabaseManager.getInstance().save(resident.getSettlement()));
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
            new RanksGUI(resident.getPlayer()).open();
            return true;
        }

        SettlementRank rank = SettlementRank.get(args[0]);
        if (rank == null) {
            getError("invalid-rank").tell(resident, "rank", args[0]);
            return false;
        }
        if (args.length == 1) {
            new RankPermGroupsGUI(resident, rank).open();
            return true;
        } else if (args.length == 2) {
            PermissionGroup group = SettlementRank.getPermGroup(args[1]);
            if (group != null) {
                new RankPermsGUI(resident, rank, group).open();
                return true;
            }
            getError("invalid-group").tell(resident, "group", args[1]);
        }
        return false;
    }
}
