package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;

public class EmpireSenatorsCommand extends EmpireSubCommand {
    public EmpireSenatorsCommand(SubCommand parent) {
        super(parent, "senators", "senatorlist");
    }

    @Override
    public boolean run(Resident sender, String base, String alias, String... args) {
        Utils.runAsync(() -> {
            sender.tell(getMessage("head"));
            Message entry = getMessage("entry");
            String online = getMessage("online").getText();
            String offline = getMessage("offline").getText();
            for (Resident senator : sender.getVillage().getEmpire().getSenators()) {
                entry.tell(sender,
                        "name", senator.getName(),
                        "title", senator.getTitle(),
                        "rank", senator.getVillageRank().getTag(senator.getVillage()),
                        "online", senator.getPlayer() != null ? online : offline,
                        "state", senator.getVillage().getStateTag(),
                        "settlement", senator.getVillage().getName()
                );
            }
        });
        return true;
    }
}
