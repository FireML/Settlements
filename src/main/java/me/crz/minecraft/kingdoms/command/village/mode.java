package me.crz.minecraft.kingdoms.command.village;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.village.Village;

enum mode
{
    mobspawning {
        mode$1(final String s, final int n) {
        }
        
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
            return village.isPvp();
        }
        
        @Override
        public /* bridge */ Object get(final Village village) {
            return this.get(village);
        }
    }, 
    pvp {
        mode$2(final String s, final int n) {
        }
        
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
        
        @Override
        public /* bridge */ Object get(final Village village) {
            return this.get(village);
        }
    }, 
    firespread {
        mode$3(final String s, final int n) {
        }
        
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
        
        @Override
        public /* bridge */ Object get(final Village village) {
            return this.get(village);
        }
    }, 
    explosions {
        mode$4(final String s, final int n) {
        }
        
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
        
        @Override
        public /* bridge */ Object get(final Village village) {
            return this.get(village);
        }
    }, 
    buff {
        mode$5(final String s, final int n) {
        }
        
        @Override
        public void set(final Boolean bool, final Village village) {
            village.setBuff(bool);
            VillageManager.getInstance().saveVillage(village);
        }
        
        @Override
        public Boolean get(final Village village) {
            return village.isBuff();
        }
        
        @Override
        public /* bridge */ Object get(final Village village) {
            return this.get(village);
        }
    }, 
    teleporter {
        mode$6(final String s, final int n) {
        }
        
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
            final Map<String, Boolean> options = new HashMap<String, Boolean>();
            options.put("private", true);
            options.put("public", false);
            return options;
        }
        
        @Override
        public /* bridge */ Object get(final Village village) {
            return this.get(village);
        }
    };
    
    private static final /* synthetic */ mode[] ENUM$VALUES;
    
    static {
        ENUM$VALUES = new mode[] { mobspawning, pvp, firespread, explosions, buff, teleporter };
    }
    
    private mode(final String s, final int n) {
    }
    
    public void set(final Boolean b, final Village village) {
    }
    
    public Object get(final Village village) {
        return null;
    }
    
    public Map<String, Boolean> getOptions() {
        final Map<String, Boolean> options = new HashMap<String, Boolean>();
        options.put("on", true);
        options.put("off", false);
        options.put("yes", true);
        options.put("no", false);
        options.put("true", true);
        options.put("false", false);
        return options;
    }
    
    public static List<String> modes() {
        final List<String> temp = new ArrayList<String>();
        mode[] values;
        for (int length = (values = values()).length, i = 0; i < length; ++i) {
            final mode m = values[i];
            temp.add(m.name().toLowerCase());
        }
        return temp;
    }
    
    public static mode[] values() {
        final mode[] enum$VALUES = ENUM$VALUES;
        final int length;
        final mode[] array;
        System.arraycopy(enum$VALUES, 0, array = new mode[length = enum$VALUES.length], 0, length);
        return array;
    }
    
    public static mode valueOf(final String s) {
        return Enum.<mode>valueOf(mode.class, s);
    }
    
    mode(final String s, final int n, final mode mode) {
        this(s, n);
    }
}
