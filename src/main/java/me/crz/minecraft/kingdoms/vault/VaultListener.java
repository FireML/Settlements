package me.crz.minecraft.kingdoms.vault;

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

public class VaultListener implements Listener
{
    public VaultListener() {
        super();
    }
    
    @EventHandler
    public void on(final InventoryCloseEvent event) {
        final Inventory inv = event.getInventory();
        if (!this.isVault(inv)) {
            return;
        }
        final ItemStack[] contents = inv.getContents();
        if (contents == null || contents.length == 0) {
            return;
        }
        final Player player = (Player)event.getPlayer();
        final Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        this.deposit(contents, village, player);
        if (this.canLevelUp(village)) {
            if (village.getLevel() == 5 && village.getState().equals(VillageState.VILLAGE)) {
                this.upgrade(village);
            }
            else {
                this.levelUp(village);
            }
        }
    }
    
    private void deposit(final ItemStack[] contents, final Village village, final Player player) {
        Document doc = (Document)village.getDocument().get((Object)"vault");
        if (doc == null) {
            doc = new Document();
            village.set("vault", doc);
            VillageManager.getInstance().saveVillage(village);
        }
        final Map<Material, Integer> fail = new HashMap<Material, Integer>();
        if (village.getLevel() != 10 || village.getState() != VillageState.KINGDOM) {
            final HashMap<String, Integer> nextLevelReqs = Levels.getLevelMapFromInt(village.getLevel() + 1, village.getState());
            final Map<Material, Integer> total = new HashMap<Material, Integer>();
            final Map<Material, Integer> deposited = new HashMap<Material, Integer>();
            for (final Map.Entry<String, Integer> entry : nextLevelReqs.entrySet()) {
                if (doc.containsKey((Object)entry.getKey())) {
                    total.put(Material.valueOf((String)entry.getKey()), doc.getInteger((Object)entry.getKey()));
                }
                else {
                    total.put(Material.valueOf((String)entry.getKey()), 0);
                }
            }
            for (final ItemStack item : contents) {
                if (item != null) {
                    final Material material = item.getType();
                    if (nextLevelReqs.containsKey(material.name())) {
                        final int required = (int)nextLevelReqs.get(material.name());
                        if (required <= total.get(material)) {
                            player.getInventory().addItem(new ItemStack[] { item });
                            this.add(fail, item.getType(), item.getAmount());
                        }
                        else {
                            final int totalAdd = item.getAmount() + total.get(material);
                            if (required - totalAdd >= 0) {
                                this.add(total, material, item.getAmount());
                                this.add(deposited, material, item.getAmount());
                            }
                            else {
                                final int rv = required - total.get(material);
                                final int drv = item.getAmount() - rv;
                                this.add(total, material, item.getAmount() - drv);
                                this.add(deposited, material, item.getAmount() - drv);
                                final ItemStack temp = item.clone();
                                temp.setAmount(drv);
                                player.getInventory().addItem(new ItemStack[] { temp });
                                this.add(fail, temp.getType(), temp.getAmount());
                            }
                        }
                    }
                    else {
                        player.getInventory().addItem(new ItemStack[] { item });
                        this.add(fail, item.getType(), item.getAmount());
                    }
                }
            }
            if (!deposited.isEmpty()) {
                MessageUtils.tellListResidents(village.getResidents(), this.buildSuccessDepositMessage(player, deposited));
                for (final Material mat : total.keySet()) {
                    this.save(village, mat, total.get(mat));
                }
            }
            if (!fail.isEmpty()) {
                MessageUtils.tellList(player, this.buildfailDepositMessage(fail, ""));
            }
            return;
        }
        if (contents.length > 0) {
            for (final ItemStack item2 : contents) {
                if (item2 != null) {
                    this.add(fail, item2.getType(), item2.getAmount());
                    player.getInventory().addItem(new ItemStack[] { item2 });
                }
            }
            MessageUtils.tellList(player, this.buildfailDepositMessage(fail, " &r&c&lReason: Max Level Reached"));
        }
    }
    
    public void add(final Map<Material, Integer> m, final Material mat, final Integer add) {
        if (m.containsKey(mat)) {
            m.put(mat, (int)add + (int)m.get(mat));
        }
        else {
            m.put(mat, add);
        }
    }
    
    private void save(final Village village, final Material material, final int amount) {
        Document doc = (Document)village.getDocument().get((Object)"vault");
        if (doc == null) {
            doc = new Document();
        }
        doc.put(material.name(), (Object)amount);
        village.getDocument().put("vault", (Object)doc);
        VillageManager.getInstance().saveVillage(village);
    }
    
