package me.crz.minecraft.kingdoms.command.village;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;

public enum PlotMode {
    pvp {
        @Override
        public void set(Boolean bool, OwnedLand land) {
            land.setPvp(bool);
        }

        @Override
        public Boolean get(OwnedLand land) {
            return land.isPvp();
        }
    },
    mobspawning {
        @Override
        public void set(Boolean bool, OwnedLand land) {
            land.setMobSpawning(bool);
        }

        @Override
        public Boolean get(OwnedLand land) {
            return land.isMobSpawning();
        }
    },
    firespread {
        @Override
        public void set(Boolean bool, OwnedLand land) {
            land.setFireSpread(bool);
        }

        @Override
        public Boolean get(OwnedLand land) {
            return land.isFireSpread();
        }
    },
    explosions {
        @Override
        public void set(Boolean bool, OwnedLand land) {
            land.setExplosions(bool);
        }

        @Override
        public Boolean get(OwnedLand land) {
            return land.isExplosions();
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
