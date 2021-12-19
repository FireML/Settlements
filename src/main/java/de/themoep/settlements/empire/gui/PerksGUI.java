package de.themoep.settlements.empire.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.perk.EmpirePerk;
import de.themoep.settlements.perk.PerkData;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.ConfigurableGui;
import de.themoep.settlements.utils.ItemUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;

public class PerksGUI extends ConfigurableGui {
    public PerksGUI(Resident resident) {
        super(resident, "empire.perks");

        Empire empire = resident.hasEmpire() ? resident.getSettlement().getEmpire() : null;

        GuiElementGroup group = createGroup('p');

        for (EmpirePerk perk : EmpireManager.getInstance().getPerks()) {
            if (empire == null || !empire.hasUnlocked(perk) || (!perk.hasEffects() && !perk.hasActions())) {
                continue;
            }

            group.addElement(new DynamicGuiElement('p', (h) -> {
                if (resident.hasEmpire()) {
                    PerkData perkData = resident.getSettlement().getEmpire().getPerkData(perk);
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
                        for (String state : perk.getActionStates(resident.getSettlement().getEmpire())) {
                            lore.add(getText("actions." + state, "name", perk.getTag()));
                        }
                    }

                    ItemStack itemStack = ItemUtils.setGlowing(ItemUtils.toIcon(perk.getIcon()), true);
                    if (!perk.getEffects().isEmpty()) {
                        ItemMeta itemMeta = itemStack.getItemMeta();
                        if (itemMeta instanceof PotionMeta) {
                            PotionType type = PotionType.getByEffect(perk.getEffects().get(0).getType());
                            if (type != null) {
                                ((PotionMeta) itemMeta).setBasePotionData(new PotionData(type));
                            } else {
                                ((PotionMeta) itemMeta).setMainEffect(perk.getEffects().get(0).getType());
                            }
                            itemStack.setItemMeta(itemMeta);
                        }
                    }

                    return new StaticGuiElement('p',
                            itemStack,
                            click -> {
                                if (resident.hasEmpire() && resident.hasPermission("empire.perks.toggle")) {
                                    if (resident.getSettlement().getEmpire().togglePerk(resident.getPlayer(), perk)) {
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
