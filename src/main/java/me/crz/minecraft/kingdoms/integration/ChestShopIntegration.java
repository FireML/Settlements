package me.crz.minecraft.kingdoms.integration;

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
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.Collection;

public class ChestShopIntegration implements LockProvider, Listener {
    private final ChestShop chestShop;

    public ChestShopIntegration() {
        chestShop = (ChestShop) Bukkit.getPluginManager().getPlugin("ChestShop");
        KingdomsPlugin.getInstance().getLockProviders().add(this);
        Bukkit.getPluginManager().registerEvents(this, KingdomsPlugin.getInstance());
    }

    @EventHandler
    public void onShopCreation(PreShopCreationEvent event) {
        if (event.getPlayer().hasPermission("kingdoms.bypass.restriction.chestshop")
                || !Utils.isWorldClaimable(event.getSign().getWorld())) {
            return;
        }
        OwnedLand land = LandManager.getInstance().getLand(event.getSign().getLocation());
        if (land == null) {
            event.setCancelled(true);
            getError("not-allowed").tell(event.getPlayer());
            return;
        }

        Pair<Boolean, String> response = getCanBuildAll(event.getPlayer(), event.getSign().getLocation());
        if (!response.getLeft()) {
            MessageUtils.tell(event.getPlayer(), response.getRight());
            event.setOutcome(PreShopCreationEvent.CreationOutcome.NO_PERMISSION_FOR_TERRAIN);
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
    public int removeInChunks(Collection<? extends SimpleChunkLocation> chunks) {
        int amount = 0;
        for (SimpleChunkLocation chunkLocation : chunks) {
            Chunk chunk = chunkLocation.getChunk();
            if (!chunk.isLoaded()) {
                chunk.load(false);
            }
            amount += removeInChunk(chunk);
        }
        return 0;
    }

    @Override
    public int removeInWorld(World world) {
        int amount = 0;
        for (Chunk chunk : world.getLoadedChunks()) {
            amount += removeInChunk(chunk);
        }
        return amount;
    }

    private int removeInChunk(Chunk chunk) {
        int amount = 0;
        for (BlockState blockState : chunk.getTileEntities()) {
            if (blockState instanceof Container && ChestShopSign.isShopBlock((Container) blockState)) {
                Block block = blockState.getBlock();
                Sign sign;
                while ((sign = uBlock.findAnyNearbyShopSign(block)) != null) {
                    new ShopDestroyedEvent(null, sign, (Container) blockState).callEvent();
                    Arrays.fill(sign.getLines(), "");
                    sign.update();
                    amount++;
                }
            }
        }
        return amount;
    }
}
