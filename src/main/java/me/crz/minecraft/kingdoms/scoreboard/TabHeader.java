package me.crz.minecraft.kingdoms.scoreboard;

import java.lang.reflect.Field;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerListHeaderFooter;
import net.minecraft.server.v1_12_R1.IChatBaseComponent;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import net.minecraft.server.v1_12_R1.Packet;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import java.util.Iterator;
import java.util.Collection;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import org.bukkit.Bukkit;

public class TabHeader
{
    private String header;
    private String footer;
    
    public TabHeader(final String header, final String footer) {
        super();
        header = header;
        footer = footer;
    }
    
    public void send() {
        this.send(new ArrayList<Player>(Bukkit.getOnlinePlayers()));
    }
    
    public void setFooter(final String footer) {
        this.footer = footer;
    }
    
    public void send(final Collection<Player> players) {
        for (final Player pl : players) {
            this.send(pl);
        }
    }
    
    public void send(final Player... players) {
        for (final Player pl : players) {
            this.send(pl);
        }
    }
    
    public void send(final Player p) {
        this.sendTabHeader(p, header, footer);
    }
    
    public void sendPacket(final Player p, final Object packet) {
        ((CraftPlayer)p).getHandle().playerConnection.sendPacket((Packet)packet);
    }
    
    public void sendTabHeader(final Player p, String header, String footer) {
        header = MessageUtils.replaceColorCodes(header);
        footer = MessageUtils.replaceColorCodes(footer);
        final IChatBaseComponent baseHeader = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + header + "\"}");
        final IChatBaseComponent baseFooter = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + footer + "\"}");
        final PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();
        try {
            final Field a = packet.getClass().getDeclaredField("a");
            a.setAccessible(true);
            a.set(packet, baseHeader);
            final Field b = packet.getClass().getDeclaredField("b");
            b.setAccessible(true);
            b.set(packet, baseFooter);
        }
        catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException ex2) {
            final Exception ex;
            final Exception e = ex;
            e.printStackTrace();
        }
        this.sendPacket(p, packet);
    }
}
