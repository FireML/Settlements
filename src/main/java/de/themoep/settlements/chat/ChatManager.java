package de.themoep.settlements.chat;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.themoep.minedown.adventure.MineDown;
import de.themoep.minedown.adventure.MineDownParser;
import de.themoep.minedown.adventure.Replacer;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.chat.events.ChannelChatEvent;
import de.themoep.settlements.command.ChatCommand;
import de.themoep.settlements.command.CommandManager;
import de.themoep.settlements.command.PlayerSubCommand;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.land.Ally;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.ComponentUtils;
import de.themoep.settlements.utils.ConfigMessages;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.StringUtils;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.utils.config.ConfigManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

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
            prefix = prefix.clickEvent(ClickEvent.suggestCommand("/punish " + sender.getName()));
            prefix = prefix.hoverEvent(HoverEvent.showText(ComponentUtils.parseComponent("&b/punish " + sender.getName())));
        }

        Component componentSender = getMessage("format.name").getComponent();
        componentSender = ComponentUtils.parsePlaceholders(componentSender,
                "name", sender.getName(),
                "displayname", ComponentUtils.toString(sender.displayName()),
                "prefix", senderPrefix,
                "suffix", senderSuffix
        );

        Map<String, Component> replacements = Map.of(
                "channel", ComponentUtils.parseComponent(channel.getTag()),
                "prefix", prefix,
                "suffix", ComponentUtils.parseComponent(senderSuffix),
                "rank", ComponentUtils.parseComponent(settlement != null ? res.getSettlementRank().getTag(settlement) : ""),
                "title", ComponentUtils.parseComponent(res.getDisplayTitle()),
                "state", ComponentUtils.parseComponent(settlement != null ? settlement.getState().getTag() : ""),
                "settlement", ComponentUtils.parseComponent(settlement != null ? settlement.getName() : ""),
                "empire", ComponentUtils.parseComponent(res.hasEmpire() ? settlement.getEmpire().getName() : ""),
                "sender", componentSender
                        .hoverEvent(getHover(sender, res))
                        .clickEvent(ClickEvent.suggestCommand("/msg " + sender.getName()))
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

        Component hover = sender.getInventory().getItemInMainHand().displayName();
        filteredMessage = new Replacer()
                .placeholderPrefix("[")
                .placeholderSuffix("]")
                .replace("item", hover)
                .replace("i", hover)
                .replaceIn(filteredMessage);
        unfilteredMessage = new Replacer()
            .placeholderPrefix("[")
            .placeholderSuffix("]")
            .replace("item", hover)
            .replace("i", hover)
            .replaceIn(unfilteredMessage);

        TextColor channelDefault = Utils.getChannelColor(channel, ChatColorType.MESSAGE);
        TextColor channelPrefixDefault = Utils.getChannelColor(channel, ChatColorType.PREFIX);

        Component finalUnfilteredMessage = unfilteredMessage;
        Component finalFilteredMessage = filteredMessage;
        switch (channel) {
            case LOCAL -> {
                tell(sender, res, channel, channelDefault, channelPrefixDefault, finalUnfilteredMessage, finalFilteredMessage, replacements, false);
                List<Resident> nearbyPlayers = new ArrayList<>(sender.getWorld().getPlayers()).stream()
                        .filter(p -> !p.equals(sender)
                                && sender.getLocation().distanceSquared(p.getLocation()) < 200 * 200)
                        .map(p -> ResidentManager.getInstance().getResident(p))
                        .toList();
                if (!nearbyPlayers.isEmpty()) {
                    tell(sender, nearbyPlayers, channel, channelDefault, channelPrefixDefault, finalUnfilteredMessage, finalFilteredMessage, replacements, true);
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
                List<Resident> targets = new ArrayList<>(Bukkit.getOnlinePlayers()).stream()
                        .map(p -> ResidentManager.getInstance().getResident(p))
                        .filter(r -> r.getActiveChannel() == channel || r.isListening(channel))
                        .toList();
                tell(sender, targets, channel, channelDefault, channelPrefixDefault, finalUnfilteredMessage, finalFilteredMessage, replacements, false);
            }
            case ADMIN, CCEM, STAFF -> {
                List<Resident> targets = new ArrayList<>(Bukkit.getOnlinePlayers()).stream()
                        .filter(channel::canChat)
                        .map(p -> ResidentManager.getInstance().getResident(p))
                        .toList();
                tell(sender, targets, channel, channelDefault, channelPrefixDefault, finalUnfilteredMessage, finalFilteredMessage, replacements, false);
            }
            case ALLY -> {
                List<Resident> residentSet = settlement.getAllies().stream()
                        .map(Ally::getLandOwner)
                        .filter(a -> a instanceof Settlement)
                        .flatMap(a -> a.getOnlineResidents().stream())
                        .collect(Collectors.toCollection(ArrayList::new));
                residentSet.addAll(settlement.getOnlineResidents());
                tell(sender, residentSet, channel, channelDefault, channelPrefixDefault, finalUnfilteredMessage, finalFilteredMessage, replacements, false);
            }
            case EMPIREALLY -> {
                List<Resident> eAllySet = settlement.getAllies().stream()
                        .map(Ally::getLandOwner)
                        .filter(a -> a instanceof Empire)
                        .flatMap(a -> a.getOnlineResidents().stream())
                        .collect(Collectors.toCollection(ArrayList::new));
                eAllySet.addAll(settlement.getOnlineResidents());
                if (settlement.hasEmpire()) {
                    eAllySet.addAll(settlement.getEmpire().getOnlineResidents());
                    settlement.getEmpire().getAllies().stream()
                            .map(Ally::getLandOwner)
                            .flatMap(a -> a.getOnlineResidents().stream())
                            .forEach(eAllySet::add);
                }
                tell(sender, eAllySet, channel, channelDefault, channelPrefixDefault, finalUnfilteredMessage, finalFilteredMessage, replacements, false);
            }
            case EMPIRE -> {
                if (settlement.hasEmpire()) {
                    List<Resident> targets = settlement.getEmpire().getOnlineResidents();
                    tell(sender, targets, channel, channelDefault, channelPrefixDefault, finalUnfilteredMessage, finalFilteredMessage, replacements, false);
                }
            }
            case SETTLEMENT -> {
                List<Resident> targets = settlement.getOnlineResidents();
                tell(sender, targets, channel, channelDefault, channelPrefixDefault, finalUnfilteredMessage, finalFilteredMessage, replacements, false);
            }
            case EVENT -> {
                if (ConfigManager.getInstance().getDataConfig().getConfig().getBoolean("chat-lock")
                        && !sender.hasPermission("settlements.bypass.chatlock")) {
                    getMessage("locked").tell(sender, "channel", channel.name());
                    return;
                }
                List<Resident> targets = new ArrayList<>(sender.getWorld().getPlayers()).stream()
                        .map(p -> ResidentManager.getInstance().getResident(p))
                        .toList();
                tell(sender, targets, channel, channelDefault, channelPrefixDefault, finalUnfilteredMessage, finalFilteredMessage, replacements, false);
            }
            default -> {
                if (ConfigManager.getInstance().getDataConfig().getConfig().getBoolean("chat-lock")
                        && !sender.hasPermission("settlements.bypass.chatlock")) {
                    getMessage("locked").tell(sender, "channel", channel.name());
                    return;
                }
                List<Resident> targets = new ArrayList<>(Bukkit.getOnlinePlayers()).stream()
                        .map(p -> ResidentManager.getInstance().getResident(p))
                        .toList();
                tell(sender, targets, channel, channelDefault, channelPrefixDefault, finalUnfilteredMessage, finalFilteredMessage, replacements, false);
            }
        }
    }

    private void tell(Player sender, List<Resident> receivers, ChatChannel channel, TextColor defaultColor, TextColor defaultPrefixColor, Component unfilteredMessage, Component filteredMessage, Map<String, Component> replacements, boolean logged) {
        for (Resident receiver : receivers) {
            Component component = new Replacer()
                .placeholderPrefix("{")
                .placeholderSuffix("}")
                    .replace(replacements)
                    .replace("message", receiver.getSwear() ? unfilteredMessage : filteredMessage)
                    .replaceIn(getFormat(sender, channel, receiver.getChannelColor(channel, ChatColorType.MESSAGE, defaultColor), receiver.getChannelColor(channel, ChatColorType.PREFIX, defaultPrefixColor)));
            tell(receiver, component);
            if (!logged) {
                logged = true;
                SettlementsPlugin.getInstance().getLogging().info(PlainTextComponentSerializer.plainText().serialize(component));
            }
        }
    }

    private void tell(Player sender, Resident receiver, ChatChannel channel, TextColor defaultColor, TextColor defaultPrefixColor, Component unfilteredMessage, Component filteredMessage, Map<String, Component> replacements, boolean logged) {
        Component component = new Replacer()
            .placeholderPrefix("{")
            .placeholderSuffix("}")
            .replace(replacements)
            .replace("message", receiver.getSwear() ? unfilteredMessage : filteredMessage)
            .replaceIn(getFormat(sender, channel, receiver.getChannelColor(channel, ChatColorType.MESSAGE, defaultColor), receiver.getChannelColor(channel, ChatColorType.PREFIX, defaultPrefixColor)));
        tell(receiver, component);
        if (logged) {
            SettlementsPlugin.getInstance().getLogging().info(PlainTextComponentSerializer.plainText().serialize(component));
        }
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
        player.sendMessage(components);
    }

    private Component getFormat(Player sender, ChatChannel channel, TextColor channelColor, TextColor channelPrefixColor) {
        Component format = MineDown.parse(getMessage("format.channel." + channel.getKey())
                    .getReplacedText("color", "&" + channelColor.asHexString(), "prefixColor", "&" + channelPrefixColor.asHexString()));
        return ComponentUtils.parseColors(format);
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
        Component hover = getMessage("hover.info").getComponent();
        return HoverEvent.showText(ComponentUtils.parsePlaceholders(hover,
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
        return SettlementsPlugin.getInstance().getChat() != null ? StringUtils.parseColors(SettlementsPlugin.getInstance().getChat().getPlayerPrefix(sender)) : "";
    }

    public String getPlayerGroupPrefix(Player sender) {
        if (SettlementsPlugin.getInstance().getChat() == null) {
            return "";
        }
        String group = SettlementsPlugin.getInstance().getChat().getPrimaryGroup(sender);
        return SettlementsPlugin.getInstance().getChat().getGroupPrefix((String) null, group);
    }

    public String getPlayerSuffix(Player sender) {
        return SettlementsPlugin.getInstance().getChat() != null ? StringUtils.parseColors(SettlementsPlugin.getInstance().getChat().getPlayerSuffix(sender)) : "";
    }

    public String getPlayerGroupSuffix(Player sender) {
        if (SettlementsPlugin.getInstance().getChat() == null) {
            return "";
        }
        String group = SettlementsPlugin.getInstance().getChat().getPrimaryGroup(sender);
        return SettlementsPlugin.getInstance().getChat().getGroupSuffix((String) null, group);
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
