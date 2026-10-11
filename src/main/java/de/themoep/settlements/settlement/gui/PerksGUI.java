package de.themoep.settlements.settlement.gui;

import de.themoep.settlements.perk.PerkData;
import de.themoep.settlements.perk.SettlementPerk;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.utils.ConfigurableGui;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.utils.ItemUtils;

import java.util.ArrayList;
import java.util.List;

public class PerksGUI extends ConfigurableGui {

    public PerksGUI(Resident resident) {
        super(resident, "settlement.perks");

        Settlement settlement = resident.getSettlement();

        GuiElementGroup group = createGroup('p');

        for (SettlementPerk perk : SettlementManager.getInstance().getPerks()) {
            if (settlement == null || !settlement.hasUnlocked(perk) || (!perk.hasEffects() && !perk.hasActions())) {
                continue;
            }

            group.addElement(new DynamicGuiElement('p', (h) -> {
                if (resident.hasSettlement()) {
                    PerkData perkData = settlement.getPerkData(perk);
                    List<String> lore = new ArrayList<>();
                    lore.add(getText("entry", "name", perk.getTag()));
                    if (perk.hasEffects()) {
                        if (perkData.isActive()) {
                            lore.add(getText("active", "name", perk.getTag()));
                        } else {
                            lore.add(getText("inactive", "name", perk.getTag()));
                        }
                        lore.add(getText("toggleable", "name", perk.getTag()));
                    }

                    if (perk.hasActions()) {
                        for (String state : perk.getActionStates(resident.getSettlement())) {
                            lore.add(getText("actions." + state, "name", perk.getTag()));
                        }
                    }

                    ItemStack itemStack = ItemUtils.setGlowing(ItemStack.of(perk.getIcon()), true);
                    if (!perk.getEffects().isEmpty()) {
                        ItemMeta itemMeta = itemStack.getItemMeta();
                        if (itemMeta instanceof PotionMeta potionMeta) {
                            PotionEffect effect = perk.getEffects().get(0).getPotionEffect();
                            if (effect != null) {
                                potionMeta.addCustomEffect(effect, true);
                            }
                            itemStack.setItemMeta(itemMeta);
                        }
                    }

                    return new StaticGuiElement('g',
                            itemStack,
                            click -> {
                                if (resident.hasSettlement() && resident.hasPermission("settlement.perks.toggle")) {
                                    if (resident.getSettlement().togglePerk(resident.getPlayer(), perk)) {
                                        gui.draw();
                                        return true;
                                    }
                                }
                                return true;
                            },
                            lore.toArray(new String[0])
                    );
                }
                return null;
            }));
        }

        gui.addElement(group);

        gui.addElement(new GuiBackElement('b', getIcon("back"), true, getText("back")));
    }

    public void open() {
        gui.show(resident.getPlayer());
    }
}
