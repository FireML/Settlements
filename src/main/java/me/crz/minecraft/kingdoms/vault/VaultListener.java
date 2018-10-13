package me.crz.minecraft.kingdoms.vault;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import me.crz.minecraft.kingdoms.utils.MessageUtils;

import java.util.Map;

import me.crz.minecraft.kingdoms.village.VillageGUI;
import org.bukkit.Material;

import java.util.HashMap;

import org.bukkit.event.EventHandler;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Inventory;
import me.crz.minecraft.kingdoms.village.VillageState;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.Listener;

public class VaultListener extends VillageGUI implements Listener {
    public VaultListener() {
        super("vault");
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
        if (village != null) {
            deposit(contents, village, player);
            VillageManager.getInstance().checkLevelUp(village);
        }
    }

    private void deposit(ItemStack[] contents, Village village, Player player) {
        Map<Material, Integer> fail = new HashMap<>();
        if (!village.isMaxLevel()) {
            Map<String, Integer> nextLevelReqs = Levels.getLevelMapFromInt(village.getLevel() + 1, village.getState());
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
                MessageUtils.tellListResidents(village.getOnlineResidents(), this.buildSuccessDepositMessage(player, deposited));
                village.getVault().putAll(total);
                Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(village));
            }
            if (!fail.isEmpty()) {
                MessageUtils.tellList(player, this.buildfailDepositMessage(fail, ""));
            }
        } else if (contents.length > 0) {
            for (ItemStack item2 : contents) {
                if (item2 != null) {
                    add(fail, item2.getType(), item2.getAmount());
                    player.getInventory().addItem(item2);
                }
            }
            MessageUtils.tellList(player, this.buildfailDepositMessage(fail, getText("max-level-reached")));
        }
    }

    public <R> void add(Map<R, Integer> m, R mat, Integer add) {
        m.put(mat, m.getOrDefault(mat, 0) + add);
    }

    private boolean isVault(Inventory inv) {
        return inv.getName().equals(getText("title"));
    }

    private List<String> buildfailDepositMessage(Map<Material, Integer> fail, String reason) {
        List<String> messages = new ArrayList<>();
        messages.add(getMessage("fail-deposit").getReplacedText("reason", reason));
        for (Map.Entry<Material, Integer> entry : fail.entrySet()) {
            messages.add(getMessage("list-deposit").getReplacedText(
                    "amount", String.valueOf(entry.getValue()),
                    "material", entry.getKey().name()));
        }
        return messages;
    }

    private List<String> buildSuccessDepositMessage(Player p, Map<String, Integer> deposited) {
        List<String> messages = new ArrayList<>();
        messages.add(getMessage("success-deposit").getReplacedText("player", p.getDisplayName()));
        for (Map.Entry<String, Integer> entry : deposited.entrySet()) {
            messages.add(getMessage("list-deposit").getReplacedText(
                "amount", String.valueOf(entry.getValue()),
                "material", entry.getKey()));
        }
        return messages;
    }

}
