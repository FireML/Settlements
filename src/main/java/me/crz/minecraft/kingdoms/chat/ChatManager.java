package me.crz.minecraft.kingdoms.chat;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.ImmutableMap;
import de.themoep.minedown.MineDown;
import de.themoep.minedown.MineDownParser;
import de.themoep.minedown.Replacer;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.chat.commands.IgnoreCommand;
import me.crz.minecraft.kingdoms.chat.commands.MessageCommand;
import me.crz.minecraft.kingdoms.chat.commands.MsgToggleCommand;
import me.crz.minecraft.kingdoms.chat.commands.NotifyToggleCommand;
import me.crz.minecraft.kingdoms.chat.commands.ReplyCommand;
import me.crz.minecraft.kingdoms.chat.events.ChannelChatEvent;
import me.crz.minecraft.kingdoms.command.ChatCommand;
import me.crz.minecraft.kingdoms.command.CommandManager;
import me.crz.minecraft.kingdoms.command.PlayerSubCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.integration.PlaceholderApiIntegration;
import me.crz.minecraft.kingdoms.utils.ConfigMessages;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.Config;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.resident.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ChatManager implements ConfigMessages, Listener {
    private static ChatManager instance;

    private final Replacer notifyReplacer = new Replacer().placeholderPrefix("@").placeholderSuffix("");

    private Map<UUID, UUID> lastMessage = new HashMap<>();

    private final static DecimalFormat BALANCE_FORMAT = new DecimalFormat("#,###.##");
    private Cache<UUID, Boolean> notifyCooldowns = Caffeine.newBuilder().expireAfterAccess(Config.Chat.Notify.COOLDOWN, TimeUnit.SECONDS).build();

    private ChatManager() {

        ChatCommand chatCommand = new ChatCommand();
        CommandManager.getInstance().registerCommand(chatCommand);
        for (ChatChannel channel : ChatChannel.values()) {
            if (channel.isPrivate()) {
                continue;
            }
            SubCommand channelCommand = chatCommand.getSubCommands().get(channel.name().toLowerCase());
            if (channelCommand != null) {
                CommandManager.getInstance().registerCommand(new PlayerSubCommand(null, channel.name().toLowerCase() + "chat") {
                    @Override
                    public boolean run(Player p, String base, String alias, String... args) {
                        return channelCommand.execute(p, base, alias, args);
                    }

                    @Override
                    public boolean isVisible() {
                        return false;
                    }
                });
            }
        }
        CommandManager.getInstance().registerCommand(new MessageCommand());
        CommandManager.getInstance().registerCommand(new ReplyCommand());
        CommandManager.getInstance().registerCommand(new IgnoreCommand());
        CommandManager.getInstance().registerCommand(new MsgToggleCommand());
        CommandManager.getInstance().registerCommand(new NotifyToggleCommand());

        KingdomsPlugin.getInstance().registerListener(this);
        KingdomsPlugin.getInstance().registerListener(new ChatListener());

        KingdomsPlugin.logDebug("Loaded ChatManager with " + ChatChannel.values().length + " chat channels");
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
            if (res.setActiveChannel(channel) && !Config.Chat.RESET_CHANNELS_ON_LOGIN) {
                DatabaseManager.getInstance().saveResident(res);
            }
        }
        event.setCancelled(true);

        sendChannelMessage(sender, ResidentManager.getInstance().getResident(sender), channel, event.getMessage());
    }

    public void sendChannelMessage(Player player, ChatChannel channel, String message) {
        Utils.runAsync(() -> {
            ChannelChatEvent event = new ChannelChatEvent(true, player, message, channel);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
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

        message = ConfigManager.getInstance().getForcedCurses().filterCurses(message);

        Village village = res.getVillage();

        Replacer replacer = new Replacer()
                .placeholderPrefix("{")
                .placeholderSuffix("}")
                .replace("channel", channel.getTag())
                .replace("prefix", TextComponent.fromLegacyText(senderPrefix))
                .replace("suffix", TextComponent.fromLegacyText(senderSuffix))
                .replace("rank", TextComponent.fromLegacyText(village != null ? res.getVillageRank().getTag(village) : ""))
                .replace("title", TextComponent.fromLegacyText(res.getDisplayTitle()))
                .replace("state", TextComponent.fromLegacyText(village != null ? village.getState().getTag() : ""))
                .replace("settlement", TextComponent.fromLegacyText(village != null ? village.getName() : ""))
                .replace("empire", TextComponent.fromLegacyText(res.hasEmpire() ? village.getEmpire().getName() : ""))
                .replace("sender", new ComponentBuilder("")
                        .event(getHover(sender, res))
                        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + sender.getName()))
                        .append(getMessage("format.name").getReplacedComponents(ImmutableMap.of(
                                "name", TextComponent.fromLegacyText(sender.getName()),
                                "displayname", TextComponent.fromLegacyText(sender.getDisplayName()),
                                "prefix", TextComponent.fromLegacyText(senderPrefix),
                                "suffix", TextComponent.fromLegacyText(senderSuffix)
                        )))
                        .create());
        BaseComponent[] filteredMessage;
        BaseComponent[] unfilteredMessage;
        if (sender.hasPermission("kingdoms.chat.links")) {
            MineDownParser linkParser = new MineDownParser()
                    .urlDetection(true)
                    .urlHoverText(getMessage("hover.click-to-open-url").getText())
                    .disable(MineDownParser.Option.SIMPLE_FORMATTING)
                    .disable(MineDownParser.Option.ADVANCED_FORMATTING);
            if (!sender.hasPermission("kingdoms.chat.colour")) {
                linkParser.disable(MineDownParser.Option.LEGACY_COLORS);
            }
            filteredMessage = linkParser.parse(ConfigManager.getInstance().getToggleableCurses().filterCurses(message)).create();
            unfilteredMessage = linkParser.reset().parse(message).create();
        } else {
            if (!sender.hasPermission("kingdoms.chat.colour")) {
                message = message.replaceAll("§([0-9a-fA-F])", "&$1");
            }
            filteredMessage = TextComponent.fromLegacyText(ConfigManager.getInstance().getToggleableCurses().filterCurses(message));
            unfilteredMessage = TextComponent.fromLegacyText(message);
        }

        ChatColor channelDefault = Utils.getChannelColor(channel, ChatColorType.MESSAGE);
        ChatColor channelPrefixDefault = Utils.getChannelColor(channel, ChatColorType.PREFIX);

        switch (channel) {
            case LOCAL:
                tell(sender, res, replacer, channel, channelDefault, channelPrefixDefault, unfilteredMessage, unfilteredMessage);
                List<Resident> nearbyPlayers = new ArrayList<>(sender.getWorld().getPlayers()).stream()
                        .filter(p -> !p.equals(sender)
                                && sender.getLocation().distanceSquared(p.getLocation()) < 200 * 200)
                        .map(p -> ResidentManager.getInstance().getResident(p))
                        .filter(r -> r.isListening(channel)
                                && !(r.getIgnore().contains(sender.getUniqueId()) && !sender.hasPermission("kingdoms.bypass.ignore")))
                        .collect(Collectors.toList());
                if (!nearbyPlayers.isEmpty()) {
                    nearbyPlayers.forEach(r -> tell(sender, r, replacer, channel, channelDefault, channelPrefixDefault, unfilteredMessage, filteredMessage));
                } else {
                    getMessage("chat.nobody-nearby").tell(sender);
                }
                break;
            case HELP:
                if (!channel.autoJoin(sender)) {
                    Resident sRes = ResidentManager.getInstance().getResident(sender);
                    if (sRes.getActiveChannel() != channel) {
                        if (res.setActiveChannel(channel)) {
                            Message.get("command.chat.joined").tell(res, "name", channel.getTag());
                            DatabaseManager.getInstance().saveResident(res);
                        }
                    }
                }
                new ArrayList<>(Bukkit.getOnlinePlayers()).stream()
                        .map(p -> ResidentManager.getInstance().getResident(p))
                        .filter(r -> r.getActiveChannel() == channel || r.isListening(channel))
                        .forEach(r -> tell(sender, r, replacer, channel, channelDefault, channelPrefixDefault, unfilteredMessage, filteredMessage));
                break;
            case ADMIN:
            case STAFF:
                new ArrayList<>(Bukkit.getOnlinePlayers()).stream()
                        .filter(channel::canChat)
                        .map(p -> ResidentManager.getInstance().getResident(p))
                        .filter(r -> r.isListening(channel)
                                && !(r.getIgnore().contains(sender.getUniqueId()) && !sender.hasPermission("kingdoms.bypass.ignore")))
                        .forEach(r -> tell(sender, r, replacer, channel, channelDefault, channelPrefixDefault, unfilteredMessage, filteredMessage));
                break;
            case ALLY:
                Set<Resident> residentSet = village.getAllies().stream()
                        .filter(a -> a instanceof Village)
                        .flatMap(a -> a.getOnlineResidents().stream())
                        .collect(Collectors.toSet());
                residentSet.addAll(village.getOnlineResidents());
                residentSet.stream()
                        .filter(r -> r.isListening(channel)
                                && !(r.getIgnore().contains(sender.getUniqueId()) && !sender.hasPermission("kingdoms.bypass.ignore")))
                        .forEach(r -> tell(sender, r, replacer, channel, channelDefault, channelPrefixDefault, unfilteredMessage, filteredMessage));
                break;
            case EMPIREALLY:
                Set<Resident> eAllySet = village.getAllies().stream()
                        .filter(a -> a instanceof Empire)
                        .flatMap(a -> a.getOnlineResidents().stream())
                        .collect(Collectors.toSet());
                eAllySet.addAll(village.getOnlineResidents());
                if (village.hasEmpire()) {
                    eAllySet.addAll(village.getEmpire().getOnlineResidents());
                    village.getEmpire().getAllies().stream()
                            .flatMap(a -> a.getOnlineResidents().stream())
                            .forEach(eAllySet::add);
                }
                eAllySet.stream()
                        .filter(r -> r.isListening(channel)
                                && !(r.getIgnore().contains(sender.getUniqueId()) && !sender.hasPermission("kingdoms.bypass.ignore")))
                        .forEach(r -> tell(sender, r, replacer, channel, channelDefault, channelPrefixDefault, unfilteredMessage, filteredMessage));
                break;
            case EMPIRE:
                if (village.hasEmpire()) {
                    village.getEmpire().getOnlineResidents().stream()
                            .filter(r -> r.isListening(channel)
                                    && !(r.getIgnore().contains(sender.getUniqueId()) && !sender.hasPermission("kingdoms.bypass.ignore")))
                            .forEach(r -> tell(sender, r, replacer, channel, channelDefault, channelPrefixDefault, unfilteredMessage, filteredMessage));
                }
                break;
            case SETTLEMENT:
                village.getOnlineResidents().stream()
                        .filter(r -> r.isListening(channel)
                                && !(r.getIgnore().contains(sender.getUniqueId()) && !sender.hasPermission("kingdoms.bypass.ignore")))
                        .forEach(r -> tell(sender, r, replacer, channel, channelDefault, channelPrefixDefault, unfilteredMessage, filteredMessage));
                break;
            case EVENT:
                if (ConfigManager.getInstance().getDataConfig().getConfig().getBoolean("chat-lock")
                        && !sender.hasPermission("kingdoms.bypass.chatlock")) {
                    getMessage("locked").tell(sender, "channel", channel.name());
                    return;
                }

                new ArrayList<>(sender.getWorld().getPlayers()).stream()
                        .map(p -> ResidentManager.getInstance().getResident(p))
                        .filter(r -> r.isListening(channel)
                                && !(r.getIgnore().contains(sender.getUniqueId()) && !sender.hasPermission("kingdoms.bypass.ignore")))
                        .forEach(r -> tell(sender, r, replacer, channel, channelDefault, channelPrefixDefault, unfilteredMessage, filteredMessage));
                break;
            default:
                if (ConfigManager.getInstance().getDataConfig().getConfig().getBoolean("chat-lock")
                        && !sender.hasPermission("kingdoms.bypass.chatlock")) {
                    getMessage("locked").tell(sender, "channel", channel.name());
                    return;
                }

                new ArrayList<>(Bukkit.getOnlinePlayers()).stream()
                        .map(p -> ResidentManager.getInstance().getResident(p))
                        .filter(r -> r.isListening(channel)
                                && !(r.getIgnore().contains(sender.getUniqueId()) && !sender.hasPermission("kingdoms.bypass.ignore")))
                        .forEach(r -> tell(sender, r, replacer, channel, channelDefault, channelPrefixDefault, unfilteredMessage, filteredMessage));
                break;
        }
        System.out.println(TextComponent.toLegacyText(replacer.replace("message", unfilteredMessage).replaceIn(getFormat(sender, channel, channelDefault, channelPrefixDefault))));
    }

    private void tell(Player sender, Resident receiver, Replacer replacer, ChatChannel channel, ChatColor defaultColor, ChatColor defaultPrefixColor, BaseComponent[] unfilteredMessage, BaseComponent[] filteredMessage) {
        tell(receiver, replacer
                .replace("message", receiver.getSwear() ? unfilteredMessage : filteredMessage)
                .replaceIn(getFormat(sender, channel, receiver.getChannelColor(channel, ChatColorType.MESSAGE, defaultColor), receiver.getChannelColor(channel, ChatColorType.PREFIX, defaultPrefixColor))));
    }

    private BaseComponent[] getFormat(Player sender, ChatChannel channel, ChatColor channelColor, ChatColor channelPrefixColor) {
        BaseComponent[] format;
        if (KingdomsPlugin.getInstance().getPlaceholderApi() != null && Config.Chat.PlaceholderApi.Format.CHANNEL) {
            format = MineDown.parse(
                    PlaceholderApiIntegration.setPlaceholders(
                            sender, getMessage("format.channel." + channel.getKey())
                                    .getReplacedText("color", channelColor.toString(), "prefixColor", channelPrefixColor.toString())));
        } else {
            format = MineDown.parse(getMessage("format.channel." + channel.getKey())
                    .getReplacedText("color", channelColor.toString(), "prefixColor", channelPrefixColor.toString()));
        }
        return format;
    }


    private void tell(Resident resident, BaseComponent[] components) {
        Player player = resident.getPlayer();
        if (player == null) {
            return;
        }
        if (MessageUtils.isContainedIn("@" + resident.getName(), components)) {
            notify(resident);
            components = notifyReplacer.copy()
                    .replace(resident.getName(), TextComponent.fromLegacyText(getText("nick-hilight") + "@" + resident.getName()))
                    .replaceIn(components);
        }
        player.spigot().sendMessage(components);
    }

    public void sendPrivateMessage(Player sender, Player target, String message) {
        message = ConfigManager.getInstance().getForcedCurses().filterCurses(message);
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
        if (sender.hasPermission("kingdoms.chat.colour")) {
            message = MessageUtils.replaceColorCodes(message);
        }
        HoverEvent senderHover = getHover(sender, res);
        HoverEvent targetHover = getHover(target, tres);

        ChatColor senderColor = res.getChannelColor(ChatChannel.PRIVATE_SENT, ChatColorType.MESSAGE, Utils.getChannelColor(ChatChannel.PRIVATE_SENT, ChatColorType.MESSAGE));
        ChatColor senderPrefixColor = res.getChannelColor(ChatChannel.PRIVATE_SENT, ChatColorType.PREFIX, Utils.getChannelColor(ChatChannel.PRIVATE_SENT, ChatColorType.PREFIX));
        BaseComponent[] senderFormat;
        if (KingdomsPlugin.getInstance().getPlaceholderApi() != null && Config.Chat.PlaceholderApi.Format.PRIVATE) {
            senderFormat = MineDown.parse(
                    PlaceholderApiIntegration.setPlaceholders(
                            sender, getMessage("format.private.sender").getReplacedText("color", senderColor.toString(), "prefixColor", senderPrefixColor.toString())));
        } else {
            senderFormat = MineDown.parse(getMessage("format.private.sender").getReplacedText("color", senderColor.toString(), "prefixColor", senderPrefixColor.toString()));
        }

        BaseComponent[] filteredMessage;
        BaseComponent[] unfilteredMessage;
        if (sender.hasPermission("kingdoms.chat.links")) {
            MineDownParser linkParser = new MineDownParser()
                    .urlDetection(true)
                    .urlHoverText(getMessage("hover.click-to-open-url").getText())
                    .disable(MineDownParser.Option.SIMPLE_FORMATTING)
                    .disable(MineDownParser.Option.ADVANCED_FORMATTING);
            if (!sender.hasPermission("kingdoms.chat.colour")) {
                linkParser.disable(MineDownParser.Option.LEGACY_COLORS);
            }
            filteredMessage = linkParser.parse(ConfigManager.getInstance().getToggleableCurses().filterCurses(message)).create();
            unfilteredMessage = linkParser.reset().parse(message).create();
        } else {
            if (!sender.hasPermission("kingdoms.chat.colour")) {
                message = message.replaceAll("§([0-9a-fA-F])", "&$1");
            }
            filteredMessage = TextComponent.fromLegacyText(ConfigManager.getInstance().getToggleableCurses().filterCurses(message));
            unfilteredMessage = TextComponent.fromLegacyText(message);
        }

        MessageUtils.tell(sender, senderFormat, ImmutableMap.of(
                "sender", new ComponentBuilder()
                        .event(senderHover)
                        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + sender.getName()))
                        .append(getMessage("format.private.name.sender").getReplacedComponents(getReplacements(sender, res)))
                        .create(),
                "target", new ComponentBuilder()
                        .event(targetHover)
                        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + target.getName() + " "))
                        .append(getMessage("format.private.name.target").getReplacedComponents(getReplacements(target, tres)))
                        .create(),
                "message", unfilteredMessage,
                "staff", sender.hasPermission("kingdoms.prefix.staff") ? getMessage("staff").getComponents() : new ComponentBuilder("").create()
        ));

        ChatColor targetColor = tres.getChannelColor(ChatChannel.PRIVATE_RECEIVED, ChatColorType.MESSAGE, Utils.getChannelColor(ChatChannel.PRIVATE_RECEIVED, ChatColorType.MESSAGE));
        ChatColor targetPrefixColor = tres.getChannelColor(ChatChannel.PRIVATE_RECEIVED, ChatColorType.PREFIX, Utils.getChannelColor(ChatChannel.PRIVATE_RECEIVED, ChatColorType.PREFIX));

        BaseComponent[] targetFormat;
        if (KingdomsPlugin.getInstance().getPlaceholderApi() != null && Config.Chat.PlaceholderApi.Format.PRIVATE) {
            targetFormat = MineDown.parse(
                    PlaceholderApiIntegration.setPlaceholders(
                            target, getMessage("format.private.target").getReplacedText("color", targetColor.toString(), "prefixColor", targetPrefixColor.toString())));
        } else {
            targetFormat = MineDown.parse(getMessage("format.private.target").getReplacedText("color", targetColor.toString(), "prefixColor", targetPrefixColor.toString()));
        }
        MessageUtils.tell(target, targetFormat, ImmutableMap.of(
                "sender", new ComponentBuilder()
                        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + sender.getName() + " "))
                        .event(senderHover)
                        .append(getMessage("format.private.name.target").getReplacedComponents(getReplacements(sender, tres)))
                        .create(),
                "target", new ComponentBuilder()
                        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + target.getName()))
                        .event(targetHover)
                        .append(getMessage("format.private.name.sender").getReplacedComponents(getReplacements(target, res)))
                        .create(),
                "message", tres.getSwear() ? unfilteredMessage : filteredMessage,
                "staff", sender.hasPermission("kingdoms.prefix.staff") ? getMessage("staff").getComponents() : new ComponentBuilder("").create()
        ));

        notify(tres);

        lastMessage.put(sender.getUniqueId(), target.getUniqueId());
        lastMessage.put(target.getUniqueId(), sender.getUniqueId());

        if (tres.isAfk()) {
            if (target.hasPermission("kingdoms.prefix.staff")) {
                getMessage("afk-staff").tell(sender, "name", tres.getName());
            } else {
                getMessage("afk").tell(sender, "name", tres.getName());
            }
        }

        BaseComponent[] socialSpy = getMessage("format.private.social-spy").getReplacedComponents(
                "sender", sender.getName(),
                "senderdisplay", sender.getDisplayName(),
                "target", target.getName(),
                "targetdisplay", target.getDisplayName(),
                "message", message
        );
        for (Player pl : new ArrayList<>(Bukkit.getOnlinePlayers())) {
            if (pl != sender && pl != target && pl.hasPermission("kingdoms.admin.ss")) {
                Resident resident = ResidentManager.getInstance().getResident(pl);
                if (resident.getSocialSpy()) {
                    MessageUtils.tell(pl, socialSpy);
                }
            }
        }
    }

    private void notify(Resident resident) {
        if (resident.isNotifyEnabled()) {
            if (notifyCooldowns.getIfPresent(resident.getUUID()) != null) {
                return;
            }
            notifyCooldowns.put(resident.getUUID(), true);
            Player player = resident.getPlayer();
            if (player != null) {
                player.playSound(player.getLocation(), Config.Chat.Notify.SOUND, 3, 1);
            }
        }
    }

    private Map<String, BaseComponent[]> getReplacements(Player player, Resident res) {
        Map<String, BaseComponent[]> map = new LinkedHashMap<>();
        map.put("name", TextComponent.fromLegacyText(player.getName()));
        map.put("displayname", TextComponent.fromLegacyText(player.getDisplayName()));
        map.put("prefix", TextComponent.fromLegacyText(MessageUtils.replaceColorCodes(KingdomsPlugin.chat.getPlayerPrefix(player))));
        map.put("suffix", TextComponent.fromLegacyText(MessageUtils.replaceColorCodes(KingdomsPlugin.chat.getPlayerSuffix(player))));
        map.put("rank", TextComponent.fromLegacyText(res.hasVillage() ? res.getVillageRank().getTag(res.getVillage()) : ""));
        map.put("title", TextComponent.fromLegacyText(res.getDisplayTitle()));
        return map;
    }

    private HoverEvent getHover(Player player, Resident res) {
        BaseComponent[] hover;
        if (KingdomsPlugin.getInstance().getPlaceholderApi() != null && Config.Chat.PlaceholderApi.HOVER) {
            hover = MineDown.parse(
                    PlaceholderApiIntegration.setPlaceholders(
                            player, getMessage("hover.info").getRawText()));
        } else {
            hover = getMessage("hover.info").getComponents();
        }
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(MessageUtils.replacePlaceholders(hover,
                "name", player.getName(),
                "displayname", player.getDisplayName(),
                "prefix", MessageUtils.replaceColorCodes(KingdomsPlugin.chat.getPlayerPrefix(player)),
                "suffix", MessageUtils.replaceColorCodes(KingdomsPlugin.chat.getPlayerSuffix(player)),
                "balance", getMessage("hover.balance").getReplacedText("balance", BALANCE_FORMAT.format(KingdomsPlugin.getInstance().getEconomy().getBalance(player))),
                "bonus", getMessage("hover.bonus").getReplacedText("bonus", BALANCE_FORMAT.format(res.getBonusClaims())),
                "village", res.hasVillage() ? getMessage("hover.village").getReplacedText(
                        "state", res.getVillage().getState().getTag(),
                        "name", res.getVillage().getName(),
                        "chiefdisplaytitle", res.getVillage().getChief().getDisplayTitle(),
                        "chieftitle", res.getVillage().getChief().getVillageTitle(),
                        "chief", res.getVillage().getChief().getName()
                ): "",
                "empire", res.hasEmpire() ? getMessage("hover.empire").getReplacedText(
                        "name", res.getVillage().getEmpire().getName(),
                        "capital", res.getVillage().getEmpire().getCapital().getName(),
                        "leaderdisplaytitle", res.getVillage().getEmpire().getCapital().getChief().getDisplayTitle(),
                        "leadertitle", res.getVillage().getEmpire().getCapital().getChief().getVillageTitle(),
                        "leader", res.getVillage().getEmpire().getCapital().getChief().getName()
                ): "",
                "rank", res.hasVillage() ? getMessage("hover.rank").getReplacedText(
                        "state", res.getVillage().getState().getTag(),
                        "rank", res.getVillageRank().getTag(res.getVillage()),
                        "title", res.getDisplayTitle()
                ) : ""
        )));
    }

    public UUID getLastMessaged(Player p) {
        return lastMessage.get(p.getUniqueId());
    }

    synchronized public static ChatManager getInstance() {
        if (!Config.Chat.ENABLED) {
            throw new IllegalStateException("Tried to get ChatManager while chat was disabled!");
        }
        if (instance == null) {
            instance = new ChatManager();
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
