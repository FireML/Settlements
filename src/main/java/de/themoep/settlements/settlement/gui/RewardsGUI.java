package de.themoep.settlements.settlement.gui;

import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.event.EventManager;
import de.themoep.settlements.event.Reward;
import de.themoep.settlements.settlement.Settlement;
import org.bukkit.entity.Player;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;

import java.util.Collections;
import java.util.Set;

public class RewardsGUI extends SettlementsGUI {
    private final InventoryGui gui;

    public RewardsGUI(Settlement settlement) {
        super("rewards");
        Set<String> rewards = EventManager.getInstance().getRewards(settlement);

        String[] setup = Collections.nCopies((rewards.size() - 1) / 9 + 1, "ggggggggg").toArray(new String[0]);
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title"), setup);
        gui.setCloseAction(close -> false);

        GuiElementGroup group = createGroup('g');

        for (String rewardId : rewards) {
            Reward reward = EventManager.getInstance().getReward(rewardId);
            if (reward == null) {
                continue;
            }
            group.addElement(new StaticGuiElement('g', reward.getIcon(), click -> {
                if (rewards.contains(rewardId)) {
                    rewards.remove(rewardId);
                    EventManager.getInstance().giveReward(reward, (Player) click.getWhoClicked(), settlement);
                    new RewardsGUI(settlement).open((Player) click.getWhoClicked());
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
