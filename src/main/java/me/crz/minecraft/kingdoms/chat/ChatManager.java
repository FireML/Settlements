package me.crz.minecraft.kingdoms.chat;

import com.google.common.collect.ImmutableMap;
import me.crz.minecraft.kingdoms.Base;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.Data;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ChatManager extends Base {
    private static ChatManager instance;

    private Map<UUID, UUID> lastMessage = new HashMap<>();

    private final static DecimalFormat BALANCE_FORMAT = new DecimalFormat("#,###.##");

    public ChatManager() {
        super();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void on(AsyncPlayerChatEvent e) {
        if (e.isCancelled()) {
            return;
        }
        Player sender = e.getPlayer();
        Resident res = ResidentManager.getInstance().getResident(sender);
        Village village = res.getVillage();
        ChatChannel channel = res.getActiveChannel();

        if (village == null) {
            switch (channel) {
                case ALLY:
                case OFFICER:
                case SETTLEMENT:
                    channel = ChatChannel.GENERAL;
                    if (res.setActiveChannel(channel)) {
                        Utils.runAsync(() -> DatabaseManager.getInstance().saveResident(res));
                    }
            }
        }

        if (!channel.canChatM(sender)) {
            return;
        }

        String senderPrefix = MessageUtils.replaceColorCodes(KingdomsPlugin.chat.getPlayerPrefix(sender));
        String senderSuffix = MessageUtils.replaceColorCodes(KingdomsPlugin.chat.getPlayerSuffix(sender));

        HoverEvent senderHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, getMessage("hover.info").getReplacedComponents(
                "name", sender.getDisplayName(),
                "prefix", senderPrefix,
                "suffix", senderSuffix,
                "balance", getMessage("hover.balance").getReplacedText("balance", BALANCE_FORMAT.format(KingdomsPlugin.getInstance().econ.getBalance(sender))),
                "village", village != null ? getMessage("hover.village").getReplacedText(
                        "state", village.getState().getTag(),
                        "name", village.getName()
                ): "",
                "rank", village != null ? getMessage("hover.rank").getReplacedText(
                        "state", village.getState().getTag(),
                        "rank", res.getVillageRank().getTag(village)
                ) : ""
        ));

        e.setCancelled(true);

        if (e.getPlayer().hasPermission("kingdoms.chat.colour")) {
            e.setMessage(MessageUtils.replaceColorCodes(e.getMessage()));
        }

        Message format = getMessage("format.channel." + channel.getKey());
        Message.MessageReplacer replacer = format.getReplacer()
                .replace("channel", channel.getTag())
                .replace("prefix", TextComponent.fromLegacyText(senderPrefix))
                .replace("suffix", TextComponent.fromLegacyText(senderSuffix))
                .replace("rank", TextComponent.fromLegacyText(village != null ? res.getVillageRank().getTag(village) : ""))
                .replace("state", TextComponent.fromLegacyText(village != null ? village.getState().getTag() : ""))
                .replace("sender", new ComponentBuilder("")
                        .append(TextComponent.fromLegacyText(sender.getDisplayName()))
                        .event(senderHover)
                        .create());
        BaseComponent[] filteredMessage = replacer
                .replace("message", KingdomsPlugin.getInstance().utilChat.filterCurses(e.getMessage()))
                .getComponents();
        BaseComponent[] unfilteredMessage = replacer
                .replace("message", e.getMessage())
                .getComponents();

        ChatChannel finalChannel = channel;
        switch (channel) {
            case TRADE:
            case GENERAL:
                if (Data.muteChat && !e.getPlayer().hasPermission("kingdoms.staff")) {
                    MessageUtils.tell(e.getPlayer(), getMessage("locked"), "channel", channel.name());
                    return;
                }

                Bukkit.getOnlinePlayers().stream()
                        .map(p -> ResidentManager.getInstance().getResident(p))
                        .filter(r -> r.isListening(finalChannel) && !r.getIgnore().contains(sender.getUniqueId()))
                        .forEach(r -> r.tell(r.getSwear() ? unfilteredMessage : filteredMessage));
                break;
            case LOCAL:
                MessageUtils.tell(sender, unfilteredMessage);
                Bukkit.getOnlinePlayers().stream()
                        .filter(p -> !p.equals(sender)
                                && sender.getLocation().getWorld().equals(p.getWorld())
                                && sender.getLocation().distanceSquared(p.getLocation()) < 200 * 200)
                        .map(p -> ResidentManager.getInstance().getResident(p))
                        .filter(r -> r.isListening(finalChannel) && !r.getIgnore().contains(sender.getUniqueId()))
                        .forEach(r -> r.tell(r.getSwear() ? unfilteredMessage : filteredMessage));
                break;
            case ADMIN:
            case STAFF:
                Bukkit.getOnlinePlayers().stream()
                        .filter(finalChannel::canChat)
                        .map(p -> ResidentManager.getInstance().getResident(p))
                        .filter(r -> r.isListening(finalChannel))
                        .forEach(r -> r.tell(r.getSwear() ? unfilteredMessage : filteredMessage));
                break;
            case OFFICER:
                village.getOnlineResidents().stream()
                        .filter(r -> r.isListening(finalChannel) && r.getVillageRank().atLeast(VillageRank.ELDER))
                        .forEach(r -> r.tell(r.getSwear() ? unfilteredMessage : filteredMessage));
                break;
            case ALLY:
                village.getAllies().stream()
                        .map(id -> VillageManager.getInstance().getVillageFromId(id))
                        .filter(Objects::nonNull)
                        .flatMap(v -> v.getOnlineResidents().stream())
                        .filter(r -> r.isListening(finalChannel))
                        .forEach(r -> r.tell(r.getSwear() ? unfilteredMessage : filteredMessage));
            case SETTLEMENT:
                village.getOnlineResidents().stream()
                        .filter(r -> r.isListening(finalChannel))
                        .forEach(r -> r.tell(r.getSwear() ? unfilteredMessage : filteredMessage));
                break;
        }
        toConsole(unfilteredMessage, e.getMessage());
    }

    private void toConsole(BaseComponent[] baseMessage, String message) {
        System.out.println(MessageUtils.replacePlaceholders(TextComponent.toLegacyText(baseMessage), "message", message));
    }

    public void sendPrivateMessage(Player sender, Player target, String message) {
        Resident res = ResidentManager.getInstance().getResident(sender);
        Resident tres = ResidentManager.getInstance().getResident(target);
        if (res.getIgnore().contains(tres.getUUID())) {
            MessageUtils.tell(sender, getMessage("ignoring"), "name", target.getName());
            return;
        }
        if (tres.getIgnore().contains(res.getUUID())) {
            MessageUtils.tell(sender, getMessage("ignored"), "name", target.getName());
            return;
        }
        String senderPrefix = MessageUtils.replaceColorCodes(KingdomsPlugin.chat.getPlayerPrefix(sender));
        String senderSuffix = MessageUtils.replaceColorCodes(KingdomsPlugin.chat.getPlayerSuffix(sender));
        HoverEvent senderHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, getMessage("hover.info").getReplacedComponents(
                "name", sender.getDisplayName(),
                "prefix", senderPrefix,
                "suffix", senderSuffix,
                "balance", getMessage("hover.balance").getReplacedText("balance", BALANCE_FORMAT.format(KingdomsPlugin.getInstance().econ.getBalance(sender))),
                "village", res.getVillage() != null ? getMessage("hover.village").getReplacedText(
                        "state", res.getVillage().getState().getTag(),
                        "name", res.getVillage().getName()
                ): ""
        ));

        String targetPrefix = MessageUtils.replaceColorCodes(KingdomsPlugin.chat.getPlayerPrefix(target));
        String targetSuffix = MessageUtils.replaceColorCodes(KingdomsPlugin.chat.getPlayerSuffix(target));
        HoverEvent targetHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, getMessage("hover.info").getReplacedComponents(
                "name", target.getDisplayName(),
                "prefix", targetPrefix,
                "suffix", targetSuffix,
                "balance", getMessage("hover.balance").getReplacedText("balance", BALANCE_FORMAT.format(KingdomsPlugin.getInstance().econ.getBalance(target))),
                "village", tres.getVillage() != null ? getMessage("hover.village").getReplacedText(
                        "state", tres.getVillage().getState().getTag(),
                        "name", tres.getVillage().getName()
                ): ""
        ));

        MessageUtils.tell(sender, getMessage("format.private.sender"), ImmutableMap.of(
                "sender", new ComponentBuilder(
                        getMessage("private.name.sender").getReplacedText(
                                "name", sender.getDisplayName(),
                                "prefix", senderPrefix,
                                "suffix", senderSuffix
                        ))
                        .event(senderHover)
                        .create(),
                "target", new ComponentBuilder(
                        getMessage("private.name.target").getReplacedText(
                                "name", target.getDisplayName(),
                                "prefix", targetPrefix,
                                "suffix", targetSuffix
                        ))
                        .event(targetHover)
                        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + target.getName() + " "))
                        .create(),
                "message", new ComponentBuilder(message).create(),
                "staff", ChatChannel.ADMIN.canChat(sender) ? getMessage("staff").getComponents() : new ComponentBuilder("").create()
        ));

        MessageUtils.tell(sender, getMessage("format.private.target"), ImmutableMap.of(
                "sender", new ComponentBuilder(
                        getMessage("private.name.target").getReplacedText(
                                "name", target.getDisplayName(),
                                "prefix", targetPrefix,
                                "suffix", targetSuffix
                        ))
                        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + sender.getName() + " "))
                        .event(senderHover)
                        .create(),
                "target", new ComponentBuilder(
                        getMessage("private.name.sender").getReplacedText(
                                "name", sender.getDisplayName(),
                                "prefix", senderPrefix,
                                "suffix", senderSuffix
                        ))
                        .event(targetHover)
                        .create(),
                "message", new ComponentBuilder(message).create(),
                "staff", ChatChannel.ADMIN.canChat(sender) ? getMessage("staff").getComponents() : new ComponentBuilder("").create()
        ));
        lastMessage.put(sender.getUniqueId(), target.getUniqueId());
        lastMessage.put(target.getUniqueId(), sender.getUniqueId());
        BaseComponent[] socialSpy = getMessage("format.social-spy").getReplacedComponents(
                "sender", sender.getDisplayName(),
                "target", target.getDisplayName(),
                "message", message
        );
        for (String sUUID : Data.socialSpy) {
            Player pl = Bukkit.getPlayer(UUID.fromString(sUUID));
            if (pl != null && pl != sender && pl != target && pl.isOnline()) {
                MessageUtils.tell(pl, socialSpy);
            }
        }
    }

    public UUID getLastMessaged(Player p) {
        return lastMessage.get(p.getUniqueId());
    }

    private String getText(String key) {
        return getMessage(key).getText();
    }

    private Message getMessage(String key) {
        return Message.get("chat." + key);
    }

    public static ChatManager getInstance() {
        if (instance == null) {
            Base.registerListener(instance = new ChatManager());
        }
        return instance;
    }
}
