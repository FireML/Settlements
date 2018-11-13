package me.crz.minecraft.kingdoms.chat;

import com.google.common.collect.ImmutableMap;
import me.crz.minecraft.kingdoms.Base;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.ConfigMessages;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.UtilChat;
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

public class ChatManager extends Base implements ConfigMessages {
    private static ChatManager instance;

    private Map<UUID, UUID> lastMessage = new HashMap<>();

    private final static DecimalFormat BALANCE_FORMAT = new DecimalFormat("#,###.##");

    private ChatManager() {
        super();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void on(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) {
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
            channel = ChatChannel.GENERAL;
            if (res.setActiveChannel(channel)) {
                Utils.runAsync(() -> DatabaseManager.getInstance().saveResident(res));
            }
        }

        if (!channel.canChatM(sender)) {
            return;
        }

        String senderPrefix = MessageUtils.replaceColorCodes(KingdomsPlugin.chat.getPlayerPrefix(sender));
        String senderSuffix = MessageUtils.replaceColorCodes(KingdomsPlugin.chat.getPlayerSuffix(sender));

        event.setCancelled(true);

        if (event.getPlayer().hasPermission("kingdoms.chat.colour")) {
            event.setMessage(MessageUtils.replaceColorCodes(event.getMessage()));
        }

        Message format = getMessage("format.channel." + channel.getKey());
        Message.MessageReplacer replacer = format.getReplacer()
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
                .replace("message", UtilChat.getInstance().filterCurses(event.getMessage()))
                .getComponents();
        BaseComponent[] unfilteredMessage = replacer
                .replace("message", event.getMessage())
                .getComponents();

        ChatChannel finalChannel = channel;
        switch (channel) {
            case TRADE:
            case GENERAL:
                if (Data.muteChat && !event.getPlayer().hasPermission("kingdoms.staff")) {
                    MessageUtils.tell(event.getPlayer(), getMessage("locked"), "channel", channel.name());
                    return;
                }

                Bukkit.getOnlinePlayers().stream()
                        .map(p -> ResidentManager.getInstance().getResident(p))
                        .filter(r -> r.isListening(finalChannel)
                                && !(r.getIgnore().contains(sender.getUniqueId()) && !sender.hasPermission("kingdoms.bypass.ignore")))
                        .forEach(r -> r.tell(r.getSwear() ? unfilteredMessage : filteredMessage));
                break;
            case LOCAL:
                MessageUtils.tell(sender, unfilteredMessage);
                Bukkit.getOnlinePlayers().stream()
                        .filter(p -> !p.equals(sender)
                                && sender.getLocation().getWorld().equals(p.getWorld())
                                && sender.getLocation().distanceSquared(p.getLocation()) < 200 * 200)
                        .map(p -> ResidentManager.getInstance().getResident(p))
                        .filter(r -> r.isListening(finalChannel)
                                && !(r.getIgnore().contains(sender.getUniqueId()) && !sender.hasPermission("kingdoms.bypass.ignore")))
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
            case ALLY:
                village.getAllies().stream()
                        .flatMap(a -> a.getOnlineResidents().stream())
                        .distinct()
                        .filter(r -> r.isListening(finalChannel))
                        .forEach(r -> r.tell(r.getSwear() ? unfilteredMessage : filteredMessage));
                if (village.hasEmpire()) {
                    village.getEmpire().getAllies().stream()
                            .filter(a -> !village.isAlly(a))
                            .flatMap(a -> a.getOnlineResidents().stream())
                            .distinct()
                            .filter(r -> r.isListening(finalChannel))
                            .forEach(r -> r.tell(r.getSwear() ? unfilteredMessage : filteredMessage));
                }
            case EMPIRE:
                if (village.hasEmpire()) {
                    village.getEmpire().getMembers().stream()
                            .filter(a -> !village.isAlly(a) && !village.equals(a))
                            .flatMap(a -> a.getOnlineResidents().stream())
                            .filter(r -> r.isListening(finalChannel))
                            .forEach(r -> r.tell(r.getSwear() ? unfilteredMessage : filteredMessage));
                }
            case SETTLEMENT:
                village.getOnlineResidents().stream()
                        .filter(r -> r.isListening(finalChannel))
                        .forEach(r -> r.tell(r.getSwear() ? unfilteredMessage : filteredMessage));
                break;
        }
        toConsole(unfilteredMessage, event.getMessage());
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
        if (tres.getIgnore().contains(res.getUUID()) && !sender.hasPermission("kingdoms.bypass.ignore")) {
            MessageUtils.tell(sender, getMessage("ignored"), "name", target.getName());
            return;
        }
        HoverEvent senderHover = getHover(sender, res);
        HoverEvent targetHover = getHover(target, tres);

        MessageUtils.tell(sender, getMessage("format.private.sender"), ImmutableMap.of(
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
                "staff", ChatChannel.ADMIN.canChat(sender) ? getMessage("staff").getComponents() : new ComponentBuilder("").create()
        ));

        MessageUtils.tell(target, getMessage("format.private.target"), ImmutableMap.of(
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
                "staff", ChatChannel.ADMIN.canChat(sender) ? getMessage("staff").getComponents() : new ComponentBuilder("").create()
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
            if (pl != null && pl != sender && pl != target && pl.isOnline()) {
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
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, getMessage("hover.info").getReplacedComponents(
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
