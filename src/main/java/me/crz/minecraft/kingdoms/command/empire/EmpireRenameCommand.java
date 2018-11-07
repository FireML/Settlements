package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.utils.UtilChat;
import me.crz.minecraft.kingdoms.village.Resident;

import java.util.regex.Pattern;

public class EmpireRenameCommand extends EmpireSubCommand {
    private Pattern allowed = Pattern.compile("[\\w-]+");

    public EmpireRenameCommand(SubCommand parent) {
        super(parent, "rename <name>", EmpireRank.LEADER);
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        Empire empire = res.getVillage().getEmpire();
        if (args.length == 0) {
            return false;
        }
        String name = UtilChat.getInstance().filterCurses(args[0]);
        if (name.toLowerCase().contains("claims")) {
            getError("invalid-characters").tell(res, "input", name);
        } else if (empireExists(name)) {
            getError("empire-exists").tell(res, "name", name);
        } else if (name.length() > 20) {
            getError("too-long").tell(res, "input", name, "amount", String.valueOf(20));
        } else if (!allowed.matcher(name).matches()) {
            getError("invalid-characters").tell(res, "input", name);
        } else {
            EmpireManager.getInstance().changeName(empire, name);
            getMessage("success").tell(empire.getOnlineResidents(), "name", empire.getName());
        }
        return true;
    }

    private boolean empireExists(String name) {
        return EmpireManager.getInstance().getEmpireFromName(name) != null;
    }
}
