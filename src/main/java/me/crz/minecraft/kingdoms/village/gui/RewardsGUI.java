package me.crz.minecraft.kingdoms.village.gui;

import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.event.EventManager;
import me.crz.minecraft.kingdoms.event.Reward;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Set;

public class RewardsGUI extends KingdomsGUI {
    private InventoryGui gui;

    public RewardsGUI(Village village) {
        super("rewards");
        Set<String> rewards = EventManager.getInstance().getRewards(village);

        String[] setup = Collections.nCopies((rewards.size() - 1) / 9 + 1, "ggggggggg").toArray(new String[0]);
        gui = new InventoryGui(KingdomsPlugin.getInstance(), getText("title"), setup);

        GuiElementGroup group = new GuiElementGroup('g');

        for (String rewardId : rewards) {
            Reward reward = EventManager.getInstance().getReward(rewardId);
            if (reward == null) {
                continue;
            }
            group.addElement(new StaticGuiElement('g', reward.getIcon(), click -> {
                if (rewards.contains(rewardId)) {
                    rewards.remove(rewardId);
                    EventManager.getInstance().giveReward(reward, (Player) click.getEvent().getWhoClicked(), village);
                    new RewardsGUI(village).open((Player) click.getEvent().getWhoClicked());
                }
                return true;
            }, getText("entry", "reward", reward.getName())));
        }

        gui.addElement(group);
    }

    public void open(final Player p) {
        gui.show(p);
    }
}
