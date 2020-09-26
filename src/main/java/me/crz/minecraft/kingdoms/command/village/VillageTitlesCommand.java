package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.Config;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.gui.RankSelectGui;
import me.crz.minecraft.kingdoms.village.gui.TitlesGUI;

import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class VillageTitlesCommand extends VillageSubCommand {

    public static final Pattern TITLE_PATTERN = Pattern.compile("[\\w-]+");

    public VillageTitlesCommand(SubCommand parent) {
        super(parent, "titles [gui|list|add <rank> <title>|set <rank> <title>|remove <rank> <title>");
        registerCommand(new VillageSubCommand(this, "gui") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                new RankSelectGui(resident, rank -> new TitlesGUI(resident, rank).open()).open();
                return true;
            }
        });
        registerCommand(new VillageSubCommand(this, "list") {
            @Override
            public boolean run(Resident res, String base, String alias, String... args) {
                getMessage("head").tell(res, "state", res.getVillage().getStateTag());
                for (VillageRank rank : VillageRank.values()) {
                    String defaultTitle = res.getVillage().getDefaultTitle(rank);
                    getMessage("rank").tell(res,
                            "rank", rank.getTag(),
                            "titles", res.getVillage().getTitles(rank).stream()
                                    .map(t -> getText(defaultTitle.equals(t) ? "default-entry" : "entry", "title", t))
                                    .collect(Collectors.joining(getText("separator")))
                    );
                }
                getMessage("footer").tell(res, "state", res.getVillage().getStateTag());
                return true;
            }
        });
        registerCommand(new VillageSubCommand(this, "add <rank> <title>") {
            @Override
            public boolean run(Resident res, String base, String alias, String... args) {
                if (args.length == 2) {
                    VillageRank rank = VillageRank.get(args[0]);
                    if (rank == null) {
                        getError("invalid-rank").tell(res, "rank", args[0]);
                    } else if (rank.above(res.getVillageRank())) {
                        getError("cant-edit-higher").tell(res, "rank", res.getVillageRank().getTag());
                    } else if (res.getVillage().getTitles(rank).size() >= Config.Settlement.TITLES_PER_RANK) {
                        getError("too-many").tell(res,
                                "rank", res.getVillageRank().getTag(),
                                "max", String.valueOf(Config.Settlement.TITLES_PER_RANK)
                        );
                    } else if (args[1].length() > 16) {
                        getError("too-long").tell(res, "input", args[1]);
                    } else if (!TITLE_PATTERN.matcher(args[1]).matches()) {
                        getError("invalid-characters").tell(res, "input", args[1]);
                    } else if (Utils.containsAnyCurses(args[1])) {
                        getError("prohibited-words").tell(res, "input", args[1]);
                    } else if (res.getVillage().addTitle(rank, args[1])) {
                        getMessage("success").tell(res, "title", args[1], "rank", rank.getTag());
                    } else {
                        getError("already-exists").tell(res, "input", args[1], "rank", rank.getTag());
                    }
                    return true;
                }
                return false;
            }
        });
        registerCommand(new VillageSubCommand(this, "set <rank> <title>", "default") {
            @Override
            public boolean run(Resident res, String base, String alias, String... args) {
                if (args.length == 2) {
                    VillageRank rank = VillageRank.get(args[0]);
                    if (rank == null) {
                        getError("invalid-rank").tell(res, "rank", args[0]);
                        return true;
                    } else if (rank.above(res.getVillageRank())) {
                        getError("cant-edit-higher").tell(res, "rank", res.getVillageRank().getTag());
                        return true;
                    }

                    String title = res.getVillage().getTitle(rank, args[1]);
                    if (title != null) {
                        // title exists, change order
                        res.getVillage().setDefaultTitle(rank, title);
                        getMessage("success").tell(res, "title", args[1], "rank", rank.getTag());
                    } else {
                        // title doesn't exist, add it
                        if (res.getVillage().getTitles(rank).size() >= Config.Settlement.TITLES_PER_RANK) {
                            getError("too-many").tell(res,
                                    "rank", res.getVillageRank().getTag(),
                                    "max", String.valueOf(Config.Settlement.TITLES_PER_RANK)
                            );
                        } else if (args[1].length() > 16) {
                            getError("too-long").tell(res, "input", args[1]);
                        } else if (!TITLE_PATTERN.matcher(args[1]).matches()) {
                            getError("invalid-characters").tell(res, "input", args[1]);
                        } else if (Utils.containsAnyCurses(args[1])) {
                            getError("prohibited-words").tell(res, "input", args[1]);
                        } else {
                            res.getVillage().setDefaultTitle(rank, args[1]);
                            getMessage("success-new").tell(res, "title", args[1], "rank", rank.getTag());
                        }
                    }
                    return true;
                }
                return false;
            }
        });
        registerCommand(new VillageSubCommand(this, "remove <rank> <title>", "delete") {
            @Override
            public boolean run(Resident res, String base, String alias, String... args) {
                if (args.length == 2) {
                    VillageRank rank = VillageRank.get(args[0]);
                    if (rank == null) {
                        getError("invalid-rank").tell(res, "rank", args[0]);
                    } else if (rank.above(res.getVillageRank())) {
                        getError("cant-edit-higher").tell(res, "rank", res.getVillageRank().getTag());
                    } else if (res.getVillage().removeTitle(rank, args[1])) {
                        getMessage("success").tell(res, "title", args[1], "rank", rank.getTag());
                        return true;
                    } else {
                        getError("doesnt-exist").tell(res, "input", args[1], "rank", rank.getTag());
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        return false;
    }
}
