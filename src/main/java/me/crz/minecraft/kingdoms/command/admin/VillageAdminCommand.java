package me.crz.minecraft.kingdoms.command.admin;

import me.crz.minecraft.kingdoms.command.HelpCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.utils.config.Config;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import org.bukkit.command.CommandSender;

public class VillageAdminCommand extends SubCommand {

    public VillageAdminCommand() {
        super(null, "villageadmin", "va");
        setPermission("kingdoms.admin.command");
        setHelpCommand(new HelpCommand(this));
        this.registerCommand(new VillageAdminSocialSpyCommand(this));
        this.registerCommand(new VillageAdminReloadCommand(this));
        this.registerCommand(new VillageAdminConvertConfigCommand(this));
        if (!Config.Chat.CHAT_ONLY) {
            this.registerCommand(new VillageAdminBonusCommand(this));
            this.registerCommand(new VillageAdminWorldCommand(this));
            this.registerCommand(new VillageAdminRankCommand(this));
            this.registerCommand(new VillageAdminEmpireRankCommand(this));
            this.registerCommand(new VillageAdminEmpireMissionCommand(this));
            this.registerCommand(new VillageAdminCooperationPointsCommand(this));
            if (ConfigManager.getInstance().getEventsConf().getConfig().getBoolean("enabled")) {
                this.registerCommand(new VillageAdminEventsCommand(this));
            }
            this.registerCommand(new VillageAdminJoinCommand(this));
            this.registerCommand(new VillageAdminLevelCommand(this));
            if (Config.Lock.ENABLED) {
                this.registerCommand(new VillageAdminLockCommand(this));
            }
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
