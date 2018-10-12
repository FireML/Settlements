package me.crz.minecraft.kingdoms.chat;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.HumanReadable;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
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

public enum ChatChannel implements HumanReadable {
    LOCAL("local", "l"),
    TRADE("trade", "t"),
    GENERAL("general", "g"),
    SETTLEMENT("settlement", "s", "village"),
    OFFICER("officer", "o") {
        @Override
        public boolean canChat(Player player) {
            Village village = VillageManager.getInstance().getVillageFromPlayer(player);
            if (village == null) {
                return false;
            }
            Resident res = ResidentManager.getInstance().getResident(player);
            return village.getChief().equals(player.getUniqueId()) || res.getVillageRank().equals(VillageRank.ELDER);
        }

        @Override
        public boolean canChatM(Player player) {
            if (this.canChat(player)) {
                return true;
            }
            Village village = VillageManager.getInstance().getVillageFromPlayer(player);
            MessageUtils.tell(player, Message.get("rank-required"), "rank", VillageRank.ELDER.getTag(village.getState()));
            return false;
        }
    },
    ALLY("ally", "a"),
    ADMIN("admin", "ac") {
        @Override
        public boolean canChat(Player player) {
            return player.hasPermission("kingdoms.admin");
        }

        @Override
        public boolean canChatM(Player player) {
            if (this.canChat(player)) {
                return true;
            }
            MessageUtils.tell(player, Message.get("no-permission"));
            return false;
        }
    },
    STAFF("staff", "sc") {
        @Override
        public boolean canChat(Player player) {
            return player.hasPermission("kingdoms.staff");
        }

        @Override
        public boolean canChatM(Player player) {
            if (this.canChat(player)) {
                return true;
            }
            MessageUtils.tell(player, Message.get("no-permission"));
            return false;
        }
    };

    private static Map<String, ChatChannel> BY_ALIAS = new HashMap<>();

    private List<String> aliases;

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

    public boolean canChat(Player player) {
        return true;
    }

    public boolean canChatM(Player player) {
        return true;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public static ChatChannel get(String name) {
        ChatChannel channel = BY_ALIAS.get(name.toLowerCase());
        return channel != null ? channel : GENERAL;
    }

    public static boolean isChannel(String name) {
        return BY_ALIAS.containsKey(name.toLowerCase());
    }

    public void setActiveChatChannel(Player p) {
        Resident res = ResidentManager.getInstance().getResident(p);
        if (res.setActiveChannel(this) | res.addChannel(this)) {
            Utils.runAsync(() -> DatabaseManager.getInstance().saveResident(res));
        }
    }
}
