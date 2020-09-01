package me.crz.minecraft.kingdoms.village.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.PlotMode;
import me.crz.minecraft.kingdoms.perk.LevelPerk;
import me.crz.minecraft.kingdoms.perk.PerkData;
import me.crz.minecraft.kingdoms.utils.ItemUtils;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;

public class PerksGUI extends KingdomsGUI {
    private final static String[] SETUP = {
            "lllllllll",
            "lllllllll",
            "ggggggggg",
            "ggggggggg",
            "lllllllll",
            "llllbllll"
    };

    private final Resident resident;
    public InventoryGui gui;

    public PerksGUI(Resident resident) {
        super("perks");
        this.resident = resident;

        if (!resident.hasVillage()) {
            return;
        }

        Village village = resident.getVillage();

        gui = new InventoryGui(KingdomsPlugin.getInstance(), getText("title"), SETUP);
        gui.addElement(new StaticGuiElement('l', BORDER_ITEM, " "));

        GuiElementGroup group = new GuiElementGroup('g');

        for (LevelPerk perk : VillageManager.getInstance().getPerks()) {
            if (!village.hasUnlocked(perk) || (!perk.hasEffects() && !perk.hasActions())) {
                continue;
            }

            group.addElement(new DynamicGuiElement('g', (h) -> {
                if (resident.hasVillage()) {
                    PerkData perkData = village.getPerkData(perk);
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
                        for (String state : perk.getActionStates(resident.getVillage())) {
                            lore.add(getText("actions." + state, "name", perk.getTag()));
                        }
                    }

                    ItemStack itemStack = ItemUtils.setGlowing(new ItemStack(perk.getIcon()), true);
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
                                if (resident.hasVillage() && resident.hasPermission("village.perks.toggle")) {
                                    if (resident.getVillage().togglePerk(perk)) {
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

        gui.addElement(new StaticGuiElement('b', new ItemStack(Material.BARRIER), click -> {
            new SettingsGUI((Player) click.getEvent().getWhoClicked()).open();
            return true;
        }, getText("back")));
    }

    public void open() {
        if (gui != null) {
            gui.show(resident.getPlayer());
        }
    }
}
