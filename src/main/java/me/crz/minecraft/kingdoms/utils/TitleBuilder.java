package me.crz.minecraft.kingdoms.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TitleBuilder {
    private static final Cache<UUID, Long> lastActionBar = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).build();
    private Message title;
    private Message subtitle;
    private Message actionBar;
    private int in = 20;
    private int stay = 60;
    private int out = 20;

    public TitleBuilder withTimings(int in, int stay, int out) {
        return withIn(in).withStay(stay).withOut(out);
    }

    public TitleBuilder withIn(int in) {
        this.in = in;
        return this;
    }

    public TitleBuilder withStay(int stay) {
        this.stay = stay;
        return this;
    }

    public TitleBuilder withOut(int out) {
        this.out = out;
        return this;
    }

    public TitleBuilder withTitle(Message title) {
        this.title = title;
        return this;
    }

    public TitleBuilder withSubtitle(Message subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    public TitleBuilder withActionbar(Message msg) {
        this.actionBar = msg;
        return this;
    }

    public void send(String... replacements) {
        for (final Player pl : Bukkit.getOnlinePlayers()) {
            this.send(pl, replacements);
        }
    }

    public void send(Player p, String... replacements) {
        if ((title != null && title.exists() && !title.getText().isEmpty())
                || (subtitle != null && subtitle.exists() && !subtitle.getText().isEmpty())) {
            KingdomsPlugin.getInstance().getServerAdapter().sendTitle(
                    p,
                    title.getReplacedComponents(replacements),
                    subtitle.getReplacedComponents(replacements),
                    in, stay, out
            );
        }
        if (actionBar != null && actionBar.exists() && !actionBar.getText().isEmpty()) {
            long sendTime = System.currentTimeMillis();
            lastActionBar.put(p.getUniqueId(), sendTime);
            BaseComponent[] replacedActionBar = actionBar.getReplacedComponents(replacements);
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, replacedActionBar);
            Bukkit.getScheduler().runTaskLater(KingdomsPlugin.getInstance(), () -> {
                Long storedSend = lastActionBar.getIfPresent(p.getUniqueId());
                if (storedSend != null && storedSend == sendTime) {
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, replacedActionBar);
                }
            }, 2 * 20);
        }
    }

    public void send(Player p, Map<String, BaseComponent[]> replacements) {
        if ((title != null && title.exists() && !title.getText().isEmpty())
                || (subtitle != null && subtitle.exists() && !subtitle.getText().isEmpty())) {
            KingdomsPlugin.getInstance().getServerAdapter().sendTitle(
                    p,
                    title.getReplacedComponents(replacements),
                    subtitle.getReplacedComponents(replacements),
                    in, stay, out
            );
        }
        if (actionBar != null && actionBar.exists() && !actionBar.getText().isEmpty()) {
            long sendTime = System.currentTimeMillis();
            lastActionBar.put(p.getUniqueId(), sendTime);
            BaseComponent[] replacedActionBar = actionBar.getReplacedComponents(replacements);
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, replacedActionBar);
            Bukkit.getScheduler().runTaskLater(KingdomsPlugin.getInstance(), () -> {
                Long storedSend = lastActionBar.getIfPresent(p.getUniqueId());
                if (storedSend != null && storedSend == sendTime) {
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, replacedActionBar);
                }
            }, 2 * 20);
        }
    }
}
