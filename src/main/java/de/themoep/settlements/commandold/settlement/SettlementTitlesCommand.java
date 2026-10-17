package de.themoep.settlements.commandold.settlement;

import de.themoep.settlements.commandold.SubCommand;
import de.themoep.settlements.land.gui.RankSelectGUI;
import de.themoep.settlements.land.gui.TitlesGUI;
import de.themoep.settlements.rank.SettlementRank;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.config.Config;

import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SettlementTitlesCommand extends SettlementSubCommand {

    public static final Pattern TITLE_PATTERN = Pattern.compile("[\\w-]+");

    public SettlementTitlesCommand(SubCommand parent) {
        super(parent, "titles [gui|list|add <rank> <title>|set <rank> <title>|remove <rank> <title>");
        registerCommand(new SettlementSubCommand(this, "gui") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                new RankSelectGUI("settlement.titles", resident, resident, resident.getSettlement(), rank -> new TitlesGUI("settlement", resident, resident.getSettlement(), rank).open()).open();
                return true;
            }
        });
        registerCommand(new SettlementSubCommand(this, "list") {
            @Override
            public boolean run(Resident res, String base, String alias, String... args) {
                getMessage("head").tell(res, "state", res.getSettlement().getStateTag());
                for (SettlementRank rank : SettlementRank.values()) {
                    String defaultTitle = res.getSettlement().getDefaultTitle(rank);
                    getMessage("rank").tell(res,
                            "rank", rank.getTag(),
                            "titles", res.getSettlement().getTitles(rank).stream()
                                    .map(t -> getText(defaultTitle.equals(t) ? "default-entry" : "entry", "title", t))
                                    .collect(Collectors.joining(getText("separator")))
                    );
                }
                getMessage("footer").tell(res, "state", res.getSettlement().getStateTag());
                return true;
            }
        });
        registerCommand(new SettlementSubCommand(this, "add <rank> <title>") {
            @Override
            public boolean run(Resident res, String base, String alias, String... args) {
                if (args.length == 2) {
                    SettlementRank rank = SettlementRank.get(args[0]);
                    if (rank == null) {
                        getError("invalid-rank").tell(res, "rank", args[0]);
                    } else if (rank.above(res.getSettlementRank())) {
                        getError("cant-edit-higher").tell(res, "rank", res.getSettlementRank().getTag());
                    } else if (res.getSettlement().getTitles(rank).size() >= Config.Settlement.TITLES_PER_RANK) {
                        getError("too-many").tell(res,
                                "rank", res.getSettlementRank().getTag(),
                                "max", String.valueOf(Config.Settlement.TITLES_PER_RANK)
                        );
                    } else if (args[1].length() > 16) {
                        getError("too-long").tell(res, "input", args[1]);
                    } else if (!TITLE_PATTERN.matcher(args[1]).matches()) {
                        getError("invalid-characters").tell(res, "input", args[1]);
                    } else if (res.getSettlement().addTitle(rank, args[1])) {
                        getMessage("success").tell(res, "title", args[1], "rank", rank.getTag());
                    } else {
                        getError("already-exists").tell(res, "input", args[1], "rank", rank.getTag());
                    }
                    return true;
                }
                return false;
            }
        });
        registerCommand(new SettlementSubCommand(this, "set <rank> <title>", "default") {
            @Override
            public boolean run(Resident res, String base, String alias, String... args) {
                if (args.length == 2) {
                    SettlementRank rank = SettlementRank.get(args[0]);
                    if (rank == null) {
                        getError("invalid-rank").tell(res, "rank", args[0]);
                        return true;
                    } else if (rank.above(res.getSettlementRank())) {
                        getError("cant-edit-higher").tell(res, "rank", res.getSettlementRank().getTag());
                        return true;
                    }

                    String title = res.getSettlement().getTitle(rank, args[1]);
                    if (title != null) {
                        // title exists, change order
                        res.getSettlement().setDefaultTitle(rank, title);
                        getMessage("success").tell(res, "title", args[1], "rank", rank.getTag());
                    } else {
                        // title doesn't exist, add it
                        if (res.getSettlement().getTitles(rank).size() >= Config.Settlement.TITLES_PER_RANK) {
                            getError("too-many").tell(res,
                                    "rank", res.getSettlementRank().getTag(),
                                    "max", String.valueOf(Config.Settlement.TITLES_PER_RANK)
                            );
                        } else if (args[1].length() > 16) {
                            getError("too-long").tell(res, "input", args[1]);
                        } else if (!TITLE_PATTERN.matcher(args[1]).matches()) {
                            getError("invalid-characters").tell(res, "input", args[1]);
                        } else {
                            res.getSettlement().setDefaultTitle(rank, args[1]);
                            getMessage("success-new").tell(res, "title", args[1], "rank", rank.getTag());
                        }
                    }
                    return true;
                }
                return false;
            }
        });
        registerCommand(new SettlementSubCommand(this, "remove <rank> <title>", "delete") {
            @Override
            public boolean run(Resident res, String base, String alias, String... args) {
                if (args.length == 2) {
                    SettlementRank rank = SettlementRank.get(args[0]);
                    if (rank == null) {
                        getError("invalid-rank").tell(res, "rank", args[0]);
                    } else if (rank.above(res.getSettlementRank())) {
                        getError("cant-edit-higher").tell(res, "rank", res.getSettlementRank().getTag());
                    } else if (res.getSettlement().removeTitle(rank, args[1])) {
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
        if (args.length == 0) {
            new RankSelectGUI("settlement.titles", res, res, res.getSettlement(), rank -> new TitlesGUI("settlement", res, res.getSettlement(), rank).open()).open();
            return true;
        }
        return false;
    }
}
