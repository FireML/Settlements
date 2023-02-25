package de.themoep.settlements.chat;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChannelCooldown {

    private static ChannelCooldown instance;
    private Map<UUID, Map<ChatChannel, Long>> chatHistory;

    private ChannelCooldown() {
        chatHistory = new HashMap<>();
    }
    public static ChannelCooldown getInstance() {
        if (instance == null) {
            instance = new ChannelCooldown();
        }
        return instance;
    }

    public long validateChatEvent(UUID sender, ChatChannel channel) {
        if (channel.getCooldown() == 0) return 0;
        if (chatHistory.containsKey(sender)) {
            if (chatHistory.get(sender).containsKey(channel)) {
                long lastMSG = chatHistory.get(sender).get(channel);
                long currentMSG;
                if ((currentMSG = Instant.now().getEpochSecond()) - lastMSG < channel.getCooldown()) return channel.getCooldown() - (currentMSG - lastMSG);
                else {
                    chatHistory.get(sender).put(channel, currentMSG);
                    return 0;
                }
            } else {
                chatHistory.get(sender).put(channel, Instant.now().getEpochSecond());
                return 0;
            }
        } else {
            Map<ChatChannel, Long> newUserMap = new HashMap<>();
            newUserMap.put(channel, Instant.now().getEpochSecond());
            chatHistory.put(sender, newUserMap);
            return 0;
        }
    }
}
