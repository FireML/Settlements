package me.crz.minecraft.kingdoms.command.village;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.Utils;

enum PlotMode {
    build {
        @Override
        public void set(final Boolean bool, final OwnedLand land) {
            land.setBuild(bool);
            Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(land));
        }

        @Override
        public Boolean get(final OwnedLand land) {
            return land.getBuild();
        }
    },
    pvp {
        @Override
        public void set(final Boolean bool, final OwnedLand land) {
            land.setPvp(bool);
            Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(land));
        }

        @Override
        public Boolean get(final OwnedLand land) {
            return land.isPvp();
        }
    },
    mobspawning {
        @Override
        public void set(final Boolean bool, final OwnedLand land) {
            land.setMobSpawning(bool);
            Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(land));
        }

        @Override
        public Boolean get(final OwnedLand land) {
            return land.isMobSpawning();
        }
    },
    firespread {
        @Override
        public void set(final Boolean bool, final OwnedLand land) {
            land.setFireSpread(bool);
            Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(land));
        }

        @Override
        public Boolean get(final OwnedLand land) {
            return land.isFireSpread();
        }
    },
    explosions {
        @Override
        public void set(final Boolean bool, final OwnedLand land) {
            land.setExplosions(bool);
            Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(land));
        }

        @Override
        public Boolean get(final OwnedLand land) {
            return land.isExplosions();
        }
    };

    public void set(final Boolean b, final OwnedLand land) {
    }

    public Object get(final OwnedLand land) {
        return null;
    }

    public Map<String, Boolean> getOptions() {
        final Map<String, Boolean> options = new HashMap<>();
        options.put("on", true);
        options.put("off", false);
        options.put("yes", true);
        options.put("no", false);
        options.put("true", true);
        options.put("false", false);
        return options;
    }

    public static Set<String> modes() {
        return Arrays.stream(values()).map(m -> m.name().toLowerCase()).collect(Collectors.toSet());
    }
}
