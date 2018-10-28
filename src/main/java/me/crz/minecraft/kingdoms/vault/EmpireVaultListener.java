package me.crz.minecraft.kingdoms.vault;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageGUI;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmpireVaultListener extends VillageGUI implements Listener {
    public EmpireVaultListener() {
        super("empirevault");
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
        Resident resident = ResidentManager.getInstance().getResident(player);
        if (resident.hasEmpire()) {
            deposit(contents, resident, player);
            EmpireManager.getInstance().checkLevelUp(resident.getVillage().getEmpire(), resident);
        }
    }

    private void deposit(ItemStack[] contents, Resident resident, Player player) {
        Map<Material, Long> fail = new HashMap<>();
        Empire empire = resident.getVillage().getEmpire();
        if (!empire.isMaxLevel()) {
            Map<String, Long> nextLevelReqs = Levels.getEmpireLevelRequirements(empire.getLevel() + 1);
            Map<String, Long> total = new HashMap<>();
            Map<String, Long> deposited = new HashMap<>();

            for (String type : nextLevelReqs.keySet()) {
                total.put(type, empire.getVault().getOrDefault(type, (long) 0));
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
                MessageUtils.tellListResidents(empire.getOnlineResidents(), this.buildSuccessDepositMessage(resident, player, deposited));
                empire.getVault().putAll(total);
                Utils.runAsync(() -> {
                    DatabaseManager.getInstance().saveEmpire(empire);
                    DatabaseManager.getInstance().logDeposit(empire, resident.getVillage(), resident, total);
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

    private boolean isVault(Inventory inv) {
        return inv.getName().equals(getText("title"));
    }

    private List<String> buildfailDepositMessage(Resident resident, Map<Material, Long> fail, String reason) {
        List<String> messages = new ArrayList<>();
        messages.add(getMessage("fail-deposit").getReplacedText("reason", reason));
        for (Map.Entry<Material, Long> entry : fail.entrySet()) {
            messages.add(getMessage("list-entry").getReplacedText(
                    "amount", String.valueOf(entry.getValue()),
                    "icon", resident.hasResourcePack() ? ConfigManager.getInstance().getIconRpMapping().getIcon(entry.getKey().name(), true) : "",
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
                "icon", resident.hasResourcePack() ? ConfigManager.getInstance().getIconRpMapping().getIcon(entry.getKey(), true) : "",
                "material", entry.getKey()));
        }
        return messages;
    }

}
