package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.command.SubCommand;

import java.util.regex.Pattern;

public class VillageRenameCommand extends VillageSubCommand {
    private Pattern allowed = Pattern.compile("[\\w-]+");

    public VillageRenameCommand(SubCommand parent) {
        super(parent, "rename <name>", VillageRank.CHIEF);
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        Village village = res.getVillage();
        if (args.length == 0) {
            return false;
        }
        String name = args[0];
        if (villageExists(name)) {
            res.tell(getError("village-exists"), "name", name);
            return true;
        }
        if (name.length() > 20) {
            res.tell(getError("too-long"), "input", name, "amount", String.valueOf(20));
            return true;
        }
        if (!allowed.matcher(name).matches()) {
            res.tell(getError("invalid-characters"), "input", name);
            return true;
        }
        VillageManager.getInstance().changeName(village, name);
        getMessage("success").tell(village.getOnlineResidents(), "name", village.getName(), "state", village.getState().getTag());
        return true;
    }

    private boolean villageExists(String name) {
        return VillageManager.getInstance().getVillageFromName(name) != null;
    }
}
