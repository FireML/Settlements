package me.crz.minecraft.kingdoms.command.village;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Village;

enum Mode {
    mobspawning {
        @Override
        public void set(Boolean bool, Village village) {
            village.setMobSpawning(bool);
            Collection<OwnedLand> claimedLand = LandManager.getInstance().getClaimedLand(village);
            for (OwnedLand land : claimedLand) {
                land.setMobSpawning(bool);
            }
            Utils.runAsync(() -> {
                DatabaseManager.getInstance().saveVillage(village);
                DatabaseManager.getInstance().saveLand(claimedLand);
            });
        }

        @Override
        public Boolean get(Village village) {
            return village.isMobSpawning();
        }
    },
    pvp {
        @Override
        public void set(Boolean bool, Village village) {
            village.setPvp(bool);
            Collection<OwnedLand> claimedLand = LandManager.getInstance().getClaimedLand(village);
            for (OwnedLand land : claimedLand) {
                land.setPvp(bool);
            }
            Utils.runAsync(() -> {
                DatabaseManager.getInstance().saveVillage(village);
                DatabaseManager.getInstance().saveLand(claimedLand);
            });
        }

        @Override
        public Boolean get(Village village) {
            return village.isPvp();
        }
    },
    firespread {
        @Override
        public void set(Boolean bool, Village village) {
            village.setFireSpread(bool);
            Collection<OwnedLand> claimedLand = LandManager.getInstance().getClaimedLand(village);
            for (OwnedLand land : claimedLand) {
                land.setFireSpread(bool);
            }
            Utils.runAsync(() -> {
                DatabaseManager.getInstance().saveVillage(village);
                DatabaseManager.getInstance().saveLand(claimedLand);
            });
        }

        @Override
        public Boolean get(Village village) {
            return village.isFireSpread();
        }
    },
    explosions {
        @Override
        public void set(Boolean bool, Village village) {
            village.setExplosions(bool);
            Collection<OwnedLand> claimedLand = LandManager.getInstance().getClaimedLand(village);
            for (OwnedLand land : claimedLand) {
                land.setExplosions(bool);
            }
            Utils.runAsync(() -> {
                DatabaseManager.getInstance().saveVillage(village);
                DatabaseManager.getInstance().saveLand(claimedLand);
            });
        }

        @Override
        public Boolean get(Village village) {
            return village.isExplosions();
        }
    },
    buff {
        @Override
        public void set(Boolean bool, Village village) {
            village.setBuff(bool);
            Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(village));
        }

        @Override
        public Boolean get(Village village) {
            return village.isBuff();
        }
    },
    teleporter {
        @Override
        public void set(Boolean bool, Village village) {
            village.setTeleporter(bool);
            Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(village));
        }

        @Override
        public Boolean get(Village village) {
            return village.isTeleporter();
        }

        @Override
        public Map<String, Boolean> getOptions() {
            Map<String, Boolean> options = new HashMap<>();
            options.put("private", true);
            options.put("public", false);
            return options;
        }
    };

    public void set(Boolean b, Village village) {
    }

    public Object get(Village village) {
        return null;
    }

    public Map<String, Boolean> getOptions() {
        Map<String, Boolean> options = new HashMap<>();
        options.put("on", true);
        options.put("off", false);
        options.put("yes", true);
        options.put("no", false);
        options.put("true", true);
        options.put("false", false);
        return options;
    }

    public static List<String> modes() {
        List<String> temp = new ArrayList<>();
        Mode[] values;
        for (int length = (values = values()).length, i = 0; i < length; ++i) {
            Mode m = values[i];
            temp.add(m.name().toLowerCase());
        }
        return temp;
    }
}
