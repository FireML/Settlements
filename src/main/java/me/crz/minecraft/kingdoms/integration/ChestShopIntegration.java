package me.crz.minecraft.kingdoms.integration;

import co.aikar.timings.Timing;
import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Commands.Toggle;
import com.Acrobot.ChestShop.Configuration.Messages;
import com.Acrobot.ChestShop.Configuration.Properties;
import com.Acrobot.ChestShop.Database.Account;
import com.Acrobot.ChestShop.Economy.Economy;
import com.Acrobot.ChestShop.Events.AccountAccessEvent;
import com.Acrobot.ChestShop.Events.AccountQueryEvent;
import com.Acrobot.ChestShop.Events.Economy.AccountCheckEvent;
import com.Acrobot.ChestShop.Events.Economy.CurrencyAddEvent;
import com.Acrobot.ChestShop.Events.Economy.CurrencyAmountEvent;
import com.Acrobot.ChestShop.Events.Economy.CurrencyCheckEvent;
import com.Acrobot.ChestShop.Events.Economy.CurrencyFormatEvent;
import com.Acrobot.ChestShop.Events.Economy.CurrencyHoldEvent;
import com.Acrobot.ChestShop.Events.Economy.CurrencySubtractEvent;
import com.Acrobot.ChestShop.Events.Economy.CurrencyTransferEvent;
import com.Acrobot.ChestShop.Events.PreShopCreationEvent;
import com.Acrobot.ChestShop.Events.PreTransactionEvent;
import com.Acrobot.ChestShop.Events.Protection.ProtectionCheckEvent;
import com.Acrobot.ChestShop.Events.ShopDestroyedEvent;
import com.Acrobot.ChestShop.Events.TransactionEvent;
import com.Acrobot.ChestShop.Listeners.Economy.EconomyAdapter;
import com.Acrobot.ChestShop.Security;
import com.Acrobot.ChestShop.Signs.ChestShopSign;
import com.Acrobot.ChestShop.Utils.uBlock;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.LandOwner;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.lock.Lock;
import me.crz.minecraft.kingdoms.lock.LockManager;
import me.crz.minecraft.kingdoms.lock.LockProvider;
import me.crz.minecraft.kingdoms.utils.IdObject;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Level;

import static com.Acrobot.ChestShop.Signs.ChestShopSign.NAME_LINE;

public class ChestShopIntegration extends EconomyAdapter implements LockProvider, ShopProvider, Listener {
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

