package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.utils.NameTag;
import me.crz.minecraft.kingdoms.utils.Utils;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum EmpireMode implements NameTag {
    pvp(Material.IRON_SWORD) {
        @Override
        public void set(Boolean bool, Empire empire) {
            empire.setPvp(bool);
            Utils.runAsync(() -> DatabaseManager.getInstance().saveEmpire(empire));
        }

        @Override
        public Boolean get(Empire empire) {
            return empire.isPvp();
        }
    },
    mobspawning(Material.SKELETON_SKULL) {
        @Override
        public void set(Boolean bool, Empire empire) {
            empire.setMobSpawning(bool);
            Utils.runAsync(() -> DatabaseManager.getInstance().saveEmpire(empire));
        }

        @Override
        public Boolean get(Empire empire) {
            return empire.isMobSpawning();
        }
    },
    firespread(Material.FIREWORK_STAR) {
        @Override
        public void set(Boolean bool, Empire empire) {
            empire.setFireSpread(bool);
            Utils.runAsync(() -> DatabaseManager.getInstance().saveEmpire(empire));
        }

        @Override
        public Boolean get(Empire empire) {
            return empire.isFireSpread();
        }
    },
    explosions(Material.TNT) {
        @Override
        public void set(Boolean bool, Empire empire) {
            empire.setExplosions(bool);
            Utils.runAsync(() -> DatabaseManager.getInstance().saveEmpire(empire));
        }

        @Override
        public Boolean get(Empire empire) {
            return empire.isExplosions();
        }
    },
    buff(Material.POTION) {
        @Override
        public void set(Boolean bool, Empire empire) {
            empire.setBuff(bool);
            Utils.runAsync(() -> DatabaseManager.getInstance().saveEmpire(empire));
        }

        @Override
        public Boolean get(Empire empire) {
            return empire.isBuff();
        }
    };

    private Material icon;

    EmpireMode(Material icon) {
        this.icon = icon;
    }

    public void set(Boolean b, Empire empire) {
    }

    public Boolean get(Empire empire) {
        return false;
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
        EmpireMode[] values;
        for (int length = (values = values()).length, i = 0; i < length; ++i) {
            EmpireMode m = values[i];
            temp.add(m.name().toLowerCase());
        }
        return temp;
    }

    public CItem getItem() {
        return new CItem(icon);
    }
}
