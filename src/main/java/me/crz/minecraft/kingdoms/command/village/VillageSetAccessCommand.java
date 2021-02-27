package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandAccess;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.village.gui.AccessGUI;

public class VillageSetAccessCommand extends VillageSubCommand {
    public VillageSetAccessCommand(SubCommand parent) {
        super(parent, "setaccess <access>",  "set", "access");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 1) {
            new AccessGUI().open(resident.getPlayer());
            return true;
        }
        try {
            LandAccess access = LandAccess.valueOf(String.join("_", args).toUpperCase());
            resident.getVillage().setAccess(access);
            Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(resident.getVillage()));
            resident.tell(getMessage("success"), "access", access.getTag());
        } catch (IllegalArgumentException e) {
            resident.tell(getMessage("usage.head"),
                    "name", name(),
                    "command", "/" + base + " " + alias,
                    "usage", getUsage(base),
                    "type", resident.getVillage().getStateTag() + " "
            );
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
