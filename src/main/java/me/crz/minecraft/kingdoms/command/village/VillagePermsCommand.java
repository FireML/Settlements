package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.gui.PermsGUI;
import me.crz.minecraft.kingdoms.village.gui.RankPermsGUI;
import org.bukkit.ChatColor;

import java.util.Map;
import java.util.Optional;

public class VillagePermsCommand extends VillageSubCommand {
    public VillagePermsCommand(SubCommand parent) {
        super(parent, "perms [<rank>|show|set|unset]", "permissions", "rights");
        registerCommand(new VillageSubCommand(this, "show <rank>", "get", "list") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                if (args.length < 1) {
                    return false;
                }

                VillageRank rank = VillageRank.get(args[0]);
                if (rank == null) {
                    getError("invalid-rank").tell(resident);
                    return false;
                }

                getMessage("head").tell(resident, "rank", rank.getTag(resident.getVillage()));
                for (Map.Entry<String, Optional<Boolean>> e : resident.getVillage().getEffectivePermissions(rank).entrySet()) {
                    getMessage("entry").tell(resident,
                            "rank", rank.name(),
                            "permission", e.getKey(),
                            "value", String.valueOf(e.getValue()),
                            "opposite", String.valueOf(!e.getValue().orElse(false)),
                            "color",  String.valueOf(e.getValue().orElse(false) ? ChatColor.GREEN : ChatColor.RED)
                    );
                }
                getMessage("footer").tell(resident, "rank", rank.getTag(resident.getVillage()));
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

                VillageRank rank = VillageRank.get(args[0]);
                if (rank == null) {
                    getError("invalid-rank").tell(resident);
                    return false;
                }

                String permission = args[1].toLowerCase();
                if (!permission.startsWith("village.") && !permission.startsWith("empire.")) {
                    permission = "village." + permission;
                }

                if (rank != VillageRank.getChief() && rank.atLeast(resident.getVillageRank())) {
                    getError("rank-required").tell(resident, "rank", rank.next().getTag(resident.getVillage()));
                    return true;
                }
                if (rank == VillageRank.getChief()) {
                    if (resident.getVillageRank() != rank) {
                        getError("rank-required").tell(resident, "rank", rank.getTag(resident.getVillage()));
                        return true;
                    }
                    if (permission.startsWith("village.perms")) {
                        getError("cant-edit-perms-permission");
                        return true;
                    }
                }

                if (!resident.hasPermission("kingdoms.command." + permission)) {
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
                resident.getVillage().setPermission(rank, permission, value);
                Utils.runAsync(() -> DatabaseManager.getInstance().save(resident.getVillage()));
                getMessage("success").tell(resident,
                        "rank", rank.getTag(resident.getVillage()),
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
                VillageRank rank = VillageRank.get(args[0]);
                if (rank == null) {
                    getError("invalid-rank").tell(resident);
                    return false;
                }
                String permission = args[1].toLowerCase();
                if (!permission.startsWith("village.") && !permission.startsWith("empire.")) {
                    permission = "village." + permission;
                }

                if (rank != VillageRank.getChief() && rank.atLeast(resident.getVillageRank())) {
                    getError("rank-required").tell(resident, "rank", rank.next().getTag(resident.getVillage()));
                    return true;
                }
                if (rank == VillageRank.getChief()) {
                    if (resident.getVillageRank() != rank) {
                        getError("rank-required").tell(resident, "rank", rank.getTag(resident.getVillage()));
                        return true;
                    }
                    if (permission.startsWith("village.perms")) {
                        getError("cant-edit-perms-permission");
                        return true;
                    }
                }

                if (!resident.hasPermission("kingdoms.command." + permission)) {
                    getError("cant-edit-permission").tell(resident, "permission", permission);
                    return true;
                }

                resident.getVillage().setDefaultPermission(rank, permission);
                Optional<Boolean> value = resident.getVillage().getEffectivePermissionValue(rank, permission);
                Utils.runAsync(() -> DatabaseManager.getInstance().save(resident.getVillage()));
                getMessage("success").tell(resident,
                        "rank", rank.getTag(resident.getVillage()),
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
            VillageRank rank = VillageRank.get(args[0]);
            if (rank == null) {
                getError("invalid-rank").tell(resident);
                return false;
            }
            RankPermsGUI.open(resident, rank);
            return true;
        }
        return false;
    }
}
