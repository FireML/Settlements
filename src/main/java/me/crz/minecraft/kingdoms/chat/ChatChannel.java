package me.crz.minecraft.kingdoms.chat;

import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum ChatChannel {
    LOCAL("local", "l"),
    TRADE("trade", "t"),
    GENERAL("general", "g"),
    SETTLEMENT("settlement", "s", "village"),
    OFFICER("officer", "o") {
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
    },
    ALLY("ally", "a"),
    ADMIN("admin", "ac") {
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
    },
    STAFF("staff", "sc") {
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
    };

    private static final Map<String, ChatChannel> BY_ALIAS = new HashMap<>();

    private final List<String> aliases;

    static {
        for (ChatChannel channel : values()) {
            for (String alias : channel.getAliases()) {
                BY_ALIAS.put(alias.toLowerCase(), channel);
            }
        }
    }

    ChatChannel(String... aliases) {
        this.aliases = Arrays.asList(aliases);
    }

    public boolean canChat(final Player player) {
        return true;
    }

    public boolean canChatM(final Player player) {
        return true;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public static ChatChannel get(final String name) {
        ChatChannel channel = BY_ALIAS.get(name.toLowerCase());
        return channel != null ? channel : GENERAL;
    }

    public static boolean isChannel(final String name) {
        return BY_ALIAS.containsKey(name.toLowerCase());
    }

    public void setActiveChatChannel(final Player p) {
        final Resident res = ResidentManager.getInstance().getResident(p);
        res.setActiveChannel(this);
        res.addChannel(this.name());
        ResidentManager.getInstance().saveResident(res);
    }
}
