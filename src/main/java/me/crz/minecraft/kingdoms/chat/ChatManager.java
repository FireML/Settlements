package me.crz.minecraft.kingdoms.chat;

import com.google.common.collect.ImmutableMap;
import de.themoep.minedown.MineDown;
import de.themoep.minedown.Replacer;
import me.crz.minecraft.kingdoms.Base;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.chat.events.ChannelChatEvent;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.ConfigMessages;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.UtilChat;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.Data;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ChatManager extends Base implements ConfigMessages {
    private static ChatManager instance;

    private Map<UUID, UUID> lastMessage = new HashMap<>();

    private final static DecimalFormat BALANCE_FORMAT = new DecimalFormat("#,###.##");

    private ChatManager() {
        super();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void on(AsyncPlayerChatEvent event) {
        if (event.isCancelled() || event instanceof ChannelChatEvent) {
            return;
        }
        Player sender = event.getPlayer();
        Resident res = ResidentManager.getInstance().getResident(sender);
        Village village = res.getVillage();
        ChatChannel channel = res.getActiveChannel();

        boolean resetChannel = false;
        if (village == null) {
            switch (channel) {
                case EMPIRE:
                case ALLY:
                case SETTLEMENT:
                    resetChannel = true;
            }
        } else if (!res.hasEmpire() && channel == ChatChannel.EMPIRE) {
            resetChannel = true;
        }

        if (resetChannel) {
            if (res.isListening(ChatChannel.GENERAL)) {
                channel = ChatChannel.GENERAL;
            } else {
                channel = res.getChannels().iterator().next();
            }
            if (res.setActiveChannel(channel) && !Utils.shouldResetChannelOnLogin()) {
                Utils.runAsync(() -> DatabaseManager.getInstance().saveResident(res));
            }
        }
        event.setCancelled(true);

        sendChannelMessage(sender, ResidentManager.getInstance().getResident(sender), channel, event.getMessage());
    }

    public void sendChannelMessage(Player player, ChatChannel channel, String message) {
        Utils.runAsync(() -> {
            if (new ChannelChatEvent(true, player, message, channel).callEvent()) {
                sendChannelMessage(player, ResidentManager.getInstance().getResident(player), channel, message);
            }
        });
    }

    private void sendChannelMessage(Player sender, Resident res, ChatChannel channel, String message) {
        if (!channel.canChatM(sender)) {
            return;
        }

        String senderPrefix = MessageUtils.replaceColorCodes(KingdomsPlugin.chat.getPlayerPrefix(sender));
        String senderSuffix = MessageUtils.replaceColorCodes(KingdomsPlugin.chat.getPlayerSuffix(sender));

        if (sender.hasPermission("kingdoms.chat.colour")) {
            message = MessageUtils.replaceColorCodes(message);
        }

        Village village = res.getVillage();

        BaseComponent[] format;
        if (KingdomsPlugin.getInstance().getPlaceholderApi() != null) {
            format = MineDown.parse(
                    KingdomsPlugin.getInstance().getPlaceholderApi().setPlaceholders(
                            sender, getMessage("format.channel." + channel.getKey()).getRawText()));
        } else {
            format = getMessage("format.channel." + channel.getKey()).getComponents();
        }

        Replacer replacer = new Replacer()
                .placeholderPrefix('{')
                .placeholderSuffix('}')
                .replace("channel", channel.getTag())
                .replace("prefix", TextComponent.fromLegacyText(senderPrefix))
                .replace("suffix", TextComponent.fromLegacyText(senderSuffix))
                .replace("rank", TextComponent.fromLegacyText(village != null ? res.getVillageRank().getTag(village) : ""))
                .replace("title", TextComponent.fromLegacyText(res.getTitle()))
                .replace("state", TextComponent.fromLegacyText(village != null ? village.getState().getTag() : ""))
                .replace("settlement", TextComponent.fromLegacyText(village != null ? village.getName() : ""))
                .replace("empire", TextComponent.fromLegacyText(res.hasEmpire() ? village.getEmpire().getName() : ""))
                .replace("sender", new ComponentBuilder("")
                        .event(getHover(sender, res))
                        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + sender.getName()))
                        .append(getMessage("format.name").getReplacedComponents(
                                "name", sender.getName(),
                                "displayname", sender.getDisplayName(),
                                "prefix", senderPrefix,
                                "suffix", senderSuffix
                        ))
                        .create());
        BaseComponent[] filteredMessage = replacer
                .replace("message", UtilChat.getInstance().filterCurses(message))
                .replaceIn(format);
        BaseComponent[] unfilteredMessage = replacer
                .replace("message", message)
                .replaceIn(format);

        switch (channel) {
            case LOCAL:
                MessageUtils.tell(sender, unfilteredMessage);
                List<Resident> nearbyPlayers = sender.getWorld().getPlayers().stream()
                        .filter(p -> !p.equals(sender)
                                && sender.getLocation().distanceSquared(p.getLocation()) < 200 * 200)
                        .map(p -> ResidentManager.getInstance().getResident(p))
                        .filter(r -> r.isListening(channel)
                                && !(r.getIgnore().contains(sender.getUniqueId()) && !sender.hasPermission("kingdoms.bypass.ignore")))
                        .collect(Collectors.toList());
                if (!nearbyPlayers.isEmpty()) {
                    nearbyPlayers.forEach(r -> r.tell(r.getSwear() ? unfilteredMessage : filteredMessage));
                } else {
                    getMessage("chat.nobody-nearby").tell(sender);
                }
                break;
            case ADMIN:
            case STAFF:
                Bukkit.getOnlinePlayers().stream()
                        .filter(channel::canChat)
                        .map(p -> ResidentManager.getInstance().getResident(p))
                        .filter(r -> r.isListening(channel)
                                && !(r.getIgnore().contains(sender.getUniqueId()) && !sender.hasPermission("kingdoms.bypass.ignore")))
                        .forEach(r -> r.tell(r.getSwear() ? unfilteredMessage : filteredMessage));
                break;
            case ALLY:
                Set<Resident> residentSet = village.getAllies().stream()
                        .flatMap(a -> a.getOnlineResidents().stream())
                        .collect(Collectors.toSet());
                residentSet.addAll(village.getOnlineResidents());
                if (village.hasEmpire()) {
                    residentSet.addAll(village.getEmpire().getOnlineResidents());
                    village.getEmpire().getAllies().stream()
                            .flatMap(a -> a.getOnlineResidents().stream())
                            .forEach(residentSet::add);
                }
                residentSet.stream()
                        .filter(r -> r.isListening(channel)
                                && !(r.getIgnore().contains(sender.getUniqueId()) && !sender.hasPermission("kingdoms.bypass.ignore")))
                        .forEach(r -> r.tell(r.getSwear() ? unfilteredMessage : filteredMessage));
                break;
            case EMPIRE:
                if (village.hasEmpire()) {
                    village.getEmpire().getOnlineResidents().stream()
                            .filter(r -> r.isListening(channel)
                                    && !(r.getIgnore().contains(sender.getUniqueId()) && !sender.hasPermission("kingdoms.bypass.ignore")))
                            .forEach(r -> r.tell(r.getSwear() ? unfilteredMessage : filteredMessage));
                }
                break;
            case SETTLEMENT:
                village.getOnlineResidents().stream()
                        .filter(r -> r.isListening(channel)
                                && !(r.getIgnore().contains(sender.getUniqueId()) && !sender.hasPermission("kingdoms.bypass.ignore")))
                        .forEach(r -> r.tell(r.getSwear() ? unfilteredMessage : filteredMessage));
                break;
            case EVENT:
                if (Data.muteChat && !sender.hasPermission("kingdoms.bypass.chatlock")) {
                    getMessage("locked").tell(sender, "channel", channel.name());
                    return;
                }

                sender.getWorld().getPlayers().stream()
                        .map(p -> ResidentManager.getInstance().getResident(p))
                        .filter(r -> r.isListening(channel)
                                && !(r.getIgnore().contains(sender.getUniqueId()) && !sender.hasPermission("kingdoms.bypass.ignore")))
                        .forEach(r -> r.tell(r.getSwear() ? unfilteredMessage : filteredMessage));
                break;
            default:
                if (Data.muteChat && !sender.hasPermission("kingdoms.bypass.chatlock")) {
                    getMessage("locked").tell(sender, "channel", channel.name());
                    return;
                }

                Bukkit.getOnlinePlayers().stream()
                        .map(p -> ResidentManager.getInstance().getResident(p))
                        .filter(r -> r.isListening(channel)
                                && !(r.getIgnore().contains(sender.getUniqueId()) && !sender.hasPermission("kingdoms.bypass.ignore")))
                        .forEach(r -> r.tell(r.getSwear() ? unfilteredMessage : filteredMessage));
                break;
        }
        System.out.println(TextComponent.toLegacyText(unfilteredMessage));
    }

    public void sendPrivateMessage(Player sender, Player target, String message) {
        Resident res = ResidentManager.getInstance().getResident(sender);
        Resident tres = ResidentManager.getInstance().getResident(target);
        if (!res.hasMessagesEnabled()) {
            getMessage("messages-disabled").tell(sender, "name", target.getName());
            return;
        }
        if (!tres.hasMessagesEnabled()) {
            getMessage("target-messages-disabled").tell(sender, "name", target.getName());
            return;
        }
        if (res.getIgnore().contains(tres.getUUID())) {
            getMessage("ignoring").tell(sender, "name", target.getName());
            return;
        }
        if (tres.getIgnore().contains(res.getUUID()) && !sender.hasPermission("kingdoms.bypass.ignore")) {
            getMessage("ignored").tell(sender, "name", target.getName());
            return;
        }
        HoverEvent senderHover = getHover(sender, res);
        HoverEvent targetHover = getHover(target, tres);

        BaseComponent[] senderFormat;
        if (KingdomsPlugin.getInstance().getPlaceholderApi() != null) {
            senderFormat = MineDown.parse(
                    KingdomsPlugin.getInstance().getPlaceholderApi().setPlaceholders(
                            sender, getMessage("format.private.sender").getRawText()));
        } else {
            senderFormat = getMessage("format.private.sender").getComponents();
        }
        MessageUtils.tell(sender, senderFormat, ImmutableMap.of(
                "sender", new ComponentBuilder(
                        getMessage("format.private.name.sender").getReplacedText(getReplacements(sender, res)))
                        .event(senderHover)
                        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + sender.getName()))
                        .create(),
                "target", new ComponentBuilder(
                        getMessage("format.private.name.target").getReplacedText(getReplacements(target, tres)))
                        .event(targetHover)
                        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + target.getName() + " "))
                        .create(),
                "message", new ComponentBuilder(message).create(),
                "staff", sender.hasPermission("kingdoms.prefix.staff") ? getMessage("staff").getComponents() : new ComponentBuilder("").create()
        ));

        BaseComponent[] targetFormat;
        if (KingdomsPlugin.getInstance().getPlaceholderApi() != null) {
            targetFormat = MineDown.parse(
                    KingdomsPlugin.getInstance().getPlaceholderApi().setPlaceholders(
                            target, getMessage("format.private.target").getRawText()));
        } else {
            targetFormat = getMessage("format.private.target").getComponents();
        }
        MessageUtils.tell(target, targetFormat, ImmutableMap.of(
                "sender", new ComponentBuilder(
                        getMessage("format.private.name.target").getReplacedText(getReplacements(sender, tres)))
                        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + sender.getName() + " "))
                        .event(senderHover)
                        .create(),
                "target", new ComponentBuilder(
                        getMessage("format.private.name.sender").getReplacedText(getReplacements(target, res)))
                        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + target.getName()))
                        .event(targetHover)
                        .create(),
                "message", new ComponentBuilder(message).create(),
                "staff", sender.hasPermission("kingdoms.prefix.staff") ? getMessage("staff").getComponents() : new ComponentBuilder("").create()
        ));
        lastMessage.put(sender.getUniqueId(), target.getUniqueId());
        lastMessage.put(target.getUniqueId(), sender.getUniqueId());
        BaseComponent[] socialSpy = getMessage("format.private.social-spy").getReplacedComponents(
                "sender", sender.getName(),
                "senderdisplay", sender.getDisplayName(),
                "target", target.getName(),
                "targetdisplay", target.getDisplayName(),
                "message", message
        );
        for (String sUUID : Data.socialSpy) {
            Player pl = Bukkit.getPlayer(UUID.fromString(sUUID));
            if (pl != null && pl != sender && pl != target && pl.isOnline() && pl.hasPermission("kingdoms.admin.ss")) {
                MessageUtils.tell(pl, socialSpy);
            }
        }
    }

    private String[] getReplacements(Player player, Resident res) {
        return new String[] {
                "name", player.getName(),
                "displayname", player.getDisplayName(),
                "prefix", MessageUtils.replaceColorCodes(KingdomsPlugin.chat.getPlayerPrefix(player)),
                "suffix", MessageUtils.replaceColorCodes(KingdomsPlugin.chat.getPlayerSuffix(player)),
                "rank", res.hasVillage() ? res.getVillageRank().getTag(res.getVillage()) : "",
                "title", res.getTitle()
        };
    }

    private HoverEvent getHover(Player player, Resident res) {
        BaseComponent[] hover;
        if (KingdomsPlugin.getInstance().getPlaceholderApi() != null) {
            hover = MineDown.parse(
                    KingdomsPlugin.getInstance().getPlaceholderApi().setPlaceholders(
                            player, getMessage("hover.info").getRawText()));
        } else {
            hover = getMessage("hover.info").getComponents();
        }
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, MessageUtils.replacePlaceholders(hover,
                "name", player.getName(),
                "displayname", player.getDisplayName(),
                "prefix", MessageUtils.replaceColorCodes(KingdomsPlugin.chat.getPlayerPrefix(player)),
                "suffix", MessageUtils.replaceColorCodes(KingdomsPlugin.chat.getPlayerSuffix(player)),
                "balance", getMessage("hover.balance").getReplacedText("balance", BALANCE_FORMAT.format(KingdomsPlugin.getInstance().econ.getBalance(player))),
                "bonus", getMessage("hover.bonus").getReplacedText("bonus", BALANCE_FORMAT.format(res.getBonusClaims())),
                "village", res.hasVillage() ? getMessage("hover.village").getReplacedText(
                        "state", res.getVillage().getState().getTag(),
                        "name", res.getVillage().getName()
                ): "",
                "empire", res.hasEmpire() ? getMessage("hover.empire").getReplacedText(
                        "name", res.getVillage().getEmpire().getName(),
                        "capital", res.getVillage().getEmpire().getCapital().getName(),
                        "leader", res.getVillage().getEmpire().getCapital().getChief().getName()
                ): "",
                "rank", res.hasVillage() ? getMessage("hover.rank").getReplacedText(
                        "state", res.getVillage().getState().getTag(),
                        "rank", res.getVillageRank().getTag(res.getVillage()),
                        "title", res.getTitle()
                ) : ""
        ));
    }

    public UUID getLastMessaged(Player p) {
        return lastMessage.get(p.getUniqueId());
    }

    public static ChatManager getInstance() {
        if (instance == null) {
            Base.registerListener(instance = new ChatManager());
        }
        return instance;
    }

    @Override
    public String type() {
        return "";
    }

    @Override
    public String name() {
        return "chat";
    }
}
