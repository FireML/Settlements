package me.crz.minecraft.kingdoms.scoreboard.commands;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.scoreboard.ScoreboardManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;

public class CommandRainbowtag extends SubCommand {
    private final ScoreboardManager manager;

    public CommandRainbowtag(ScoreboardManager manager) {
        super(null, "rainbowtag");
        setPermission("kingdoms.rainbowtag");
        this.manager = manager;
    }

    @Override
    public boolean run(Player p, String base, String alias, String[] args) {
        if (manager.toggleRainbowTag(p)) {
            MessageUtils.tell(p, getMessage("enabled"));
        } else {
            MessageUtils.tell(p, getMessage("disabled"));
        }
        return true;
    }
}
