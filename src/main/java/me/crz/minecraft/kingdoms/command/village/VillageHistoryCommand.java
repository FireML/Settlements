package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;

import java.util.List;

public class VillageHistoryCommand extends VillageSubCommand {
    private final Type type;

    public VillageHistoryCommand(SubCommand parent, VillageRank rank, Type type) {
        super(parent, "history [<page>]", rank, "log");
        this.type = type;
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        int page;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                getError("not-a-number").tell(resident, "input", args[0]);
                return false;
            }
        } else {
            page = 1;
        }
        Utils.runAsync(() -> {
            getMessage("head").tell(resident, "page", String.valueOf(page));
            List<String[]> history = DatabaseManager.getInstance().getLog(resident.getVillage(), resident, page, type);
            for (String[] entry : history) {
                getMessage("entry").tell(resident, entry);
            }
        });
        return true;
    }

    public enum Type {
        BANK,
        VAULT,
        ALL;
    }
}
