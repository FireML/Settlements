package me.crz.minecraft.kingdoms.command.village;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;

enum plotMode
{
    build {
        plotMode$1(final String s, final int n) {
        }
        
        @Override
        public void set(final Boolean bool, final OwnedLand land) {
            land.setBuild(bool);
            LandManager.getInstance().saveLand(land);
        }
        
        @Override
        public Boolean get(final OwnedLand land) {
            return land.getBuild();
        }
        
        @Override
        public /* bridge */ Object get(final OwnedLand land) {
            return this.get(land);
        }
    }, 
    pvp {
        plotMode$2(final String s, final int n) {
        }
        
        @Override
        public void set(final Boolean bool, final OwnedLand land) {
            land.setPvp(bool);
            LandManager.getInstance().saveLand(land);
        }
        
        @Override
        public Boolean get(final OwnedLand land) {
            return land.isPvp();
        }
        
        @Override
        public /* bridge */ Object get(final OwnedLand land) {
            return this.get(land);
        }
    }, 
    mobspawning {
        plotMode$3(final String s, final int n) {
        }
        
        @Override
        public void set(final Boolean bool, final OwnedLand land) {
            land.setMobSpawning(bool);
            LandManager.getInstance().saveMobData(land, bool);
        }
        
        @Override
        public Boolean get(final OwnedLand land) {
            return land.isMobSpawning();
        }
        
        @Override
        public /* bridge */ Object get(final OwnedLand land) {
            return this.get(land);
        }
    }, 
    firespread {
        plotMode$4(final String s, final int n) {
        }
        
        @Override
        public void set(final Boolean bool, final OwnedLand land) {
            land.setFireSpread(bool);
            LandManager.getInstance().saveLand(land);
        }
        
        @Override
        public Boolean get(final OwnedLand land) {
            return land.isFireSpread();
        }
        
        @Override
        public /* bridge */ Object get(final OwnedLand land) {
            return this.get(land);
        }
    }, 
    explosions {
        plotMode$5(final String s, final int n) {
        }
        
        @Override
        public void set(final Boolean bool, final OwnedLand land) {
            land.setExplosions(bool);
            LandManager.getInstance().saveLand(land);
        }
        
        @Override
        public Boolean get(final OwnedLand land) {
            return land.isExplosions();
        }
        
        @Override
        public /* bridge */ Object get(final OwnedLand land) {
            return this.get(land);
        }
    };
    
    private static final /* synthetic */ plotMode[] ENUM$VALUES;
    
    static {
        ENUM$VALUES = new plotMode[] { build, pvp, mobspawning, firespread, explosions };
    }
    
    private plotMode(final String s, final int n) {
    }
    
    public void set(final Boolean b, final OwnedLand land) {
    }
    
    public Object get(final OwnedLand land) {
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
        plotMode[] values;
        for (int length = (values = values()).length, i = 0; i < length; ++i) {
            final plotMode m = values[i];
            temp.add(m.name().toLowerCase());
        }
        return temp;
    }
    
    public static plotMode[] values() {
        final plotMode[] enum$VALUES = ENUM$VALUES;
        final int length;
        final plotMode[] array;
        System.arraycopy(enum$VALUES, 0, array = new plotMode[length = enum$VALUES.length], 0, length);
        return array;
    }
    
    public static plotMode valueOf(final String s) {
        return Enum.<plotMode>valueOf(plotMode.class, s);
    }
    
    plotMode(final String s, final int n, final plotMode plotMode) {
        this(s, n);
    }
}