    @EventHandler(priority = EventPriority.HIGH)
    public void onShopAccess(ProtectionCheckEvent event) {
        Lock lock = LockManager.getInstance().getLock(event.getBlock());
        if (lock != null && lock.canAccess(event.getPlayer())) {
            event.setResult(Event.Result.ALLOW);
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

    @Override
    public void renameWorld(String oldName, String newName) {
        // No need, ChestShop doesn't store shop information outside of signs
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

    @Override
    public boolean isLocked(Block block) {
        return isShop(block);
    }

    @Override
    public boolean canAccess(Player player, Block block) {
        return Security.canAccess(player, block);
    }

    private LandOwner getLandOwner(String name) {
        String[] parts = name.split(":", 3);

        if (parts.length > 2) {
            try {
                int id = Integer.parseInt(parts[2]);
                if ("s".equalsIgnoreCase(parts[0]) || "v".equalsIgnoreCase(parts[0]) || "k".equalsIgnoreCase(parts[0])) {
                    return VillageManager.getInstance().getVillageFromId(IdObject.get(id));
                } else if ("e".equalsIgnoreCase(parts[0])) {
                    return EmpireManager.getInstance().getEmpireFromId(IdObject.get(id));
                }
            } catch (IllegalArgumentException e) {
                KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Invalid shop owner ID " + name);
            }
        }
        return null;
    }

    private LandOwner getLandOwner(UUID uuid) {
        LandOwner landOwner = VillageManager.getInstance().getVillageFromUuid(uuid);
        if (landOwner == null) {
            landOwner = EmpireManager.getInstance().getEmpireFromUuid(uuid);
        }
        return landOwner;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreShopCreation(PreShopCreationEvent event) {
        if (event.getOwnerAccount() == null) {
            String name = event.getSignLine(NAME_LINE);
            LandOwner landOwner = getLandOwner(name);
            if (landOwner != null) {
                event.setOwnerAccount(createAccount(landOwner));
            } else if (name.length() > 1 && name.charAt(1) == ':') {
                // Short name is possible kingdoms sign creation, check if user has access of type
                Resident resident = ResidentManager.getInstance().getResident(event.getPlayer());
                switch (name.toLowerCase().charAt(0)) {
                    case 's':
                    case 'v':
                    case 'k':
                        if (resident.hasVillage() && resident.hasPermission("village.chestshop.admin")) {
                            event.setOwnerAccount(createAccount(resident.getVillage()));
                        }
                        break;
                    case 'e':
                        if (resident.hasEmpire() && resident.hasPermission("empire.chestshop.admin")) {
                            event.setOwnerAccount(createAccount(resident.getVillage().getEmpire()));
                        }
                        break;
                }
            }
            if (event.getOwnerAccount() != null) {
                event.setSignLine(NAME_LINE, event.getOwnerAccount().getShortName());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTransaction(TransactionEvent event) {
        if (event.getTransactionType() == TransactionEvent.TransactionType.BUY) {
            sendBuyMessage(event);
        } else {
            sendSellMessage(event);
        }
    }

    private void sendBuyMessage(TransactionEvent event) {
        Player player = event.getClient();

        if (Properties.SHOW_TRANSACTION_INFORMATION_OWNER && !Toggle.isIgnoring(event.getOwnerAccount().getUuid())) {
            LandOwner landOwner = getLandOwner(event.getOwnerAccount().getUuid());
            if (landOwner != null) {
                sendMessage(landOwner, event.getOwnerAccount().getName(), getMessage("bought-from-shop"),
                        event.getSign(), event.getExactPrice(), event.getStock(), "buyer", player.getName());
            }
        }
    }

    private void sendSellMessage(TransactionEvent event) {
        Player player = event.getClient();

        if (Properties.SHOW_TRANSACTION_INFORMATION_OWNER && !Toggle.isIgnoring(event.getOwnerAccount().getUuid())) {
            LandOwner landOwner = getLandOwner(event.getOwnerAccount().getUuid());
            if (landOwner != null) {
                sendMessage(landOwner, event.getOwnerAccount().getName(), getMessage("sold-to-shop"),
                        event.getSign(), event.getExactPrice(), event.getStock(), "seller", player.getName());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMessage(PreTransactionEvent event) {
        switch (event.getTransactionOutcome()) {
            case NOT_ENOUGH_SPACE_IN_CHEST:
                if (Properties.SHOW_MESSAGE_FULL_SHOP && !Properties.CSTOGGLE_TOGGLES_FULL_SHOP || !Toggle.isIgnoring(event.getOwnerAccount().getUuid())) {
                    LandOwner landOwner = getLandOwner(event.getOwnerAccount().getUuid());
                    if (landOwner != null) {
                        sendMessage(landOwner, event.getOwnerAccount().getName(), getMessage("not-enough-space"),
                                event.getSign(), event.getExactPrice(), event.getStock(), "seller", event.getClient().getName());
                    }
                }
                break;
            case NOT_ENOUGH_STOCK_IN_CHEST:
                if (Properties.SHOW_MESSAGE_OUT_OF_STOCK && !Properties.CSTOGGLE_TOGGLES_OUT_OF_STOCK || !Toggle.isIgnoring(event.getOwnerAccount().getUuid())) {
                    LandOwner landOwner = getLandOwner(event.getOwnerAccount().getUuid());
                    if (landOwner != null) {
                        sendMessage(landOwner, event.getOwnerAccount().getName(), getMessage("out-of-stock"),
                                event.getSign(), event.getExactPrice(), event.getStock(), "buyer", event.getClient().getName());
                    }
                }
                break;
        }
    }

    private void sendMessage(LandOwner landOwner, String playerName, Message rawMessage, Sign sign, BigDecimal price, ItemStack[] stock, String... replacements) {
        Location loc = sign.getLocation();
        String message = Messages.prefix(rawMessage.getReplacer()
                .replace(replacements)
                .replace("state", landOwner.getStateTag(),
                        "item", "%item",
                        "price", Economy.formatBalance(price),
                        "world", loc.getWorld().getName(),
                        "x", String.valueOf(loc.getBlockX()),
                        "y", String.valueOf(loc.getBlockY()),
                        "z", String.valueOf(loc.getBlockZ())
                ).getText());

        String itemList = MaterialUtil.getItemList(stock);

        for (Resident resident : landOwner.getResidents()) {
            if (landOwner.hasPermission(resident, "chestshop.admin")) {
                Player player = resident.getPlayer();
                if (player != null) {
                    if (Properties.SHOWITEM_MESSAGE && MaterialUtil.Show.sendMessage(player, message, stock)) {
                        return;
                    }
                    player.sendMessage(message.replace("%item", itemList));
                } else if (playerName != null) {
                    ChestShop.sendBungeeMessage(playerName, message.replace("%item", itemList));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onAccountQuery(AccountQueryEvent event) {
        if (event.getAccount() == null) {
            LandOwner landOwner = getLandOwner(event.getName());
            if (landOwner != null) {
                event.setAccount(createAccount(landOwner));
            } else {
                KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Could not get shop owner from ID " + event.getName());
            }
        }
    }

    private static Account createAccount(LandOwner landOwner) {
        return new Account(landOwner.getStateTag() + " " + landOwner.getName(), getShortName(landOwner), landOwner.getUUID());
    }

    private static String getShortName(LandOwner landOwner) {
        String shortName = landOwner.getStateKey().charAt(0) + ":" + landOwner.getName() + ":" + landOwner.getId();
        String noSpaceName = landOwner.getName().replace(" ", "").replace("-", "").replace("_", "");
        if (shortName.length() > 15) {
            shortName = landOwner.getStateKey().charAt(0) + ":" + noSpaceName + ":" + landOwner.getId();
        }
        if (shortName.length() > 15) {
            shortName = landOwner.getStateKey().charAt(0) + ":" + noSpaceName.substring(0, noSpaceName.length() - (shortName.length() - 15)) + ":" + landOwner.getId();
        }

        return shortName;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onAccountAccess(AccountAccessEvent event) {
        LandOwner landOwner = getLandOwner(event.getAccount().getShortName());
        if (landOwner != null) {
            event.setAccess(landOwner.hasPermission(ResidentManager.getInstance().getResident(event.getPlayer()), "chestshop.admin"));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onAmountCheck(CurrencyAmountEvent event) {
        if (!event.getAmount().equals(BigDecimal.ZERO)) {
            return;
        }

        LandOwner landOwner = getLandOwner(event.getAccount());
        if (landOwner != null) {
            event.setAmount(BigDecimal.valueOf(landOwner.getBank()));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCurrencyCheck(CurrencyCheckEvent event) {
        if (!event.hasEnough()) {
            LandOwner landOwner = getLandOwner(event.getAccount());
            if (landOwner != null) {
                try {
                    event.hasEnough(landOwner.has(event.getAmount().setScale(0, RoundingMode.UP).longValueExact()));
                } catch (ArithmeticException e) {
                    KingdomsPlugin.getInstance().getLogger().log(Level.SEVERE, "Could not check balance of " + landOwner.getStateTag() + " " + landOwner.getName() + "! " + e.getMessage());
                    event.hasEnough(false);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onAccountCheck(AccountCheckEvent event) {
        if (event.hasAccount()) {
            return;
        }

        LandOwner landOwner = getLandOwner(event.getAccount());
        if (landOwner != null) {
            event.hasAccount(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCurrencyFormat(CurrencyFormatEvent event) {
        // Ignored, formatting is forwarded to economy plugin
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCurrencyAdd(CurrencyAddEvent event) {
        if (event.isAdded()) {
            return;
        }

        LandOwner landOwner = getLandOwner(event.getTarget());
        if (landOwner != null) {
            try {
                landOwner.addToBank(event.getAmount().setScale(0, RoundingMode.DOWN).longValueExact());
                event.setAdded(true);
                Utils.runAsync(() -> DatabaseManager.getInstance().save(landOwner));
            } catch (ArithmeticException e) {
                KingdomsPlugin.getInstance().getLogger().log(Level.SEVERE, "Could not add money to " + landOwner.getStateTag() + " " + landOwner.getName() + "! " + e.getMessage());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCurrencySubtraction(CurrencySubtractEvent event) {
        if (event.isSubtracted()) {
            return;
        }

        LandOwner landOwner = getLandOwner(event.getTarget());
        if (landOwner != null) {
            try {
                landOwner.removeFromBank(event.getAmount().setScale(0, RoundingMode.UP).longValueExact());
                event.setSubtracted(true);
                Utils.runAsync(() -> DatabaseManager.getInstance().save(landOwner));
            } catch (ArithmeticException e) {
                KingdomsPlugin.getInstance().getLogger().log(Level.SEVERE, "Could not remove money from " + landOwner.getStateTag() + " " + landOwner.getName() + "! " + e.getMessage());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCurrencyTransfer(CurrencyTransferEvent event) {
        processTransfer(event);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCurrencyHoldCheck(CurrencyHoldEvent event) {
        if (event.getAccount() == null || event.canHold()) {
            return;
        }

        LandOwner landOwner = getLandOwner(event.getAccount());
        if (landOwner != null) {
            event.canHold(true);
        }
    }
}
