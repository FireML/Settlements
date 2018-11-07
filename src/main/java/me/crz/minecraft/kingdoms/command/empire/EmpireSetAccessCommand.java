package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.empire.gui.AccessGUI;
import me.crz.minecraft.kingdoms.land.LandAccess;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;

public class EmpireSetAccessCommand extends EmpireSubCommand {
    public EmpireSetAccessCommand(SubCommand parent) {
        super(parent, "setaccess <access>", EmpireRank.LEADER, "set", "access");
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 1) {
            AccessGUI.getInstance().open(resident.getPlayer());
            return true;
        }
        try {
            LandAccess access = LandAccess.valueOf(String.join("_", args).toUpperCase());
            resident.getVillage().getEmpire().setAccess(access);
            Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(resident.getVillage()));
            resident.tell(getMessage("success"), "access", access.getTag());
        } catch (IllegalArgumentException e) {
            resident.tell(getMessage("usage.head"), "name", name(), "command", "/" + base + " " + alias, "usage", getUsage(base));
            for (LandAccess access : LandAccess.values()) {
                resident.tell(getMessage("usage.entry"),
                        "usage", "/" + base + " " + alias + " " + access.getTag(),
                        "description", getText("access." + access.getKey())
                );
            }
            if (getMessage("usage.footer").exists()) {
                resident.tell(getMessage("usage.footer"), "name", name(), "command", "/" + base + " " + alias);
            }
        }
        return true;
    }
}
