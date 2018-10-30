package me.crz.minecraft.kingdoms.command.village;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.utils.NameTag;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.Material;

public enum Mode implements NameTag {
    pvp(Material.IRON_SWORD) {
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
    mobspawning(Material.SKELETON_SKULL) {
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
    firespread(Material.FIREWORK_STAR) {
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
    explosions(Material.TNT) {
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
    buff(Material.POTION) {
        @Override
        public void set(Boolean bool, Village village) {
            village.setBuff(bool);
            Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(village));
        }

        @Override
        public Boolean get(Village village) {
            return village.isBuff();
        }
    };

    private Material icon;

    Mode(Material icon) {
        this.icon = icon;
    }

    public void set(Boolean b, Village village) {
    }

    public Boolean get(Village village) {
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

    public CItem getItem() {
        return new CItem(icon).hideAllFlags(true);
    }
}