    private boolean isVault(final Inventory inv) {
        return inv.getName().equals(VaultInventory.inventoryName);
    }
    
    private List<String> buildfailDepositMessage(final Map<Material, Integer> fail, final String reason) {
        final List<String> messages = new ArrayList<String>();
        messages.add(String.valueOf(MessageUtils.FAIL_DEPOSIT_MESSAGE) + reason);
        for (final Map.Entry<Material, Integer> entry : fail.entrySet()) {
            String message = MessageUtils.LIST_DEPOSIT_MESSAGE;
            if (message.contains("{amount}")) {
                message = message.replace("{amount}", ((Integer)entry.getValue()).toString());
            }
            if (message.contains("{material}")) {
                message = message.replace("{material}", ((Material)entry.getKey()).name());
            }
            messages.add(ChatColor.translateAlternateColorCodes('&', message));
        }
        return messages;
    }
    
    private List<String> buildSuccessDepositMessage(final Player p, final Map<Material, Integer> deposited) {
        final List<String> messages = new ArrayList<String>();
        messages.add("&6&l" + p.getName() + " &7&lhas deposited:");
        for (final Map.Entry<Material, Integer> entry : deposited.entrySet()) {
            String message = MessageUtils.LIST_DEPOSIT_MESSAGE;
            if (message.contains("{amount}")) {
                message = message.replace("{amount}", ((Integer)entry.getValue()).toString());
            }
            if (message.contains("{material}")) {
                message = message.replace("{material}", ((Material)entry.getKey()).name());
            }
            messages.add(ChatColor.translateAlternateColorCodes('&', message));
        }
        return messages;
    }
    
    private void upgrade(final Village village) {
        village.setState(VillageState.KINGDOM);
        village.setLevel(1);
        VillageManager.getInstance().saveVillage(village);
        MessageUtils.tellResidents(village.getResidents(), MessageUtils.UPGRADE_SETTLEMENT_MESSAGE, "{state}", village.getState().toString());
        this.resetVaultAfterLevelUp(village);
    }
    
    private void levelUp(final Village village) {
        village.levelUp();
        VillageManager.getInstance().saveVillage(village);
        this.resetVaultAfterLevelUp(village);
        MessageUtils.tellResidents(village.getResidents(), MessageUtils.UPGRADE_MESSAGE, "{level}", Integer.toString(village.getLevel()));
        if (village.getState().equals(VillageState.KINGDOM)) {
            String unlock = "-";
            if (village.getLevel() == 4) {
                unlock = "Teleporter";
            }
            if (village.getLevel() == 5) {
                unlock = "Regen 1 Buff";
            }
            if (village.getLevel() == 6) {
                unlock = "Speed 1 Buff";
            }
            if (village.getLevel() == 7) {
                unlock = "Regen 2 Buff";
            }
            if (village.getLevel() == 8) {
                unlock = "Haste 1 Buff";
            }
            if (village.getLevel() == 9) {
                unlock = "Haste 2 Buff";
            }
            if (village.getLevel() == 10) {
                unlock = "World";
            }
            if (unlock != "-") {
                MessageUtils.tellResidents(village.getResidents(), "&6&l{unlock} &7&lhas been unlocked!", "{unlock}", unlock);
            }
        }
    }
    
    private void resetVaultAfterLevelUp(final Village village) {
        village.getDocument().put("vault", (Object)new Document());
        VillageManager.getInstance().saveVillage(village);
    }
    
    private boolean canLevelUp(final Village village) {
        Document doc = (Document)village.getDocument().get((Object)"vault");
        if (doc == null) {
            doc = new Document();
            village.set("vault", doc);
            VillageManager.getInstance().saveVillage(village);
        }
        final int currentLevel = village.getLevel();
        if (currentLevel == 10 && village.getState() == VillageState.KINGDOM) {
            return false;
        }
        final HashMap<String, Integer> nextLevelReqs = Levels.getLevelMapFromInt(currentLevel + 1, village.getState());
        final Iterator<Map.Entry<String, Integer>> iterator = nextLevelReqs.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry pair = (Map.Entry<String, Integer>)iterator.next();
            if (!doc.containsKey(pair.getKey())) {
                return false;
            }
            final int amountRequired = (int)nextLevelReqs.get(pair.getKey());
            final int amountInVault = (int)doc.getInteger(pair.getKey());
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
