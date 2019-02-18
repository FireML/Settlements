package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.empire.gui.PermsGUI;
import me.crz.minecraft.kingdoms.empire.gui.RankPermsGUI;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.rank.RankManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
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
                            "value", String.valueOf(e.getValue()),
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

                if (!resident.hasPermission("kingdoms.command." + permission) || !EmpireRank.getToggleablePermissions().contains(permission.toLowerCase())) {
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

                if (!resident.hasPermission("kingdoms.command." + permission) || !EmpireRank.getToggleablePermissions().contains(permission.toLowerCase())) {
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
            PermsGUI.getInstance().open(resident.getPlayer());
            return true;
        } else if (args.length == 1) {
            EmpireRank rank = EmpireRank.get(args[0]);
            if (rank == null) {
                getError("invalid-rank").tell(resident);
                return false;
            }
            RankPermsGUI.getInstance(rank).open(resident.getPlayer());
            return true;
        }
        return false;
    }
}
