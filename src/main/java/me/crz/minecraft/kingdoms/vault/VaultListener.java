package me.crz.minecraft.kingdoms.vault;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import me.crz.minecraft.kingdoms.utils.MessageUtils;

import java.util.Map;

import org.bukkit.Material;

import java.util.HashMap;

import org.bson.Document;
import org.bukkit.event.EventHandler;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Inventory;
import me.crz.minecraft.kingdoms.village.VillageState;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.Listener;

public class VaultListener implements Listener {
    public VaultListener() {
        super();
    }

    @EventHandler
    public void on(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();
        if (!this.isVault(inv)) {
            return;
        }
        ItemStack[] contents = inv.getContents();
        if (contents == null || contents.length == 0) {
            return;
        }
        Player player = (Player) event.getPlayer();
        Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        this.deposit(contents, village, player);
        if (this.canLevelUp(village)) {
            if (village.getLevel() == 5 && village.getState().equals(VillageState.VILLAGE)) {
                this.upgrade(village);
            } else {
                this.levelUp(village);
            }
        }
    }

    private void deposit(ItemStack[] contents, Village village, Player player) {
        Map<Material, Integer> fail = new HashMap<>();
        if (village.getLevel() != 10 || village.getState() != VillageState.KINGDOM) {
            HashMap<String, Integer> nextLevelReqs = Levels.getLevelMapFromInt(village.getLevel() + 1, village.getState());
            Map<String, Integer> total = new HashMap<>();
            Map<String, Integer> deposited = new HashMap<>();

            for (String type : nextLevelReqs.keySet()) {
                total.put(type, village.getVault().getOrDefault(type, 0));
            }

            for (ItemStack item : contents) {
                if (item != null) {
                    String material = item.getType().name();
                    if (nextLevelReqs.containsKey(material)) {
                        int required = nextLevelReqs.get(material);
                        if (required <= total.get(material)) {
                            player.getInventory().addItem(item);
                            this.add(fail, item.getType(), item.getAmount());
                        } else {
                            int totalAdd = item.getAmount() + total.get(material);
                            if (required - totalAdd >= 0) {
                                this.add(total, material, item.getAmount());
                                this.add(deposited, material, item.getAmount());
                            } else {
                                int rv = required - total.get(material);
                                int drv = item.getAmount() - rv;
                                this.add(total, material, item.getAmount() - drv);
                                this.add(deposited, material, item.getAmount() - drv);
                                ItemStack temp = item.clone();
                                temp.setAmount(drv);
                                player.getInventory().addItem(temp);
                                this.add(fail, temp.getType(), temp.getAmount());
                            }
                        }
                    } else {
                        player.getInventory().addItem(item);
                        this.add(fail, item.getType(), item.getAmount());
                    }
                }
            }
            if (!deposited.isEmpty()) {
                MessageUtils.tellListResidents(village.getResidents(), this.buildSuccessDepositMessage(player, deposited));
                village.getVault().putAll(total);
                Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(village));
            }
            if (!fail.isEmpty()) {
                MessageUtils.tellList(player, this.buildfailDepositMessage(fail, ""));
            }
            return;
        }
        if (contents.length > 0) {
            for (ItemStack item2 : contents) {
                if (item2 != null) {
                    this.add(fail, item2.getType(), item2.getAmount());
                    player.getInventory().addItem(item2);
                }
            }
            MessageUtils.tellList(player, this.buildfailDepositMessage(fail, " &r&c&lReason: Max Level Reached"));
        }
    }

    public <R> void add(Map<R, Integer> m, R mat, Integer add) {
        m.put(mat, m.getOrDefault(mat, 0) + add);
    }

    private boolean isVault(Inventory inv) {
        return inv.getName().equals(VaultInventory.inventoryName);
    }

    private List<String> buildfailDepositMessage(Map<Material, Integer> fail, String reason) {
        List<String> messages = new ArrayList<>();
        messages.add(String.valueOf(MessageUtils.FAIL_DEPOSIT_MESSAGE) + reason);
        for (Map.Entry<Material, Integer> entry : fail.entrySet()) {
            String message = MessageUtils.LIST_DEPOSIT_MESSAGE;
            if (message.contains("{amount}")) {
                message = message.replace("{amount}", (entry.getValue()).toString());
            }
            if (message.contains("{material}")) {
                message = message.replace("{material}", (entry.getKey()).name());
            }
            messages.add(ChatColor.translateAlternateColorCodes('&', message));
        }
        return messages;
    }

    private List<String> buildSuccessDepositMessage(Player p, Map<String, Integer> deposited) {
        List<String> messages = new ArrayList<>();
        messages.add("&6&l" + p.getName() + " &7&lhas deposited:");
        for (Map.Entry<String, Integer> entry : deposited.entrySet()) {
            String message = MessageUtils.LIST_DEPOSIT_MESSAGE;
            if (message.contains("{amount}")) {
                message = message.replace("{amount}", (entry.getValue()).toString());
            }
            if (message.contains("{material}")) {
                message = message.replace("{material}", (entry.getKey()));
            }
            messages.add(ChatColor.translateAlternateColorCodes('&', message));
        }
        return messages;
    }

    private void upgrade(Village village) {
        village.setState(VillageState.KINGDOM);
        village.setLevel(1);
        MessageUtils.tellResidents(village.getResidents(), MessageUtils.UPGRADE_SETTLEMENT_MESSAGE, "{state}", village.getState().toString());
        this.resetVaultAfterLevelUp(village);
    }

    private void levelUp(Village village) {
        village.levelUp();
        this.resetVaultAfterLevelUp(village);
        MessageUtils.tellResidents(village.getResidents(), MessageUtils.UPGRADE_MESSAGE, "{level}", Integer.toString(village.getLevel()));
        if (village.getState().equals(VillageState.KINGDOM)) {
            String unlock;
            switch (village.getLevel()) {
                case 4:
                    unlock = "Teleporter";
                    break;
                case 5:
                    unlock = "Regen 1 Buff";
                    break;
                case 6:
                    unlock = "Speed 1 Buff";
                    break;
                case 7:
                    unlock = "Regen 2 Buff";
                    break;
                case 8:
                    unlock = "Haste 1 Buff";
                    break;
                case 9:
                    unlock = "Haste 2 Buff";
                    break;
                case 10:
                    unlock = "World";
                    break;
                default:
                    return;
            }
            MessageUtils.tellResidents(village.getResidents(), "&6&l{unlock} &7&lhas been unlocked!", "{unlock}", unlock);
        }
    }

    private void resetVaultAfterLevelUp(Village village) {
        village.getVault().clear();
        Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(village));
    }

    private boolean canLevelUp(Village village) {
        int currentLevel = village.getLevel();
        if (currentLevel == 10 && village.getState() == VillageState.KINGDOM) {
            return false;
        }
        HashMap<String, Integer> nextLevelReqs = Levels.getLevelMapFromInt(currentLevel + 1, village.getState());
        if (nextLevelReqs == null) {
            return false;
        }
        Iterator<Map.Entry<String, Integer>> iterator = nextLevelReqs.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> pair = iterator.next();
            if (!village.getVault().containsKey(pair.getKey())) {
                return false;
            }
            int amountRequired = nextLevelReqs.get(pair.getKey());
            int amountInVault = village.getVault().get(pair.getKey());
            if (amountInVault < amountRequired) {
                return false;
            }
            if (!iterator.hasNext()) {
                return true;
            }
        }
        return false;
    }
}
