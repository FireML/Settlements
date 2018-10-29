package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.EmpireSubCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
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
        String name = args[0];
        if (empireExists(name)) {
            res.tell(getError("empire-exists"), "name", name);
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
        EmpireManager.getInstance().changeName(empire, name);
        getMessage("success").tell(empire.getOnlineResidents(), "name", empire.getName());
        return true;
    }

    private boolean empireExists(String name) {
        return EmpireManager.getInstance().getEmpireFromName(name) != null;
    }
}
