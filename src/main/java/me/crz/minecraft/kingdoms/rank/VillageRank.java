package me.crz.minecraft.kingdoms.rank;

import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import me.crz.minecraft.kingdoms.village.VillageState;

public enum VillageRank
{
    CHIEF {
        VillageRank$1(final String s, final int n) {
        }
        
        @Override
        public String getTag(final VillageState state) {
            if (state.equals(VillageState.VILLAGE)) {
                return ConfigManager.getInstance().getConfig().getString("chief-village-name");
            }
            return ConfigManager.getInstance().getConfig().getString("chief-kingdom-name");
        }
    }, 
    ELDER {
        VillageRank$2(final String s, final int n) {
        }
        
        @Override
        public String getTag(final VillageState state) {
            if (state.equals(VillageState.VILLAGE)) {
                return ConfigManager.getInstance().getConfig().getString("elder-village-name");
            }
            return ConfigManager.getInstance().getConfig().getString("elder-kingdom-name");
        }
    }, 
    RECRUITER {
        VillageRank$3(final String s, final int n) {
        }
        
        @Override
        public String getTag(final VillageState state) {
            if (state.equals(VillageState.VILLAGE)) {
                return ConfigManager.getInstance().getConfig().getString("recruiter-village-name");
            }
            return ConfigManager.getInstance().getConfig().getString("recruiter-kingdom-name");
        }
    }, 
    BUILDER {
        VillageRank$4(final String s, final int n) {
        }
        
        @Override
        public String getTag(final VillageState state) {
            if (state.equals(VillageState.VILLAGE)) {
                return ConfigManager.getInstance().getConfig().getString("builder-village-name");
            }
            return ConfigManager.getInstance().getConfig().getString("builder-kingdom-name");
        }
    }, 
    RESIDENT {
        VillageRank$5(final String s, final int n) {
        }
        
        @Override
        public String getTag(final VillageState state) {
            if (state.equals(VillageState.VILLAGE)) {
                return ConfigManager.getInstance().getConfig().getString("resident-village-name");
            }
            return ConfigManager.getInstance().getConfig().getString("resident-kingdom-name");
        }
    };
    
    private static final /* synthetic */ VillageRank[] ENUM$VALUES;
    
    static {
        ENUM$VALUES = new VillageRank[] { CHIEF, ELDER, RECRUITER, BUILDER, RESIDENT };
    }
    
    private VillageRank(final String s, final int n) {
    }
    
    public String getTag(final VillageState state) {
        return "";
    }
    
    public static boolean hasRank(final VillageRank current, final VillageRank looking) {
        return current.ordinal() <= looking.ordinal();
    }
    
    public static VillageRank[] values() {
        final VillageRank[] enum$VALUES = ENUM$VALUES;
        final int length;
        final VillageRank[] array;
        System.arraycopy(enum$VALUES, 0, array = new VillageRank[length = enum$VALUES.length], 0, length);
        return array;
    }
    
    public static VillageRank valueOf(final String s) {
        return Enum.<VillageRank>valueOf(VillageRank.class, s);
    }
    
    VillageRank(final String s, final int n, final VillageRank villageRank) {
        this(s, n);
    }
}
