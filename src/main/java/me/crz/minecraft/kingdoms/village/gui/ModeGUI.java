package me.crz.minecraft.kingdoms.village.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.PlotMode;
import me.crz.minecraft.kingdoms.utils.ItemUtils;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ModeGUI extends KingdomsGUI {
    private final static String[] SETUP = {
            "lllllllll",
            "lllllllll",
            "ggggggggg",
            "ggggggggg",
            "lllllllll",
            "llllbllll"
    };

    public InventoryGui gui;

    public ModeGUI() {
        super("mode");
        gui = new InventoryGui(KingdomsPlugin.getInstance(), getText("title"), SETUP);
        gui.addElement(new StaticGuiElement('l', BORDER_ITEM, " "));

        GuiElementGroup group = new GuiElementGroup('g');

        for (PlotMode m : PlotMode.values(PlotMode.AppliesTo.LAND_OWNER)) {
            group.addElement(new DynamicGuiElement('g', (h) -> {
                Resident resident = ResidentManager.getInstance().getResident((Player) h);
                if (resident.hasVillage()) {
                    boolean enabled = m.get(resident.getVillage());
                    return new StaticGuiElement('g',
                            ItemUtils.setGlowing(m.getItem(), enabled),
                            click -> {
                                if (resident.hasVillage() && resident.hasPermission("village.mode")) {
                                    boolean state = !m.get(resident.getVillage());
                                    if (m.set(state, resident.getVillage())) {
                                        Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(resident.getVillage()));
                                    }
                                    Message.get("command.village.mode.success").tell(resident,
                                            "mode", m.getTag(),
                                            "value", getText("entry.state." + state)
                                    );
                                    gui.draw();
                                }
                                return true;
                            },
                            getText("entry.title",
                                    "mode", m.getTag(),
                                    "value", getText("entry.state." + enabled)
                            ),
                            getText("entry.lore",
                                    "mode", m.getTag(),
                                    "value", getText("entry.state." + enabled)
                            )
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

    public void open(final Player p) {
        gui.show(p);
    }
}
