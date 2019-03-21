package me.crz.minecraft.kingdoms.integration;

import co.aikar.timings.Timing;
import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Events.PreShopCreationEvent;
import com.Acrobot.ChestShop.Events.ShopDestroyedEvent;
import com.Acrobot.ChestShop.Signs.ChestShopSign;
import com.Acrobot.ChestShop.Utils.uBlock;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.lock.LockProvider;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Pair;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import java.util.Arrays;
import java.util.Collection;

public class ChestShopIntegration implements LockProvider, ShopProvider, Listener {
    private final ChestShop chestShop;

    public ChestShopIntegration() {
        chestShop = (ChestShop) Bukkit.getPluginManager().getPlugin("ChestShop");
        KingdomsPlugin.getInstance().getLockProviders().add(this);
        KingdomsPlugin.getInstance().getShopProviders().add(this);
        Bukkit.getPluginManager().registerEvents(this, KingdomsPlugin.getInstance());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onShopCreation(SignChangeEvent event) {
        if (event.getPlayer().hasPermission("kingdoms.bypass.restriction.chestshop")
                || !Utils.isWorldClaimable(event.getBlock().getWorld())) {
            return;
        }
        if (ChestShopSign.isValidPreparedSign(event.getLines())) {
            OwnedLand land = LandManager.getInstance().getLand(event.getBlock().getLocation());
            if (land == null) {
                event.setCancelled(true);
                getError("not-allowed").tell(event.getPlayer());
            }
        }
    }

    public ChestShop getChestShop() {
        return chestShop;
    }

    @Override
    public String name() {
        return "chestshop";
    }

    @Override
    public void removeInChunks(Collection<? extends SimpleChunkLocation> chunks) {
        Utils.runSync(() -> {
            for (SimpleChunkLocation chunkLocation : chunks) {
                World world = Bukkit.getWorld(chunkLocation.getWorld());
                if (world != null) {
                    world.getChunkAtAsync(chunkLocation.getX(), chunkLocation.getZ()).thenAccept(this::removeInChunk);
                }
            }
        });
    }

    @Override
    public void removeInWorld(World world) {
        Utils.runSync(() -> {
            for (Chunk chunk : world.getLoadedChunks()) {
                removeInChunk(chunk);
            }
        });
    }

    private int removeInChunk(Chunk chunk) {
        int amount = 0;
        try (Timing ignored = KingdomsPlugin.getInstance().getAndStartTiming("ChestShop chunk shop removal")) {
            for (BlockState blockState : chunk.getTileEntities()) {
                if (blockState instanceof Sign && ChestShopSign.isValid((Sign) blockState)) {
                    Sign sign = (Sign) blockState;
                    Container container = uBlock.findConnectedContainer(sign);
                    new ShopDestroyedEvent(null, (Sign) sign.getBlock().getState(), container).callEvent();
                    Arrays.fill(sign.getLines(), "");
                    sign.update(true);
                    amount++;
                }
            }
        }
        return amount;
    }

    @Override
    public boolean isShop(Block block) {
        return ChestShopSign.isValid(block) || ChestShopSign.isShopBlock(block);
    }
}
