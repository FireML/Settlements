package me.crz.minecraft.kingdoms.perk;

import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.utils.UtilTime;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.WeatherState;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class EmpirePerk extends Perk {
    private int bonusRadius = 0;

    static {
        registerAction("flying", (e, t) -> {
            if (e instanceof Empire) {
                World world = Bukkit.getWorld(((Empire) e).getWorldName());
                if (world != null) {
                    if (t) {
                        for (Player player : world.getPlayers()) {
                            player.setAllowFlight(true);
                            ResidentManager.getInstance().getResident(player).setHasEmpireFlight(true);
                        }
                    } else {
                        for (Player player : world.getPlayers()) {
                            if (player.getAllowFlight()
                                    && player.getGameMode() != GameMode.CREATIVE
                                    && player.getGameMode() != GameMode.SPECTATOR
                                    && !player.hasPermission("kingdoms.bypass.flyremoval")) {
                                if (player.isFlying()) {
                                    player.setFlying(false);
                                    Block block = player.getLocation().getBlock();
                                    int i = 0;
                                    while (block.getY() > 0 && (block = block.getRelative(BlockFace.DOWN)).getType() == Material.AIR) {
                                        i++;
                                    }
                                    if (i > 2 && block.getType() != Material.AIR) {
                                        player.teleport(block.getRelative(BlockFace.UP).getLocation().setDirection(player.getEyeLocation().getDirection()));
                                    }
                                }
                                player.setAllowFlight(false);
                                ResidentManager.getInstance().getResident(player).setHasEmpireFlight(false);
                            }
                        }
                    }
                }
            }
        }, null);
        registerAction("weather", (e, t) -> {
            if (e instanceof Empire) {
                World world = Bukkit.getWorld(((Empire) e).getWorldName());
                if (world != null) {
                    WeatherState weather = Utils.getWeatherState(world);
                    switch (weather.next()) {
                        case CLEAR:
                            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
                            world.setStorm(false);
                            world.setThundering(false);
                            break;
                        case RAIN:
                            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
                            world.setStorm(true);
                            world.setThundering(false);
                            break;
                        case THUNDER:
                            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
                            world.setStorm(true);
                            world.setThundering(true);
                            break;
                        case NORMAL:
                            world.setStorm(false);
                            world.setThundering(false);
                            world.setGameRule(GameRule.DO_WEATHER_CYCLE, true);
                            break;
                    }
                }
            }
        }, e -> {
            if (e instanceof Empire) {
                World world = Bukkit.getWorld(((Empire) e).getWorldName());
                if (world != null) {
                    return Utils.getWeatherState(world).getKey();
                }
            }
            return null;
        });
        registerAction("time", (e, t) -> {
            if (e instanceof Empire) {
                World world = Bukkit.getWorld(((Empire) e).getWorldName());
                if (world != null) {
                    world.setTime(UtilTime.MinecraftTime.getClosest(world.getTime()).next().getTicks());
                }
            }
        }, e -> {
            if (e instanceof Empire) {
                World world = Bukkit.getWorld(((Empire) e).getWorldName());
                if (world != null) {
                    return UtilTime.MinecraftTime.getClosest(world.getTime()).getKey();
                }
            }
            return null;
        });
    }

    public EmpirePerk(ConfigurationSection config) throws IllegalArgumentException {
        super(config);
        if (config.getBoolean("world.flying", false)) {
            actions.put("flying", AVAILABLE_ACTIONS.get("flying"));
        }
        this.bonusRadius = config.getInt("world.radius", bonusRadius);
    }

    public boolean isFlying() {
        return actions.containsKey("flying");
    }

    public int getBonusRadius() {
        return bonusRadius;
    }

    @Override
    protected PerkEffect.EffectLocation getDefaultEffectLocation() {
        return PerkEffect.EffectLocation.EMPIRE_WORLD;
    }
}
