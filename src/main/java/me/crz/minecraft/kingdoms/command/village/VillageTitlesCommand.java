package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.UtilChat;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import me.crz.minecraft.kingdoms.village.Resident;

import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class VillageTitlesCommand extends VillageSubCommand {

    private final Pattern TITLE_PATTERN = Pattern.compile("[\\w-]+");

    public VillageTitlesCommand(SubCommand parent) {
        super(parent, "titles [list|add <rank> <title>|remove <rank> <title>");
        registerCommand(new VillageSubCommand(this, "list") {
            @Override
            public boolean run(Resident res, String base, String alias, String... args) {
                getMessage("head").tell(res, "state", res.getVillage().getStateTag());
                for (VillageRank rank : VillageRank.values()) {
                    getMessage("rank").tell(res,
                            "rank", rank.getTag(),
                            "titles", res.getVillage().getTitles(rank).stream()
                                    .map(t -> getText("entry", "title", t))
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
                    } else if (args[1].length() > 16) {
                        getError("too-long").tell(res, "input", args[1]);
                    } else if (!TITLE_PATTERN.matcher(args[1]).matches()) {
                        getError("invalid-characters").tell(res, "input", args[1]);
                    } else if (UtilChat.getInstance().containsCurses(args[1])) {
                        getError("prohibited-words").tell(res, "input", args[1]);
                        return true;
                    } else if (res.getVillage().addTitle(rank, args[1])) {
                        getMessage("success").tell(res, "title", args[1], "rank", rank.getTag());
                        return true;
                    } else {
                        getError("already-exists").tell(res, "input", args[1], "rank", rank.getTag());
                        return true;
                    }
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
                        getError("doesnt-exists").tell(res, "input", args[1], "rank", rank.getTag());
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
