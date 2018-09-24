package me.crz.minecraft.kingdoms.command.village;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.village.Village;

enum Mode {
    mobspawning {
        @Override
        public void set(final Boolean bool, final Village village) {
            village.setMobSpawning(bool);
            VillageManager.getInstance().saveVillage(village);
            for (final OwnedLand land : LandManager.getInstance().getClaimedLand(village)) {
                land.setMobSpawning(bool);
            }
        }

        @Override
        public Boolean get(final Village village) {
            return village.isMobSpawning();
        }
    },
    pvp {
        @Override
        public void set(final Boolean bool, final Village village) {
            village.setPvp(bool);
            VillageManager.getInstance().saveVillage(village);
            for (final OwnedLand land : LandManager.getInstance().getClaimedLand(village)) {
                land.setPvp(bool);
                LandManager.getInstance().saveLand(land);
            }
        }

        @Override
        public Boolean get(final Village village) {
            return village.isPvp();
        }
    },
    firespread {
        @Override
        public void set(final Boolean bool, final Village village) {
            village.setFireSpread(bool);
            VillageManager.getInstance().saveVillage(village);
            for (final OwnedLand land : LandManager.getInstance().getClaimedLand(village)) {
                land.setFireSpread(bool);
                LandManager.getInstance().saveLand(land);
            }
        }

        @Override
        public Boolean get(final Village village) {
            return village.isFireSpread();
        }
    },
    explosions {
        @Override
        public void set(final Boolean bool, final Village village) {
            village.setExplosions(bool);
            VillageManager.getInstance().saveVillage(village);
            for (final OwnedLand land : LandManager.getInstance().getClaimedLand(village)) {
                land.setExplosions(bool);
                LandManager.getInstance().saveLand(land);
            }
        }

        @Override
        public Boolean get(final Village village) {
            return village.isExplosions();
        }
    },
    buff {
        @Override
        public void set(final Boolean bool, final Village village) {
            village.setBuff(bool);
            VillageManager.getInstance().saveVillage(village);
        }

        @Override
        public Boolean get(final Village village) {
            return village.isBuff();
        }
    },
    teleporter {
        @Override
        public void set(final Boolean bool, final Village village) {
            village.setTeleporter(bool);
            VillageManager.getInstance().saveVillage(village);
        }

        @Override
        public Boolean get(final Village village) {
            return village.isTeleporter();
        }

        @Override
        public Map<String, Boolean> getOptions() {
            final Map<String, Boolean> options = new HashMap<>();
            options.put("private", true);
            options.put("public", false);
            return options;
        }
    };

    public void set(final Boolean b, final Village village) {
    }

    public Object get(final Village village) {
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

    public static List<String> modes() {
        final List<String> temp = new ArrayList<>();
        Mode[] values;
        for (int length = (values = values()).length, i = 0; i < length; ++i) {
            final Mode m = values[i];
            temp.add(m.name().toLowerCase());
        }
        return temp;
    }
}
