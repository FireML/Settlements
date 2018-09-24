package me.crz.minecraft.kingdoms.utils;

import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import java.util.Iterator;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import net.minecraft.server.v1_12_R1.IChatBaseComponent;
import net.minecraft.server.v1_12_R1.PacketPlayOutTitle;
import java.util.ArrayList;
import net.minecraft.server.v1_12_R1.Packet;
import java.util.List;

public class TitleBuilder
{
    private List<Packet<?>> packets;
    private boolean title;
    private boolean subtitle;
    
    public TitleBuilder() {
        super();
        packets = new ArrayList<Packet<?>>();
    }
    
    public TitleBuilder withTimings(final int in, final int stay, final int out) {
        packets.add((Packet<?>)new PacketPlayOutTitle(in, stay, out));
        return this;
    }
    
    public TitleBuilder withTitle(final String title) {
        final IChatBaseComponent message = IChatBaseComponent.ChatSerializer.a("{\"text\": \"\"}").a(title);
        packets.add((Packet<?>)new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, message));
        this.title = true;
        return this;
    }
    
    public TitleBuilder withSubtitle(final String subtitle) {
        final IChatBaseComponent message = IChatBaseComponent.ChatSerializer.a("{\"text\": \"\"}").a(subtitle);
        packets.add((Packet<?>)new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, message));
        this.subtitle = true;
        return this;
    }
    
    public TitleBuilder withActionbar(final String msg) {
        final IChatBaseComponent message = IChatBaseComponent.ChatSerializer.a("{\"text\": \"\"}").a(msg);
        packets.add((Packet<?>)new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.ACTIONBAR, message));
        return this;
    }
    
    public void send() {
        for (final Player pl : Bukkit.getOnlinePlayers()) {
            this.send(pl);
        }
    }
    
    public void send(final Player p) {
        if (!title) {
            this.withTitle("");
        }
        if (!subtitle) {
            this.withSubtitle("");
        }
        for (final Packet<?> packet : packets) {
            ((CraftPlayer)p).getHandle().playerConnection.sendPacket((Packet)packet);
        }
    }
}
