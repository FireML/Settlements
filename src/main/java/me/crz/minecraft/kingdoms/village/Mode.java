package me.crz.minecraft.kingdoms.village;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.utils.NameTag;
import me.crz.minecraft.kingdoms.utils.Utils;
import org.bukkit.Material;

public enum Mode implements NameTag {
    pvp(Material.IRON_SWORD) {
        @Override
        public void set(Boolean bool, Village village) {
            village.setPvp(bool);
            applyAndSave(village, land -> land.setPvp(bool));
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
            applyAndSave(village, land -> land.setMobSpawning(bool));
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
            applyAndSave(village, land -> land.setFireSpread(bool));
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
            applyAndSave(village, land -> land.setExplosions(bool));
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
            applyAndSave(village, land -> land.setBuff(bool));
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

    private static void applyAndSave(Village village, Function<OwnedLand, Boolean> landApplier) {
        List<OwnedLand> toSave = new ArrayList<>();
        Collection<OwnedLand> claimedLand = LandManager.getInstance().getClaimedLand(village);
        for (OwnedLand land : claimedLand) {
            if (!land.isPlot() && landApplier.apply(land)) {
                toSave.add(land);
            }
        }
        Utils.runAsync(() -> {
            DatabaseManager.getInstance().saveVillage(village);
            DatabaseManager.getInstance().saveLand(toSave);
        });
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
