package de.themoep.settlements.village.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.village.Settlement;
import de.themoep.settlements.village.SettlementManager;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.perk.LevelPerk;
import de.themoep.settlements.perk.PerkData;
import de.themoep.settlements.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;

public class PerksGUI extends SettlementsGUI {
    private final static String[] SETUP = {
            "         ",
            "         ",
            "ggggggggg",
            "ggggggggg",
            "         ",
            "    b    "
    };

    private final Resident resident;
    private final InventoryGui gui;

    public PerksGUI(Resident resident) {
        super("perks");
        this.resident = resident;

        Settlement settlement = resident.getSettlement();

        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title"), SETUP);
        gui.setCloseAction(close -> false);
        gui.setFiller(BORDER_ITEM);

        GuiElementGroup group = new GuiElementGroup('g');

        for (LevelPerk perk : SettlementManager.getInstance().getPerks()) {
            if (settlement == null || !settlement.hasUnlocked(perk) || (!perk.hasEffects() && !perk.hasActions())) {
                continue;
            }

            group.addElement(new DynamicGuiElement('g', (h) -> {
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

        gui.addElement(new StaticGuiElement('b', ItemUtils.toIcon(Material.BARRIER), click -> {
            new SettingsGUI((Player) click.getEvent().getWhoClicked()).open();
            return true;
        }, getText("back")));
    }

    public void open() {
        gui.show(resident.getPlayer());
    }
}
