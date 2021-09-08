package de.themoep.settlements.command.empire;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.rank.EmpireRank;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.empire.gui.RankSelectGUI;
import de.themoep.settlements.empire.gui.TitlesGUI;

import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EmpireTitlesCommand extends EmpireSubCommand {

    public static final Pattern TITLE_PATTERN = Pattern.compile("[\\w-]+");

    public EmpireTitlesCommand(SubCommand parent) {
        super(parent, "titles [gui|list|add <rank> <title>|set <rank> <title>|remove <rank> <title>");
        registerCommand(new EmpireSubCommand(this, "gui") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                new RankSelectGUI(resident, rank -> new TitlesGUI(resident, rank).open()).open();
                return true;
            }
        });
        registerCommand(new EmpireSubCommand(this, "list") {
            @Override
            public boolean run(Resident res, String base, String alias, String... args) {
                getMessage("head").tell(res);
                for (EmpireRank rank : EmpireRank.values()) {
                    String defaultTitle = res.getVillage().getEmpire().getDefaultTitle(rank);
                    getMessage("rank").tell(res,
                            "rank", rank.getTag(),
                            "titles", res.getVillage().getEmpire().getTitles(rank).stream()
                                    .map(t -> getText(defaultTitle.equals(t) ? "default-entry" : "entry", "title", t))
                                    .collect(Collectors.joining(getText("separator")))
                    );
                }
                getMessage("footer").tell(res);
                return true;
            }
        });
        registerCommand(new EmpireSubCommand(this, "add <rank> <title>") {
            @Override
            public boolean run(Resident res, String base, String alias, String... args) {
                if (args.length == 2) {
                    EmpireRank rank = EmpireRank.get(args[0]);
                    if (rank == null) {
                        getError("invalid-rank").tell(res, "rank", args[0]);
                    } else if (rank.above(res.getEmpireRank())) {
                        getError("cant-edit-higher").tell(res, "rank", res.getEmpireRank().getTag());
                    } else if (res.getVillage().getEmpire().getTitles(rank).size() >= Config.Empire.TITLES_PER_RANK) {
                        getError("too-many").tell(res,
                                "rank", res.getEmpireRank().getTag(),
                                "max", String.valueOf(Config.Empire.TITLES_PER_RANK)
                        );
                    } else if (args[1].length() > 16) {
                        getError("too-long").tell(res, "input", args[1]);
                    } else if (!TITLE_PATTERN.matcher(args[1]).matches()) {
                        getError("invalid-characters").tell(res, "input", args[1]);
                    } else if (Utils.containsAnyCurses(args[1])) {
                        getError("prohibited-words").tell(res, "input", args[1]);
                    } else if (res.getVillage().getEmpire().addTitle(rank, args[1])) {
                        getMessage("success").tell(res, "title", args[1], "rank", rank.getTag());
                    } else {
                        getError("already-exists").tell(res, "input", args[1], "rank", rank.getTag());
                    }
                    return true;
                }
                return false;
            }
        });
        registerCommand(new EmpireSubCommand(this, "set <rank> <title>", "default") {
            @Override
            public boolean run(Resident res, String base, String alias, String... args) {
                if (args.length == 2) {
                    EmpireRank rank = EmpireRank.get(args[0]);
                    if (rank == null) {
                        getError("invalid-rank").tell(res, "rank", args[0]);
                        return true;
                    } else if (rank.above(res.getEmpireRank())) {
                        getError("cant-edit-higher").tell(res, "rank", res.getEmpireRank().getTag());
                        return true;
                    }

                    String title = res.getVillage().getEmpire().getTitle(rank, args[1]);
                    if (title != null) {
                        // title exists, change order
                        res.getVillage().getEmpire().setDefaultTitle(rank, title);
                        getMessage("success").tell(res, "title", args[1], "rank", rank.getTag());
                    } else {
                        // title doesn't exist, add it
                        if (res.getVillage().getEmpire().getTitles(rank).size() >= Config.Empire.TITLES_PER_RANK) {
                            getError("too-many").tell(res,
                                    "rank", res.getEmpireRank().getTag(),
                                    "max", String.valueOf(Config.Empire.TITLES_PER_RANK)
                            );
                        } else if (args[1].length() > 16) {
                            getError("too-long").tell(res, "input", args[1]);
                        } else if (!TITLE_PATTERN.matcher(args[1]).matches()) {
                            getError("invalid-characters").tell(res, "input", args[1]);
                        } else if (Utils.containsAnyCurses(args[1])) {
                            getError("prohibited-words").tell(res, "input", args[1]);
                        } else {
                            res.getVillage().getEmpire().setDefaultTitle(rank, args[1]);
                            getMessage("success-new").tell(res, "title", args[1], "rank", rank.getTag());
                        }
                    }
                    return true;
                }
                return false;
            }
        });
        registerCommand(new EmpireSubCommand(this, "remove <rank> <title>", "delete") {
            @Override
            public boolean run(Resident res, String base, String alias, String... args) {
                if (args.length == 2) {
                    EmpireRank rank = EmpireRank.get(args[0]);
                    if (rank == null) {
                        getError("invalid-rank").tell(res, "rank", args[0]);
                    } else if (rank.above(res.getEmpireRank())) {
                        getError("cant-edit-higher").tell(res, "rank", res.getEmpireRank().getTag());
                    } else if (res.getVillage().getEmpire().removeTitle(rank, args[1])) {
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
