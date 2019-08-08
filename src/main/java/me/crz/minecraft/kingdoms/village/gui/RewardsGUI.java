package me.crz.minecraft.kingdoms.village.gui;

import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.event.EventManager;
import me.crz.minecraft.kingdoms.event.Reward;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class RewardsGUI extends KingdomsGUI {
    private static RewardsGUI instance;
    private CInventory gui;

    public RewardsGUI(Village village) {
        super("rewards");
        Set<String> rewards = EventManager.getInstance().getRewards(village);
        InventorySize size = InventorySize.getMatchingSize(rewards.size());
        if (size == null) {
            size = InventorySize.SIX;
        }
        gui = new CInventory(getText("title"), size) {};

        for (String rewardId : rewards) {
            Reward reward = EventManager.getInstance().getReward(rewardId);
            if (reward == null) {
                continue;
            }
            gui.addItem(new ClickableItem() {
                @Override
                public ItemStack getItem(Player p) {
                    if (rewards.contains(rewardId)) {
                        return new CItem(reward.getIcon())
                                .setName(getMessage("entry").getReplacedText("reward", reward.getName()))
                                .build();
                    } else {
                        return null;
                    }
                }

                @Override
                public boolean click(Player p, ClickType clickType) {
                    if (rewards.contains(rewardId)) {
                        rewards.remove(rewardId);
                        EventManager.getInstance().giveReward(reward, p, village);
                        gui.build(p);
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        }
    }

    public void open(final Player p) {
        gui.build(p);
    }
}
