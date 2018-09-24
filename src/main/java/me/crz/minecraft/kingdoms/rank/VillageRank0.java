package me.crz.minecraft.kingdoms.rank;

import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import me.crz.minecraft.kingdoms.village.VillageState;

public enum VillageRank0
{
    RESIDENT {
        VillageRank0$1(final String s, final int n) {
        }
        
        @Override
        public String getTag(final VillageState state) {
            if (state.equals(VillageState.VILLAGE)) {
                return ConfigManager.getInstance().getConfig().getString("resident-village-name");
            }
            return ConfigManager.getInstance().getConfig().getString("resident-kingdom-name");
        }
    }, 
    BUILDER {
        VillageRank0$2(final String s, final int n) {
        }
        
        @Override
        public String getTag(final VillageState state) {
            if (state.equals(VillageState.VILLAGE)) {
                return ConfigManager.getInstance().getConfig().getString("builder-village-name");
            }
            return ConfigManager.getInstance().getConfig().getString("builder-kingdom-name");
        }
    }, 
    RECRUITER {
        VillageRank0$3(final String s, final int n) {
        }
        
        @Override
        public String getTag(final VillageState state) {
            if (state.equals(VillageState.VILLAGE)) {
                return ConfigManager.getInstance().getConfig().getString("recruiter-village-name");
            }
            return ConfigManager.getInstance().getConfig().getString("recruiter-kingdom-name");
        }
    }, 
    ELDER {
        VillageRank0$4(final String s, final int n) {
        }
        
        @Override
        public String getTag(final VillageState state) {
            if (state.equals(VillageState.VILLAGE)) {
                return ConfigManager.getInstance().getConfig().getString("elder-village-name");
            }
            return ConfigManager.getInstance().getConfig().getString("elder-kingdom-name");
        }
    }, 
    CHIEF {
        VillageRank0$5(final String s, final int n) {
        }
        
        @Override
        public String getTag(final VillageState state) {
            if (state.equals(VillageState.VILLAGE)) {
                return ConfigManager.getInstance().getConfig().getString("chief-village-name");
            }
            return ConfigManager.getInstance().getConfig().getString("chief-kingdom-name");
        }
    };
    
    private static final /* synthetic */ VillageRank0[] ENUM$VALUES;
    
    static {
        ENUM$VALUES = new VillageRank0[] { RESIDENT, BUILDER, RECRUITER, ELDER, CHIEF };
    }
    
    private VillageRank0(final String s, final int n) {
    }
    
    public String getTag(final VillageState state) {
        return "";
    }
    
    public static boolean hasRank(final VillageRank0 current, final VillageRank0 looking) {
        return current.ordinal() <= looking.ordinal();
    }
    
    public static VillageRank0[] values() {
        final VillageRank0[] enum$VALUES = ENUM$VALUES;
        final int length;
        final VillageRank0[] array;
        System.arraycopy(enum$VALUES, 0, array = new VillageRank0[length = enum$VALUES.length], 0, length);
        return array;
    }
    
    public static VillageRank0 valueOf(final String s) {
        return Enum.<VillageRank0>valueOf(VillageRank0.class, s);
    }
    
    VillageRank0(final String s, final int n, final VillageRank0 villageRank0) {
        this(s, n);
    }
}
