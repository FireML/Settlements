package de.themoep.settlements.commandold.settlement;

import de.themoep.settlements.commandold.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.resident.Resident;

public class SettlementDiscordCommand extends SettlementSubCommand {
    public SettlementDiscordCommand(SubCommand parent) {
        super(parent, "discord [set <link>]");
        registerCommand(new SettlementSubCommand(this, "set <link>", "setup") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                if (args.length == 0) {
                    String old = resident.getSettlement().getDiscord();
                    resident.getSettlement().setDiscord("");
                    getMessage("removed").tell(resident,
                            "state", resident.getSettlement().getStateTag(),
                            "discordlink", old
                    );
                } else {
                    resident.getSettlement().setDiscord(String.join(" ", args));
                    getMessage("success").tell(resident,
                            "state", resident.getSettlement().getStateTag(),
                            "discordlink", resident.getSettlement().getDiscord()
                    );
                }
                DatabaseManager.getInstance().saveSettlement(resident.getSettlement());
                return true;
            }
        });
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length == 0) {
            if (resident.getSettlement().getDiscord().isEmpty()) {
                getMessage("none").tell(resident,
                        "state", resident.getSettlement().getStateTag()
                );
            } else {
                getMessage("current").tell(resident,
                        "state", resident.getSettlement().getStateTag(),
                        "discordlink", resident.getSettlement().getDiscord()
                );
            }
            if (resident.hasPermission("settlement.discord.set")) {
                getMessage("setup").tell(resident,
                        "state", resident.getSettlement().getStateTag()
                );
            }
            return true;
        }
        return false;
    }
}
