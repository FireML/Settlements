package me.crz.minecraft.kingdoms.chat;

import java.util.Iterator;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import java.util.Arrays;
import java.util.List;

public enum ChatChannel
{
    LOCAL {
        ChatChannel$1(final String s, final int n) {
        }
        
        @Override
        public List<String> getAliases() {
            return Arrays.<String>asList("local", "l");
        }
    }, 
    TRADE {
        ChatChannel$2(final String s, final int n) {
        }
        
        @Override
        public List<String> getAliases() {
            return Arrays.<String>asList("trade", "t");
        }
    }, 
    GENERAL {
        ChatChannel$3(final String s, final int n) {
        }
        
        @Override
        public List<String> getAliases() {
            return Arrays.<String>asList("general", "g");
        }
    }, 
    SETTLEMENT {
        ChatChannel$4(final String s, final int n) {
        }
        
        @Override
        public List<String> getAliases() {
            return Arrays.<String>asList("settlement", "s", "village");
        }
    }, 
    OFFICER {
        ChatChannel$5(final String s, final int n) {
        }
        
        @Override
        public boolean canChat(final Player player) {
            final Village village = VillageManager.getInstance().getVillageFromPlayer(player);
            if (village == null) {
                return false;
            }
            final Resident res = ResidentManager.getInstance().getResident(player);
            return village.getChief().equals(player.getUniqueId()) || res.getVillageRank().equals(VillageRank.ELDER);
        }
        
        @Override
        public boolean canChatM(final Player player) {
            if (this.canChat(player)) {
                return true;
            }
            final Village village = VillageManager.getInstance().getVillageFromPlayer(player);
            MessageUtils.tell(player, "&c&lYou require the rank &r&6&l" + VillageRank.ELDER.getTag(village.getState()) + " or higher&r&c&l!");
            return false;
        }
        
        @Override
        public List<String> getAliases() {
            return Arrays.<String>asList("officer", "o");
        }
    }, 
    ALLY {
        ChatChannel$6(final String s, final int n) {
        }
        
        @Override
        public List<String> getAliases() {
            return Arrays.<String>asList("ally", "a");
        }
    }, 
    ADMIN {
        ChatChannel$7(final String s, final int n) {
        }
        
        @Override
        public boolean canChat(final Player player) {
            return player.hasPermission("village.admin");
        }
        
        @Override
        public boolean canChatM(final Player player) {
            if (this.canChat(player)) {
                return true;
            }
            MessageUtils.tell(player, MessageUtils.NO_PERMISSION_MESSAGE);
            return false;
        }
        
        @Override
        public List<String> getAliases() {
            return Arrays.<String>asList("admin", "ac");
        }
    }, 
    STAFF {
        ChatChannel$8(final String s, final int n) {
        }
        
        @Override
        public boolean canChat(final Player player) {
            return player.hasPermission("village.staff");
        }
        
        @Override
        public boolean canChatM(final Player player) {
            if (this.canChat(player)) {
                return true;
            }
            MessageUtils.tell(player, MessageUtils.NO_PERMISSION_MESSAGE);
            return false;
        }
        
        @Override
        public List<String> getAliases() {
            return Arrays.<String>asList("staff", "sc");
        }
    };
    
    private static final /* synthetic */ ChatChannel[] ENUM$VALUES;
    
    static {
        ENUM$VALUES = new ChatChannel[] { LOCAL, TRADE, GENERAL, SETTLEMENT, OFFICER, ALLY, ADMIN, STAFF };
    }
    
    private ChatChannel(final String s, final int n) {
    }
    
    public boolean canChat(final Player player) {
        return true;
    }
    
    public boolean canChatM(final Player player) {
        return true;
    }
    
    public abstract List<String> getAliases();
    
    public static ChatChannel get(final String name) {
        ChatChannel[] values;
        for (int length = (values = values()).length, i = 0; i < length; ++i) {
            final ChatChannel channel = values[i];
            for (final String a : channel.getAliases()) {
                if (a.equalsIgnoreCase(name)) {
                    return channel;
                }
            }
        }
        return GENERAL;
    }
    
    public static boolean isChannel(final String name) {
        ChatChannel[] values;
        for (int length = (values = values()).length, i = 0; i < length; ++i) {
            final ChatChannel channel = values[i];
            for (final String a : channel.getAliases()) {
                if (a.equalsIgnoreCase(name)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public void setActiveChatChannel(final Player p) {
        final Resident res = ResidentManager.getInstance().getResident(p);
        res.setActiveChannel(this);
        res.addChannel(this.name());
        ResidentManager.getInstance().saveResident(res);
    }
    
    public static ChatChannel[] values() {
        final ChatChannel[] enum$VALUES = ENUM$VALUES;
        final int length;
        final ChatChannel[] array;
        System.arraycopy(enum$VALUES, 0, array = new ChatChannel[length = enum$VALUES.length], 0, length);
        return array;
    }
    
    public static ChatChannel valueOf(final String s) {
        return Enum.<ChatChannel>valueOf(ChatChannel.class, s);
    }
    
    ChatChannel(final String s, final int n, final ChatChannel chatChannel) {
        this(s, n);
    }
}
