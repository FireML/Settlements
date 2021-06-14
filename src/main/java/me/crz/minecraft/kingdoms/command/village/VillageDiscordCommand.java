package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.resident.Resident;

public class VillageDiscordCommand extends VillageSubCommand {
    public VillageDiscordCommand(SubCommand parent) {
        super(parent, "discord [set <link>]");
        registerCommand(new VillageSubCommand(this, "set <link>", "setup") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                if (args.length == 0) {
                    String old = resident.getVillage().getDiscord();
                    resident.getVillage().setDiscord("");
                    getMessage("removed").tell(resident,
                            "state", resident.getVillage().getStateTag(),
                            "discordlink", old
                    );
                } else {
                    resident.getVillage().setDiscord(String.join(" ", args));
                    getMessage("success").tell(resident,
                            "state", resident.getVillage().getStateTag(),
                            "discordlink", resident.getVillage().getDiscord()
                    );
                }
                DatabaseManager.getInstance().saveVillage(resident.getVillage());
                return true;
            }
        });
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length == 0) {
            if (resident.getVillage().getDiscord().isEmpty()) {
                getMessage("none").tell(resident,
                        "state", resident.getVillage().getStateTag()
                );
            } else {
                getMessage("current").tell(resident,
                        "state", resident.getVillage().getStateTag(),
                        "discordlink", resident.getVillage().getDiscord()
                );
            }
            if (resident.hasPermission("village.discord.set")) {
                getMessage("setup").tell(resident,
                        "state", resident.getVillage().getStateTag()
                );
            }
            return true;
        }
        return false;
    }
}
