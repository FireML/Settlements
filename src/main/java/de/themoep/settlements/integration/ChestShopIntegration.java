package de.themoep.settlements.integration;

import co.aikar.timings.lib.MCTiming;
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
import com.Acrobot.ChestShop.Libs.Kyori.adventure.text.Component;
import com.Acrobot.ChestShop.Libs.Kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import com.Acrobot.ChestShop.Listeners.Economy.EconomyAdapter;
import com.Acrobot.ChestShop.Security;
import com.Acrobot.ChestShop.Signs.ChestShopSign;
import com.Acrobot.ChestShop.Utils.ItemUtil;
import com.Acrobot.ChestShop.Utils.uBlock;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.papermc.lib.PaperLib;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.lock.Lock;
import de.themoep.settlements.lock.LockManager;
import de.themoep.settlements.lock.LockProvider;
import de.themoep.settlements.utils.IdObject;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.village.VillageManager;
import de.themoep.settlements.world.SimpleChunkLocation;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static com.Acrobot.ChestShop.Signs.ChestShopSign.NAME_LINE;

public class ChestShopIntegration extends EconomyAdapter implements LockProvider, ShopProvider, Listener {
    private final ChestShop chestShop;

    private final Cache<String, Long> notificationCooldown;

    public ChestShopIntegration() {
        chestShop = (ChestShop) Bukkit.getPluginManager().getPlugin("ChestShop");
        notificationCooldown = CacheBuilder.newBuilder().expireAfterWrite(Config.Chestshop.NOTIFICATION_COOLDOWN, TimeUnit.SECONDS).build();
        SettlementsPlugin.getInstance().getLockProviders().add(this);
        SettlementsPlugin.getInstance().getShopProviders().add(this);
        SettlementsPlugin.getInstance().registerListener(this);

        try {
            if (Properties.VALID_PLAYERNAME_REGEXP.equals("^\\w+$")) {
                Properties.VALID_PLAYERNAME_REGEXP = "^([ksve]:(\\w+)?|\\w+)$";
            } else if (!Properties.VALID_PLAYERNAME_REGEXP.equals("^([ksve]:(\\w+)?|\\w+)$")) {
                SettlementsPlugin.getInstance().getLogger().log(Level.INFO, "Please make sure that `VALID_PLAYERNAME_REGEXP` in your ChestShop config.yml matches settlement IDs! E.g. \"^([ksve]:(\\w+)?|\\w+)$\"");
            }
        } catch (NoSuchFieldError ignored) {}
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
                    PaperLib.getChunkAtAsync(world, chunkLocation.getX(), chunkLocation.getZ()).thenAccept(this::removeInChunk);
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
        try (MCTiming ignored = SettlementsPlugin.getInstance().getAndStartTiming("ChestShop chunk shop removal")) {
            for (BlockState blockState : chunk.getTileEntities()) {
                if (blockState instanceof Sign && ChestShopSign.isValid((Sign) blockState)) {
                    Sign sign = (Sign) blockState;
                    Container container = uBlock.findConnectedContainer(sign);
                    Bukkit.getPluginManager().callEvent(new ShopDestroyedEvent(null, (Sign) sign.getBlock().getState(), container));
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
                    return VillageManager.getInstance().getFromId(IdObject.get(id));
                } else if ("e".equalsIgnoreCase(parts[0])) {
                    return EmpireManager.getInstance().getEmpireFromId(IdObject.get(id));
                }
            } catch (IllegalArgumentException e) {
                SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, "Invalid shop owner ID " + name);
            }
        } else if (parts.length == 2) {
            if ("s".equalsIgnoreCase(parts[0]) || "v".equalsIgnoreCase(parts[0]) || "k".equalsIgnoreCase(parts[0])) {
                return VillageManager.getInstance().getFromName(parts[1]);
            } else if ("e".equalsIgnoreCase(parts[0])) {
                return EmpireManager.getInstance().getEmpireFromName(parts[1]);
            }
        }
        return null;
    }

    private LandOwner getLandOwner(UUID uuid) {
        LandOwner landOwner = VillageManager.getInstance().getFromUuid(uuid);
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
                Resident resident = ResidentManager.getInstance().getResident(event.getPlayer());
                if (landOwner.hasPermission(resident, "chestshop.admin")) {
                    event.setOwnerAccount(createAccount(landOwner));
                } else {
                    event.setOutcome(PreShopCreationEvent.CreationOutcome.NO_PERMISSION);
                }
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void shopPermissionCheck(PreShopCreationEvent event) {
        String name = event.getSignLine(NAME_LINE);
        LandOwner landOwner = getLandOwner(name);
        if (landOwner != null) {
            Resident resident = ResidentManager.getInstance().getResident(event.getPlayer());
            if (!landOwner.hasPermission(resident, "chestshop.admin")) {
                event.setOutcome(PreShopCreationEvent.CreationOutcome.NO_PERMISSION);
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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMessage(PreTransactionEvent event) {
        switch (event.getTransactionOutcome()) {
            case SHOP_DOES_NOT_HAVE_ENOUGH_MONEY: {
                LandOwner landOwner = getLandOwner(event.getOwnerAccount().getUuid());
                if (landOwner != null && checkCoolDown(event)) {
                    sendMessage(landOwner, event.getOwnerAccount().getName(), getMessage("not-enough-money"),
                            event.getSign(), event.getExactPrice(), event.getStock(), "seller", event.getClient().getName());
                }
                break;
            }
            case NOT_ENOUGH_SPACE_IN_CHEST: {
                if (Properties.SHOW_MESSAGE_FULL_SHOP && !Properties.CSTOGGLE_TOGGLES_FULL_SHOP || !Toggle.isIgnoring(event.getOwnerAccount().getUuid())) {
                    LandOwner landOwner = getLandOwner(event.getOwnerAccount().getUuid());
                    if (landOwner != null && checkCoolDown(event)) {
                        sendMessage(landOwner, event.getOwnerAccount().getName(), getMessage("not-enough-space"),
                                event.getSign(), event.getExactPrice(), event.getStock(), "seller", event.getClient().getName());
                    }
                }
                break;
            }
            case NOT_ENOUGH_STOCK_IN_CHEST: {
                if (Properties.SHOW_MESSAGE_OUT_OF_STOCK && !Properties.CSTOGGLE_TOGGLES_OUT_OF_STOCK || !Toggle.isIgnoring(event.getOwnerAccount().getUuid())) {
                    LandOwner landOwner = getLandOwner(event.getOwnerAccount().getUuid());
                    if (landOwner != null && checkCoolDown(event)) {
                        sendMessage(landOwner, event.getOwnerAccount().getName(), getMessage("out-of-stock"),
                                event.getSign(), event.getExactPrice(), event.getStock(), "buyer", event.getClient().getName());
                    }
                }
                break;
            }
        }
    }

    private boolean checkCoolDown(PreTransactionEvent event) {
        String key = event.getTransactionOutcome() + "/" + event.getSign().getLocation();
        if (notificationCooldown.getIfPresent(key) != null) {
            return false;
        }

        notificationCooldown.put(key, System.currentTimeMillis());
        return true;
    }

    private void sendMessage(LandOwner<?, ?> landOwner, String playerName, Message rawMessage, Sign sign, BigDecimal price, ItemStack[] stock, String... replacements) {
        Location loc = sign.getLocation();
        Map<String, String> replacementMap = new LinkedHashMap<>();
        replacementMap.put("state", landOwner.getStateTag());
        replacementMap.put("price", Economy.formatBalance(price));
        replacementMap.put("world", loc.getWorld().getName());
        replacementMap.put("x", String.valueOf(loc.getBlockX()));
        replacementMap.put("y", String.valueOf(loc.getBlockY()));
        replacementMap.put("z", String.valueOf(loc.getBlockZ()));

        for (int i = 0; i + 1 < replacements.length; i+=2) {
            replacementMap.put(replacements[i], replacements[i + 1]);
        }

        for (Resident resident : landOwner.getResidents()) {
            if (!Toggle.isIgnoring(resident.getUUID()) && landOwner.hasPermission(resident, "chestshop.admin")) {
                Player player = resident.getPlayer();
                if (Properties.SHOWITEM_MESSAGE) {
                    replacementMap.put("item", "%item");
                    if (MaterialUtil.Show.sendMessage(player, playerName, new ChestShopMessage(rawMessage), stock, replacementMap)) {
                        return;
                    }
                }

                if (player != null) {
                    replacementMap.put("item", ItemUtil.getItemList(stock));
                    new ChestShopMessage(rawMessage).sendWithPrefix(player, replacementMap);
                } else if (playerName != null) {
                    replacementMap.put("item", ItemUtil.getItemList(stock));
                    ChestShop.sendBungeeMessage(playerName, new ChestShopMessage(rawMessage), replacementMap);
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
        if (event.wasHandled() || !event.getAmount().equals(BigDecimal.ZERO)) {
            return;
        }

        LandOwner landOwner = getLandOwner(event.getAccount());
        if (landOwner != null) {
            long amount = landOwner.getBank();
            if (!Config.Chestshop.ALLOW_SELLING_BELOW_UPKEEP) {
                amount -= landOwner.getUpkeep();
            }
            event.setAmount(BigDecimal.valueOf(Math.max(amount, 0)));
            event.setHandled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCurrencyCheck(CurrencyCheckEvent event) {
        if (!event.wasHandled() && !event.hasEnough()) {
            LandOwner landOwner = getLandOwner(event.getAccount());
            if (landOwner != null) {
                try {
                    long amount = event.getAmount().setScale(0, RoundingMode.UP).longValueExact();
                    if (!Config.Chestshop.ALLOW_SELLING_BELOW_UPKEEP) {
                        amount += landOwner.getUpkeep();
                    }
                    event.hasEnough(landOwner.has(amount));
                    event.setHandled(true);
                } catch (ArithmeticException e) {
                    SettlementsPlugin.getInstance().getLogger().log(Level.SEVERE, "Could not check balance of " + landOwner.getStateTag() + " " + landOwner.getName() + "! " + e.getMessage());
                    event.hasEnough(false);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onAccountCheck(AccountCheckEvent event) {
        if (event.wasHandled() || event.hasAccount()) {
            return;
        }

        LandOwner landOwner = getLandOwner(event.getAccount());
        if (landOwner != null) {
            event.hasAccount(true);
            event.setHandled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCurrencyFormat(CurrencyFormatEvent event) {
        // Ignored, formatting is forwarded to economy plugin
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCurrencyAdd(CurrencyAddEvent event) {
        if (event.wasHandled()) {
            return;
        }

        LandOwner landOwner = getLandOwner(event.getTarget());
        if (landOwner != null) {
            try {
                landOwner.addToBank(event.getAmount().setScale(0, RoundingMode.DOWN).longValueExact());
                event.setHandled(true);
                DatabaseManager.getInstance().save(landOwner);
            } catch (ArithmeticException e) {
                SettlementsPlugin.getInstance().getLogger().log(Level.SEVERE, "Could not add money to " + landOwner.getStateTag() + " " + landOwner.getName() + "! " + e.getMessage());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCurrencySubtraction(CurrencySubtractEvent event) {
        if (event.wasHandled()) {
            return;
        }

        LandOwner landOwner = getLandOwner(event.getTarget());
        if (landOwner != null) {
            try {
                landOwner.removeFromBank(event.getAmount().setScale(0, RoundingMode.UP).longValueExact());
                event.setHandled(true);
                DatabaseManager.getInstance().save(landOwner);
            } catch (ArithmeticException e) {
                SettlementsPlugin.getInstance().getLogger().log(Level.SEVERE, "Could not remove money from " + landOwner.getStateTag() + " " + landOwner.getName() + "! " + e.getMessage());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCurrencyTransfer(CurrencyTransferEvent event) {
        processTransfer(event);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCurrencyHoldCheck(CurrencyHoldEvent event) {
        if (event.wasHandled() || event.getAccount() == null || event.canHold()) {
            return;
        }

        LandOwner landOwner = getLandOwner(event.getAccount());
        if (landOwner != null) {
            event.canHold(true);
            event.setHandled(true);
        }
    }

    private class ChestShopMessage extends Messages.Message {
        private final Message message;

        public ChestShopMessage(Message message) {
            super(message.getRawText());
            this.message = message;
        }

        public BaseComponent[] getComponents(CommandSender sender, boolean prefixSuffix, Map<String, String> replacementMap, String... replacements) {
            return message.getReplacer().replace(replacementMap).replace(replacements).getComponents();
        }

        public Component getComponent(CommandSender sender, boolean prefixSuffix, Map<String, String> replacementMap, String... replacements) {
            return BungeeComponentSerializer.get().deserialize(getComponents(sender, prefixSuffix, replacementMap, replacements));
        }

    }
}
