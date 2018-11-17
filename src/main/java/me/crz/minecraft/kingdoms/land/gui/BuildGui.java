package me.crz.minecraft.kingdoms.land.gui;

import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.landflags.BuildState;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.stream.Collectors;

public class BuildGui extends PlotGUI {
    private static BuildGui instance;
    private CInventory gui;

    public BuildGui() {
        super("plot.build");
        gui = new CInventory(getText("title"), InventorySize.ONE) {};
        for (BuildState state : BuildState.values(BuildState.AppliesTo.LAND)) {
            gui.addItem(new ClickableItem() {
                @Override
                public ItemStack getItem(Player p) {
                    OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                    if (land != null) {
                        if (state == land.getBuildState()) {
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
                    OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                    if (land != null && state != land.getBuildState()) {
                        Resident resident = ResidentManager.getInstance().getResident(p);
                        if (land.isPlotOwner(p) || land.canAdmin(resident)) {
                            land.setBuildState(state);
                            MessageUtils.tell(p, Message.get("command.plot.build.success"),
                                    "state", state.getTag()
                            );
                            LandManager.getInstance().syncAndSave(land);
                            gui.build(p);
                            return true;
                        }
                    }
                    return false;
                }
            });
        }
        gui.addItem((gui.getMaxSlot() / 9) * 9 + 7, getPlotLinkItem(gui));
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
