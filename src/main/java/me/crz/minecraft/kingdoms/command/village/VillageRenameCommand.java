package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.utils.UtilChat;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.rank.VillageRank;
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
        String name = UtilChat.getInstance().filterCurses(args[0]);
        if (name.toLowerCase().contains("claims")) {
            getError("invalid-characters").tell(res, "input", name);
        } else if (villageExists(name)) {
            getError("village-exists").tell(res, "name", name);
        } else if (name.length() > 20) {
            getError("too-long").tell(res, "input", name, "amount", String.valueOf(20));
        } else if (!allowed.matcher(name).matches()) {
            getError("invalid-characters").tell(res, "input", name);
        } else {
            VillageManager.getInstance().changeName(village, name);
            getMessage("success").tell(village.getOnlineResidents(), "name", village.getName(), "state", village.getState().getTag());
        }
        return true;
    }

    private boolean villageExists(String name) {
        return VillageManager.getInstance().getVillageFromName(name) != null;
    }
}
