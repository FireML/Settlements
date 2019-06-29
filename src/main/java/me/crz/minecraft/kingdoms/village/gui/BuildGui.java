package me.crz.minecraft.kingdoms.village.gui;

import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.landflags.BuildState;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class BuildGui extends KingdomsGUI {
    private static BuildGui instance;
    private CInventory gui;

    private BuildGui() {
        super("build");
        gui = new CInventory(getText("title"), InventorySize.ONE) {};
        for (BuildState state : BuildState.values(BuildState.AppliesTo.SETTLEMENT)) {
            gui.addItem(new ClickableItem() {
                @Override
                public ItemStack getItem(Player p) {
                    Resident res = ResidentManager.getInstance().getResident(p);
                    if (res.hasVillage()) {
                        Map<String, String> replacements = Utils.toMap(
                                "state", state.getTag(),
                                "description", getMessage("build." + state.getKey()).getReplacedText("state", res.getVillage().getStateTag())
                        );
                        if (state == res.getVillage().getBuildState()) {
                            return new CItem(Material.LIME_WOOL)
                                    .setName(getMessage("selected-state").getReplacedText(replacements))
                                    .addLore(getMessage("description").getReplacedTextList(replacements))
                                    .build();
                        }
                        return new CItem(Material.LIGHT_GRAY_WOOL)
                                .setName(getMessage("set-state").getReplacedText(replacements))
                                .addLore(getMessage("description").getReplacedTextList(replacements))
                                .build();
                    }
                    return null;
                }

                @Override
                public boolean click(Player p, ClickType clickType) {
                    Resident res = ResidentManager.getInstance().getResident(p);
                    if (res.hasVillage() && state != res.getVillage().getBuildState() && res.hasPermission("village.build")) {
                        res.getVillage().setBuildState(state);
                        MessageUtils.tell(p, Message.get("command.village.build.success"),
                                "state", state.getTag()
                        );
                        Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(res.getVillage()));
                        gui.build(p);
                        return true;
                    }
                    return false;
                }
            });
        }
        gui.addItem((gui.getMaxSlot() / 9) * 9 + 8, new ClickableItem() {
            public ItemStack getItem(final Player p) {
                return new CItem(Material.BARRIER).setName(getText("back")).build();
            }

            public boolean click(final Player p, final ClickType clickType) {
                new SettingsGUI(p).open();
                return true;
            }
        });
    }

    public void open(final Player p) {
        gui.build(p);
    }

    synchronized public static BuildGui getInstance() {
        if (instance == null) {
            instance = new BuildGui();
        }
        return instance;
    }
}
