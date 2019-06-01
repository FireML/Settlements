package me.crz.minecraft.kingdoms.vault;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import me.crz.minecraft.kingdoms.utils.MessageUtils;

import java.util.Map;

import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.KingdomsGUI;
import org.bukkit.Material;

import java.util.HashMap;

import org.bukkit.event.EventHandler;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Inventory;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.Listener;

public class VaultListener extends KingdomsGUI implements Listener {
    public VaultListener() {
        super("vault");
    }

    @EventHandler
    public void on(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();
        if (!this.isVault(event.getView())) {
            return;
        }
        ItemStack[] contents = inv.getContents();
        if (contents == null || contents.length == 0) {
            return;
        }
        Player player = (Player) event.getPlayer();
        Resident resident = ResidentManager.getInstance().getResident(player);
        if (resident.hasVillage()) {
            deposit(contents, resident, player);
            VillageManager.getInstance().checkLevelUp(resident.getVillage(), resident);
        }
    }

    private void deposit(ItemStack[] contents, Resident resident, Player player) {
        Map<Material, Long> fail = new HashMap<>();
        Village village = resident.getVillage();
        if (!village.isMaxLevel()) {
            Map<String, Long> nextLevelReqs = village.getNextLevelRequirements();
            Map<String, Long> total = new HashMap<>();
            Map<String, Long> deposited = new HashMap<>();

            for (String type : nextLevelReqs.keySet()) {
                total.put(type, village.getAmount(type));
            }

            for (ItemStack item : contents) {
                if (item != null) {
                    String material = item.getType().name();
                    if (nextLevelReqs.containsKey(material)) {
                        int required = Math.toIntExact(nextLevelReqs.get(material));
                        if (required <= total.get(material)) {
                            player.getInventory().addItem(item);
                            this.add(fail, item.getType(), (long) item.getAmount());
                        } else {
                            long totalAdd = item.getAmount() + total.get(material);
                            if (required - totalAdd >= 0) {
                                this.add(total, material, (long) item.getAmount());
                                this.add(deposited, material, (long) item.getAmount());
                            } else {
                                long rv = required - total.get(material);
                                long drv = item.getAmount() - rv;
                                this.add(total, material, item.getAmount() - drv);
                                this.add(deposited, material, item.getAmount() - drv);
                                ItemStack temp = item.clone();
                                temp.setAmount(Math.toIntExact(drv));
                                player.getInventory().addItem(temp);
                                this.add(fail, temp.getType(), (long) temp.getAmount());
                            }
                        }
                    } else {
                        player.getInventory().addItem(item);
                        this.add(fail, item.getType(), (long) item.getAmount());
                    }
                }
            }
            if (!deposited.isEmpty()) {
                MessageUtils.tellListResidents(village.getOnlineResidents(), this.buildSuccessDepositMessage(resident, player, deposited));
                village.add(deposited);
                Utils.runAsync(() -> {
                    DatabaseManager.getInstance().saveVillage(village);
                    DatabaseManager.getInstance().logDeposit(village, resident, deposited);
                });
            }
            if (!fail.isEmpty()) {
                MessageUtils.tellList(player, this.buildfailDepositMessage(resident, fail, ""));
            }
        } else if (contents.length > 0) {
            for (ItemStack item2 : contents) {
                if (item2 != null) {
                    add(fail, item2.getType(), (long) item2.getAmount());
                    player.getInventory().addItem(item2);
                }
            }
            MessageUtils.tellList(player, this.buildfailDepositMessage(resident, fail, getText("max-level-reached")));
        }
    }

    public <R> void add(Map<R, Long> m, R mat, Long add) {
        m.put(mat, m.getOrDefault(mat, (long) 0) + add);
    }

    private boolean isVault(InventoryView view) {
        return view.getTitle().equals(getText("title"));
    }

    private List<String> buildfailDepositMessage(Resident resident, Map<Material, Long> fail, String reason) {
        List<String> messages = new ArrayList<>();
        messages.add(getMessage("fail-deposit").getReplacedText("reason", reason));
        for (Map.Entry<Material, Long> entry : fail.entrySet()) {
            messages.add(getMessage("list-entry").getReplacedText(
                    "amount", String.valueOf(entry.getValue()),
                    "icon", Utils.getIcon(resident, entry.getKey().name()),
                    "material", entry.getKey().name()));
        }
        return messages;
    }

    private List<String> buildSuccessDepositMessage(Resident resident, Player p, Map<String, Long> deposited) {
        List<String> messages = new ArrayList<>();
        messages.add(getMessage("success-deposit").getReplacedText("player", p.getDisplayName()));
        for (Map.Entry<String, Long> entry : deposited.entrySet()) {
            messages.add(getMessage("list-entry").getReplacedText(
                "amount", String.valueOf(entry.getValue()),
                "icon", Utils.getIcon(resident, entry.getKey()),
                "material", entry.getKey()));
        }
        return messages;
    }

}
