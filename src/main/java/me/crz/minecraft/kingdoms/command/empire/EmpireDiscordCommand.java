package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.resident.Resident;

public class EmpireDiscordCommand extends EmpireSubCommand {
    public EmpireDiscordCommand(SubCommand parent) {
        super(parent, "discord [set <link>]");
        registerCommand(new EmpireSubCommand(this, "set <link>", "setup") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                if (args.length == 0) {
                    String old = resident.getVillage().getEmpire().getDiscord();
                    resident.getVillage().getEmpire().setDiscord("");
                    getMessage("removed").tell(resident,
                            "state", resident.getVillage().getEmpire().getStateTag(),
                            "discordlink", old
                    );
                } else {
                    resident.getVillage().getEmpire().setDiscord(String.join(" ", args));
                    getMessage("success").tell(resident,
                            "state", resident.getVillage().getEmpire().getStateTag(),
                            "discordlink", resident.getVillage().getEmpire().getDiscord()
                    );
                }
                Utils.runAsync(() -> DatabaseManager.getInstance().saveEmpire(resident.getVillage().getEmpire()));
                return true;
            }
        });
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length == 0) {
            if (resident.getVillage().getEmpire().getDiscord().isEmpty()) {
                getMessage("none").tell(resident,
                        "state", resident.getVillage().getEmpire().getStateTag());
            } else {
                getMessage("current").tell(resident,
                        "state", resident.getVillage().getEmpire().getStateTag(),
                        "discordlink", resident.getVillage().getEmpire().getDiscord()
                );
            }
            if (resident.hasPermission("empire.discord.set")) {
                getMessage("setup").tell(resident);
            }
            return true;
        }
        return false;
    }
}
