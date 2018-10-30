package me.crz.minecraft.kingdoms.command.empire.settings;

import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.command.village.settings.SettingsGUI;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.empire.BuildState;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.KingdomsGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.stream.Collectors;

public class BuildGui extends KingdomsGUI {
    private static BuildGui instance;
    private CInventory gui;

    private BuildGui() {
        super("empire-build");
        gui = new CInventory(getText("title"), InventorySize.ONE) {};
        for (BuildState state : BuildState.values()) {
            gui.addItem(new ClickableItem() {
                @Override
                public ItemStack getItem(Player p) {
                    Resident res = ResidentManager.getInstance().getResident(p);
                    if (res.hasEmpire()) {
                        if (state == res.getVillage().getEmpire().getBuildState()) {
                            return new CItem(Material.LIME_WOOL)
                                    .setName(getMessage("selected-state").getReplacedText("state", state.getTag()))
                                    .setLore(getMessage("selected-state").getReplacedTextList("state", state.getTag()).stream().skip(1).collect(Collectors.toList()))
                                    .build();
                        }
                        return new CItem(Material.LIGHT_GRAY_WOOL)
                                .setName(getMessage("set-state").getReplacedText("state", state.getTag()))
                                .setLore(getMessage("set-state").getReplacedTextList("state", state.getTag()).stream().skip(1).collect(Collectors.toList()))
                                .build();
                    }
                    return null;
                }

                @Override
                public boolean click(Player p, ClickType clickType) {
                    Resident res = ResidentManager.getInstance().getResident(p);
                    if (res.hasEmpire() && state != res.getVillage().getEmpire().getBuildState() && res.getVillageRank().atLeast(VillageRank.CHIEF)) {
                        res.getVillage().getEmpire().setBuildState(state);
                        MessageUtils.tell(p, Message.get("command.empire.build.success"),
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
                SettingsGUI.getInstance().open(p);
                return true;
            }
        });
    }

    public void open(final Player p) {
        gui.build(p);
    }

    public static BuildGui getInstance() {
        if (instance == null) {
            instance = new BuildGui();
        }
        return instance;
    }
}
