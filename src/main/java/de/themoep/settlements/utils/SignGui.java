package de.themoep.settlements.utils;

import de.themoep.settlements.SettlementsPlugin;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.SignChangeEvent;
import de.themoep.settlements.utils.ComponentUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SignGui {
    private static boolean registered = false;
    private static Map<Block, SignGui> signs = new HashMap<>();

    private final Consumer<String[]> inputConsumer;

    public SignGui(Consumer<String[]> inputConsumer) {
        this.inputConsumer = inputConsumer;
        if (!registered) {
            registered = true;
            SettlementsPlugin.getInstance().getServer().getPluginManager().registerEvents(new EventListener(), SettlementsPlugin.getInstance());
        }
    }

    public void open(Player player) {
        Block block = player.getLocation().getBlock();
        if (!block.isEmpty()) {
            block = block.getRelative(BlockFace.UP);
            if (!block.isEmpty()) {
                player.sendMessage(ComponentUtils.parseComponent("Unable to open sign editor."));
                return;
            }
        }
        signs.put(block, this);
        block.setType(Material.OAK_SIGN, false);
        for (Player p : player.getLocation().getNearbyPlayers(16)) {
            if (p != player) {
                p.sendBlockChange(block.getLocation(), Material.AIR.createBlockData());
            }
        }
        player.openSign((Sign) block.getState(), Side.FRONT);
    }

    public static class EventListener implements Listener {

        @EventHandler
        public void onSignChange(SignChangeEvent e) {
            SignGui gui = signs.get(e.getBlock());
            if (gui != null) {
                String[] strings = e.lines().stream().map(PlainTextComponentSerializer.plainText()::serialize).toArray(String[]::new);
                gui.inputConsumer.accept(strings);
                e.setCancelled(true);
                e.getBlock().setType(Material.AIR, false);
            }
        }

        @EventHandler
        public void onSignUpdate(BlockPhysicsEvent e) {
            if (signs.containsKey(e.getBlock()) || signs.containsKey(e.getSourceBlock())) {
                e.setCancelled(true);
            }
        }

        @EventHandler
        public void onSignBreak(BlockBreakEvent e) {
            if (signs.containsKey(e.getBlock())) {
                e.setCancelled(true);
            }
        }
    }

}
