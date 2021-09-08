package de.themoep.settlements.command.empire;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.empire.gui.PermsGUI;
import de.themoep.settlements.empire.gui.RankPermGroupsGUI;
import de.themoep.settlements.empire.gui.RankPermsGUI;
import de.themoep.settlements.rank.EmpireRank;
import de.themoep.settlements.rank.PermissionGroup;
import de.themoep.settlements.utils.Utils;
import org.bukkit.ChatColor;

import java.util.Map;
import java.util.Optional;

public class EmpirePermsCommand extends EmpireSubCommand {
    public EmpirePermsCommand(SubCommand parent) {
        super(parent, "perms [<rank>|show|set|unset]", "permissions", "rights");
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
                for (Map.Entry<String, Optional<Boolean>> e : resident.getVillage().getEmpire().getEffectivePermissions(rank).entrySet()) {
                    getMessage("entry").tell(resident,
                            "rank", rank.name(),
                            "permission", e.getKey(),
                            "value", String.valueOf(e.getValue().orElse(false)),
                            "opposite", String.valueOf(!e.getValue().orElse(false)),
                            "color",  String.valueOf(e.getValue().orElse(false) ? ChatColor.GREEN : ChatColor.RED)
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

                if (!resident.hasPermission("kingdoms.command." + permission) || !EmpireRank.isToggleablePermission(permission)) {
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
                resident.getVillage().getEmpire().setPermission(rank, permission, value);
                Utils.runAsync(() -> DatabaseManager.getInstance().save(resident.getVillage().getEmpire()));
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

                if (!resident.hasPermission("kingdoms.command." + permission) || !EmpireRank.isToggleablePermission(permission)) {
                    getError("cant-edit-permission").tell(resident, "permission", permission);
                    return true;
                }

                resident.getVillage().getEmpire().setDefaultPermission(rank, permission);
                Optional<Boolean> value = resident.getVillage().getEmpire().getEffectivePermissionValue(rank, permission);
                Utils.runAsync(() -> DatabaseManager.getInstance().save(resident.getVillage().getEmpire()));
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
            new PermsGUI(resident.getPlayer()).open();
            return true;
        }

        EmpireRank rank = EmpireRank.get(args[0]);
        if (rank == null) {
            getError("invalid-rank").tell(resident, "rank", args[0]);
            return false;
        }
        if (args.length == 1) {
            new RankPermGroupsGUI(resident, rank).open();
            return true;
        } else if (args.length == 2) {
            PermissionGroup group = EmpireRank.getPermGroup(args[1]);
            if (group != null) {
                new RankPermsGUI(resident, rank, group).open();
                return true;
            }
            getError("invalid-group").tell(resident, "group", args[1]);
        }
        return false;
    }
}
