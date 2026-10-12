package de.themoep.settlements.command.admin;

import de.themoep.settlements.command.HelpCommand;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.utils.config.ConfigManager;
import org.bukkit.command.CommandSender;

public class AdminCommand extends SubCommand {

    public AdminCommand() {
        super(null, "settlementadmin", "settlementsadmin", "sa", "villageadmin", "va", "kingdomadmin", "ka");
        setPermission("settlements.admin.command");
        setHelpCommand(new HelpCommand(this));
        this.registerCommand(new AdminSocialSpyCommand(this));
        this.registerCommand(new AdminReloadCommand(this));
        this.registerCommand(new AdminConvertConfigCommand(this));
        if (!Config.Chat.CHAT_ONLY) {
            this.registerCommand(new AdminBonusCommand(this));
            this.registerCommand(new AdminWorldCommand(this));
            this.registerCommand(new AdminRankCommand(this));
            this.registerCommand(new AdminEmpireLevelCommand(this));
            this.registerCommand(new AdminEmpireRankCommand(this));
            this.registerCommand(new AdminDistributeMissionCommand(this));
            this.registerCommand(new AdminDistributeNewMissionsCommand(this));
            this.registerCommand(new AdminUpkeepCommand(this));
            this.registerCommand(new AdminMissionCommand(this));
            this.registerCommand(new AdminCooperationPointsCommand(this));
            if (ConfigManager.getInstance().getEventsConf().getConfig().getBoolean("enabled")) {
                this.registerCommand(new AdminEventsCommand(this));
            }
            this.registerCommand(new AdminJoinCommand(this));
            this.registerCommand(new AdminLevelCommand(this));
        }
    }

    @Override
    public boolean run(CommandSender s, String base, String alias, String... args) {
        return false;
    }

    public int ordinal() {
        return Integer.MAX_VALUE;
    }
}
