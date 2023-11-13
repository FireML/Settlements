package de.themoep.settlements.chat;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.chat.events.ChannelChatEvent;
import de.themoep.settlements.command.ChatCommand;
import de.themoep.settlements.command.CommandManager;
import de.themoep.settlements.command.PlayerSubCommand;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.integration.PlaceholderApiIntegration;
import de.themoep.settlements.land.Ally;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.ConfigMessages;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.utils.config.ConfigManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import uk.firedev.poleislib.libs.themoep.minedown.adventure.MineDown;
import uk.firedev.poleislib.libs.themoep.minedown.adventure.MineDownParser;
import uk.firedev.poleislib.libs.themoep.minedown.adventure.Replacer;
import uk.firedev.poleislib.utils.ComponentUtils;
import uk.firedev.poleislib.utils.StringUtils;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ChatManager implements ConfigMessages, Listener {
    private static ChatManager instance;

    private final Replacer notifyReplacer = new Replacer().placeholderPrefix("@").placeholderSuffix("");

    private Map<UUID, UUID> lastMessage = new HashMap<>();

    private final static DecimalFormat BALANCE_FORMAT = new DecimalFormat("#,###.##");
    private Cache<UUID, Boolean> notifyCooldowns = Caffeine.newBuilder().expireAfterAccess(Config.Chat.Notify.COOLDOWN, TimeUnit.SECONDS).build();

    private void setup() {

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

        SettlementsPlugin.getInstance().registerListener(this);
        SettlementsPlugin.getInstance().registerListener(new ChatListener());

        SettlementsPlugin.logDebug("Loaded ChatManager with " + ChatChannel.values().length + " chat channels");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void on(AsyncChatEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player sender = event.getPlayer();
        Resident res = ResidentManager.getInstance().getResident(sender);
        Settlement settlement = res.getSettlement();
        ChatChannel channel = res.getActiveChannel();

        boolean resetChannel = false;
        if (settlement == null) {
            switch (channel) {
                case EMPIRE, ALLY, SETTLEMENT -> resetChannel = true;
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

        sendChannelMessage(sender, channel, event.message());
    }

    public void sendChannelMessage(Player player, ChatChannel channel, Component message) {
        Utils.runAsync(() -> {
            Resident res = ResidentManager.getInstance().getResident(player);
            Utils.runSync(() -> {
                ChannelChatEvent event = new ChannelChatEvent(player, message, channel);
                Bukkit.getPluginManager().callEvent(event);
                if (!event.isCancelled()) {
                    sendChannelMessage(player, res, channel, ComponentUtils.toString(message));
                }
            });
        });
    }

    private void sendChannelMessage(Player sender, Resident res, ChatChannel channel, String message) {
        if (!channel.canChatM(sender)) {
            return;
        }

        String senderPrefix = getPlayerPrefix(sender);
        String senderSuffix = getPlayerSuffix(sender);

        if (sender.hasPermission("settlements.chat.colour")) {
            message = StringUtils.parseColors(message);
        }

        message = ConfigManager.getInstance().getForcedCurses().filterCurses(message);

        Settlement settlement = res.getSettlement();

        Component prefix = ComponentUtils.parseComponent(senderPrefix);

        if (res.hasPermission("poleis.staff.punish")) {
            prefix = prefix.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/punish " + sender.getName()));
            prefix = prefix.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentUtils.parseComponent("/punish " + sender.getName())));
        }

        Component componentSender = getMessage("format.name").getComponent();
        componentSender = ComponentUtils.parsePlaceholders(componentSender,
                "name", sender.getName(),
                "displayname", ComponentUtils.toString(sender.displayName()),
                "prefix", senderPrefix,
                "suffix", senderSuffix
        );


        Replacer replacer = new Replacer()
                .placeholderPrefix("{")
                .placeholderSuffix("}")
                .replace("channel", channel.getTag())
                .replace("prefix", prefix)
                .replace("suffix", ComponentUtils.parseComponent(senderSuffix))
                .replace("rank", ComponentUtils.parseComponent(settlement != null ? res.getSettlementRank().getTag(settlement) : ""))
                .replace("title", ComponentUtils.parseComponent(res.getDisplayTitle()))
                .replace("state", ComponentUtils.parseComponent(settlement != null ? settlement.getState().getTag() : ""))
                .replace("settlement", ComponentUtils.parseComponent(settlement != null ? settlement.getName() : ""))
                .replace("empire", ComponentUtils.parseComponent(res.hasEmpire() ? settlement.getEmpire().getName() : ""))
                .replace("sender", componentSender
                        .hoverEvent(getHover(sender, res))
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + sender.getName()))
                );
        Component filteredMessage;
        Component unfilteredMessage;
        if (sender.hasPermission("settlements.chat.links")) {
            MineDownParser linkParser = new MineDownParser()
                    .urlDetection(true)
                    .urlHoverText(getMessage("hover.click-to-open-url").getText())
                    .disable(MineDownParser.Option.SIMPLE_FORMATTING)
                    .disable(MineDownParser.Option.ADVANCED_FORMATTING);
            if (!sender.hasPermission("settlements.chat.colour")) {
                linkParser.disable(MineDownParser.Option.LEGACY_COLORS);
            }
            filteredMessage = linkParser.parse(ConfigManager.getInstance().getToggleableCurses().filterCurses(message)).build();
            unfilteredMessage = linkParser.reset().parse(message).build();
        } else {
            if (!sender.hasPermission("settlements.chat.colour")) {
                message = message.replaceAll("§([0-9a-fA-F])", "&$1");
            }
            filteredMessage = ComponentUtils.parseComponent(ConfigManager.getInstance().getToggleableCurses().filterCurses(message));
            unfilteredMessage = ComponentUtils.parseComponent(message);
        }

        TextColor channelDefault = Utils.getChannelColor(channel, ChatColorType.MESSAGE);
        TextColor channelPrefixDefault = Utils.getChannelColor(channel, ChatColorType.PREFIX);

        unfilteredMessage = ComponentUtils.parseColors(unfilteredMessage);
        filteredMessage = ComponentUtils.parseColors(filteredMessage);

        Component finalUnfilteredMessage = unfilteredMessage;
        Component finalFilteredMessage = filteredMessage;
        switch (channel) {
            case LOCAL -> {
                tell(sender, res, replacer, channel, channelDefault, channelPrefixDefault, finalUnfilteredMessage, finalFilteredMessage);
                List<Resident> nearbyPlayers = new ArrayList<>(sender.getWorld().getPlayers()).stream()
                        .filter(p -> !p.equals(sender)
                                && sender.getLocation().distanceSquared(p.getLocation()) < 200 * 200)
                        .map(p -> ResidentManager.getInstance().getResident(p))
                        .filter(r -> r.isListening(channel)
                                && !(r.getIgnore().contains(sender.getUniqueId()) && !sender.hasPermission("settlements.bypass.ignore")))
                        .toList();
                if (!nearbyPlayers.isEmpty()) {
                    nearbyPlayers.forEach(r -> tell(sender, r, replacer, channel, channelDefault, channelPrefixDefault, finalUnfilteredMessage, finalFilteredMessage));
                } else {
                    getMessage("chat.nobody-nearby").tell(sender);
                }
            }
            case HELP -> {
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
                        .forEach(r -> tell(sender, r, replacer, channel, channelDefault, channelPrefixDefault, finalUnfilteredMessage, finalFilteredMessage));
            }
            case ADMIN, CCEM, STAFF -> new ArrayList<>(Bukkit.getOnlinePlayers()).stream()
                    .filter(channel::canChat)
                    .map(p -> ResidentManager.getInstance().getResident(p))
                    .filter(r -> r.isListening(channel)
                            && !(r.getIgnore().contains(sender.getUniqueId()) && !sender.hasPermission("settlements.bypass.ignore")))
                    .forEach(r -> tell(sender, r, replacer, channel, channelDefault, channelPrefixDefault, finalUnfilteredMessage, finalFilteredMessage));
            case ALLY -> {
                Set<Resident> residentSet = settlement.getAllies().stream()
                        .map(Ally::getLandOwner)
                        .filter(a -> a instanceof Settlement)
                        .flatMap(a -> a.getOnlineResidents().stream())
                        .collect(Collectors.toSet());
                residentSet.addAll(settlement.getOnlineResidents());
                residentSet.stream()
                        .filter(r -> r.isListening(channel)
                                && !(r.getIgnore().contains(sender.getUniqueId()) && !sender.hasPermission("settlements.bypass.ignore")))
                        .forEach(r -> tell(sender, r, replacer, channel, channelDefault, channelPrefixDefault, finalUnfilteredMessage, finalFilteredMessage));
            }
            case EMPIREALLY -> {
                Set<Resident> eAllySet = settlement.getAllies().stream()
                        .map(Ally::getLandOwner)
                        .filter(a -> a instanceof Empire)
                        .flatMap(a -> a.getOnlineResidents().stream())
                        .collect(Collectors.toSet());
                eAllySet.addAll(settlement.getOnlineResidents());
                if (settlement.hasEmpire()) {
                    eAllySet.addAll(settlement.getEmpire().getOnlineResidents());
                    settlement.getEmpire().getAllies().stream()
                            .map(Ally::getLandOwner)
                            .flatMap(a -> a.getOnlineResidents().stream())
                            .forEach(eAllySet::add);
                }
                eAllySet.stream()
                        .filter(r -> r.isListening(channel)
                                && !(r.getIgnore().contains(sender.getUniqueId()) && !sender.hasPermission("settlements.bypass.ignore")))
                        .forEach(r -> tell(sender, r, replacer, channel, channelDefault, channelPrefixDefault, finalUnfilteredMessage, finalFilteredMessage));
            }
            case EMPIRE -> {
                if (settlement.hasEmpire()) {
                    settlement.getEmpire().getOnlineResidents().stream()
                            .filter(r -> r.isListening(channel)
                                    && !(r.getIgnore().contains(sender.getUniqueId()) && !sender.hasPermission("settlements.bypass.ignore")))
                            .forEach(r -> tell(sender, r, replacer, channel, channelDefault, channelPrefixDefault, finalUnfilteredMessage, finalFilteredMessage));
                }
            }
            case SETTLEMENT -> settlement.getOnlineResidents().stream()
                    .filter(r -> r.isListening(channel)
                            && !(r.getIgnore().contains(sender.getUniqueId()) && !sender.hasPermission("settlements.bypass.ignore")))
                    .forEach(r -> tell(sender, r, replacer, channel, channelDefault, channelPrefixDefault, finalUnfilteredMessage, finalFilteredMessage));
            case EVENT -> {
                if (ConfigManager.getInstance().getDataConfig().getConfig().getBoolean("chat-lock")
                        && !sender.hasPermission("settlements.bypass.chatlock")) {
                    getMessage("locked").tell(sender, "channel", channel.name());
                    return;
                }
                new ArrayList<>(sender.getWorld().getPlayers()).stream()
                        .map(p -> ResidentManager.getInstance().getResident(p))
                        .filter(r -> r.isListening(channel)
                                && !(r.getIgnore().contains(sender.getUniqueId()) && !sender.hasPermission("settlements.bypass.ignore")))
                        .forEach(r -> tell(sender, r, replacer, channel, channelDefault, channelPrefixDefault, finalUnfilteredMessage, finalFilteredMessage));
            }
            default -> {
                if (ConfigManager.getInstance().getDataConfig().getConfig().getBoolean("chat-lock")
                        && !sender.hasPermission("settlements.bypass.chatlock")) {
                    getMessage("locked").tell(sender, "channel", channel.name());
                    return;
                }
                new ArrayList<>(Bukkit.getOnlinePlayers()).stream()
                        .map(p -> ResidentManager.getInstance().getResident(p))
                        .filter(r -> r.isListening(channel)
                                && !(r.getIgnore().contains(sender.getUniqueId()) && !sender.hasPermission("settlements.bypass.ignore")))
                        .forEach(r -> tell(sender, r, replacer, channel, channelDefault, channelPrefixDefault, finalUnfilteredMessage, finalFilteredMessage));
            }
        }
        Bukkit.getLogger().info(ComponentUtils.toUncoloredString(replacer.replace("message", unfilteredMessage).replaceIn(getFormat(sender, channel, channelDefault, channelPrefixDefault))));
    }

    private void tell(Player sender, Resident receiver, Replacer replacer, ChatChannel channel, TextColor defaultColor, TextColor defaultPrefixColor, Component unfilteredMessage, Component filteredMessage) {
        tell(receiver, replacer
                .replace("message", receiver.getSwear() ? unfilteredMessage : filteredMessage)
                .replaceIn(getFormat(sender, channel, receiver.getChannelColor(channel, ChatColorType.MESSAGE, defaultColor), receiver.getChannelColor(channel, ChatColorType.PREFIX, defaultPrefixColor))));
    }

    private void tell(Resident resident, Component components) {
        Player player = resident.getPlayer();
        if (player == null) {
            return;
        }
        if (MessageUtils.isContainedIn("@" + resident.getName(), ComponentUtils.toString(components))) {
            notify(resident);
            components = notifyReplacer.copy()
                    .replace(resident.getName(), ComponentUtils.parseComponent(getText("nick-hilight") + "@" + resident.getName()))
                    .replaceIn(components);
        }
        player.sendMessage(ComponentUtils.parseColors(components));
    }

    private Component getFormat(Player sender, ChatChannel channel, TextColor channelColor, TextColor channelPrefixColor) {
        Component format;
        if (SettlementsPlugin.getInstance().getPlaceholderApi() != null && Config.Chat.PlaceholderApi.Format.CHANNEL) {
            if (channelColor == null) {
                channelColor = NamedTextColor.WHITE;
            }
            if (channelPrefixColor == null) {
                channelPrefixColor = NamedTextColor.AQUA;
            }
            format = MineDown.parse(
                    PlaceholderApiIntegration.setPlaceholders(
                            sender, getMessage("format.channel." + channel.getKey())
                                    .getReplacedText("color", "&" + channelColor.asHexString(), "prefixColor", "&" + channelPrefixColor.asHexString())));
        } else {
            format = MineDown.parse(getMessage("format.channel." + channel.getKey())
                    .getReplacedText("color", "&" + channelColor.asHexString(), "prefixColor", "&" + channelPrefixColor.asHexString()));
        }
        return format;
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

    private Map<String, Component> getReplacements(Player player, Resident res) {
        Map<String, Component> map = new LinkedHashMap<>();
        map.put("name", ComponentUtils.parseComponent(player.getName()));
        map.put("displayname", player.displayName());
        map.put("prefix", ComponentUtils.parseComponent(getPlayerPrefix(player)));
        map.put("suffix", ComponentUtils.parseComponent(getPlayerSuffix(player)));
        map.put("rank", ComponentUtils.parseComponent(res.hasSettlement() ? res.getSettlementRank().getTag(res.getSettlement()) : ""));
        map.put("title", ComponentUtils.parseComponent(res.getDisplayTitle()));
        return map;
    }

    private HoverEvent<?> getHover(Player player, Resident res) {
        Component hover;
        if (SettlementsPlugin.getInstance().getPlaceholderApi() != null && Config.Chat.PlaceholderApi.HOVER) {
            hover = MineDown.parse(
                    PlaceholderApiIntegration.setPlaceholders(
                            player, getMessage("hover.info").getRawText()));
        } else {
            hover = getMessage("hover.info").getComponent();
        }
        return HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentUtils.parsePlaceholders(hover,
                "name", player.getName(),
                "displayname", ComponentUtils.toString(player.displayName()),
                "prefix", getPlayerPrefix(player),
                "suffix", getPlayerSuffix(player),
                "balance", getMessage("hover.balance").getReplacedText("balance", BALANCE_FORMAT.format(SettlementsPlugin.getInstance().getEconomy().getBalance(player))),
                "bonus", getMessage("hover.bonus").getReplacedText("bonus", BALANCE_FORMAT.format(res.getBonusClaims())),
                "settlement", res.hasSettlement() ? getMessage("hover.settlement").getReplacedText(
                        "state", res.getSettlement().getState().getTag(),
                        "name", res.getSettlement().getName(),
                        "chiefdisplaytitle", res.getSettlement().getChief().getDisplayTitle(),
                        "chieftitle", res.getSettlement().getChief().getSettlementTitle(),
                        "chief", res.getSettlement().getChief().getName()
                ): "",
                "empire", res.hasEmpire() ? getMessage("hover.empire").getReplacedText(
                        "name", res.getSettlement().getEmpire().getName(),
                        "capital", res.getSettlement().getEmpire().getCapital().getName(),
                        "leaderdisplaytitle", res.getSettlement().getEmpire().getCapital().getChief().getDisplayTitle(),
                        "leadertitle", res.getSettlement().getEmpire().getCapital().getChief().getSettlementTitle(),
                        "leader", res.getSettlement().getEmpire().getCapital().getChief().getName()
                ): "",
                "rank", res.hasSettlement() ? getMessage("hover.rank").getReplacedText(
                        "state", res.getSettlement().getState().getTag(),
                        "rank", res.getSettlementRank().getTag(res.getSettlement()),
                        "title", res.getDisplayTitle()
                ) : ""
        ));
    }

    public UUID getLastMessaged(Player p) {
        return lastMessage.get(p.getUniqueId());
    }

    public String getPlayerPrefix(Player sender) {
        return SettlementsPlugin.chat != null ? StringUtils.parseColors(SettlementsPlugin.chat.getPlayerPrefix(sender)) : "";
    }

    public String getPlayerGroupPrefix(Player sender) {
        if (SettlementsPlugin.chat == null) {
            return "";
        }
        String group = SettlementsPlugin.chat.getPrimaryGroup(sender);
        return SettlementsPlugin.chat.getGroupPrefix((String) null, group);
    }

    public String getPlayerSuffix(Player sender) {
        return SettlementsPlugin.chat != null ? StringUtils.parseColors(SettlementsPlugin.chat.getPlayerSuffix(sender)) : "";
    }

    public String getPlayerGroupSuffix(Player sender) {
        if (SettlementsPlugin.chat == null) {
            return "";
        }
        String group = SettlementsPlugin.chat.getPrimaryGroup(sender);
        return SettlementsPlugin.chat.getGroupSuffix((String) null, group);
    }

    public static ChatManager getInstance() {
        if (!Config.Chat.ENABLED) {
            throw new IllegalStateException("Tried to get ChatManager while chat was disabled!");
        }
        return instance;
    }

    public static void load() {
        if (!Config.Chat.ENABLED) {
            throw new IllegalStateException("Tried to load ChatManager while chat was disabled!");
        }
        if (instance == null) {
            instance = new ChatManager();
            instance.setup();
        }
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
