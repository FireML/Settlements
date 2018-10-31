package me.crz.minecraft.kingdoms.land;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.NameTag;
import org.bukkit.Material;

public enum PlotMode implements NameTag {
    pvp(Material.IRON_SWORD) {
        @Override
        public void set(Boolean bool, OwnedLand land) {
            land.setPvp(bool);
        }

        @Override
        public Boolean get(OwnedLand land) {
            return land.isPvp();
        }
    },
    mobspawning(Material.SKELETON_SKULL) {
        @Override
        public void set(Boolean bool, OwnedLand land) {
            land.setMobSpawning(bool);
        }

        @Override
        public Boolean get(OwnedLand land) {
            return land.isMobSpawning();
        }
    },
    firespread(Material.FIREWORK_STAR) {
        @Override
        public void set(Boolean bool, OwnedLand land) {
            land.setFireSpread(bool);
        }

        @Override
        public Boolean get(OwnedLand land) {
            return land.isFireSpread();
        }
    },
    explosions(Material.TNT) {
        @Override
        public void set(Boolean bool, OwnedLand land) {
            land.setExplosions(bool);
        }

        @Override
        public Boolean get(OwnedLand land) {
            return land.isExplosions();
        }
    },
    buff(Material.POTION) {
        @Override
        public void set(Boolean bool, OwnedLand land) {
            land.setBuff(bool);
        }

        @Override
        public Boolean get(OwnedLand land) {
            return land.isBuff();
        }
    },
    embassy(Material.RED_BANNER) {
        @Override
        public void set(Boolean bool, OwnedLand land) {
            land.setEmbassy(bool);
        }

        @Override
        public Boolean get(OwnedLand land) {
            return land.isEmbassy();
        }
    };

    private static Map<String, Boolean> DEFAULT_OPTIONS = new HashMap<>();
    static {
        DEFAULT_OPTIONS.put("on", true);
        DEFAULT_OPTIONS.put("off", false);
        DEFAULT_OPTIONS.put("yes", true);
        DEFAULT_OPTIONS.put("no", false);
        DEFAULT_OPTIONS.put("true", true);
        DEFAULT_OPTIONS.put("false", false);
    }
    private Material icon;

    PlotMode(Material icon) {
        this.icon = icon;
    }

    public CItem getItem() {
        return new CItem(icon).hideAllFlags(true);
    }

    public void set(Boolean b, OwnedLand land) {
    }

    public Boolean get(OwnedLand land) {
        return false;
    }

    public Map<String, Boolean> getOptions() {
        return DEFAULT_OPTIONS;
    }

    public static Set<String> modes() {
        return Arrays.stream(values()).map(m -> m.name().toLowerCase()).collect(Collectors.toSet());
    }
}
