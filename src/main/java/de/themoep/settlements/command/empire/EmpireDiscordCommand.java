package de.themoep.settlements.command.empire;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.resident.Resident;

public class EmpireDiscordCommand extends EmpireSubCommand {
    public EmpireDiscordCommand(SubCommand parent) {
        super(parent, "discord [set <link>]");
        registerCommand(new EmpireSubCommand(this, "set <link>", "setup") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                if (args.length == 0) {
                    String old = resident.getSettlement().getEmpire().getDiscord();
                    resident.getSettlement().getEmpire().setDiscord("");
                    getMessage("removed").tell(resident,
                            "state", resident.getSettlement().getEmpire().getStateTag(),
                            "discordlink", old
                    );
                } else {
                    resident.getSettlement().getEmpire().setDiscord(String.join(" ", args));
                    getMessage("success").tell(resident,
                            "state", resident.getSettlement().getEmpire().getStateTag(),
                            "discordlink", resident.getSettlement().getEmpire().getDiscord()
                    );
                }
                DatabaseManager.getInstance().saveEmpire(resident.getSettlement().getEmpire());
                return true;
            }
        });
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length == 0) {
            if (resident.getSettlement().getEmpire().getDiscord().isEmpty()) {
                getMessage("none").tell(resident,
                        "state", resident.getSettlement().getEmpire().getStateTag());
            } else {
                getMessage("current").tell(resident,
                        "state", resident.getSettlement().getEmpire().getStateTag(),
                        "discordlink", resident.getSettlement().getEmpire().getDiscord()
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
