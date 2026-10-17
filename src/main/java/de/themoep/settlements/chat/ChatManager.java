package de.themoep.settlements.chat;

import org.jspecify.annotations.NonNull;
import uk.firedev.chatchannels.registry.ChatChannelRegistry;

public class ChatManager {

    private static final ChatManager INSTANCE = new ChatManager();

    private ChatManager() {}

    public static @NonNull ChatManager getInstance() {
        return INSTANCE;
    }

    public void load() {
        ChatChannelRegistry.getInstance().register(new SettlementChat());
    }

}
