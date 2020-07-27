package me.crz.minecraft.kingdoms.land.gui;

import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.landflags.BuildState;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class BuildGui extends PlotGUI {
    private CInventory gui;
    private final SimpleChunkLocation landLocation;

    public BuildGui(Player player, OwnedLand land) {
        super("plot.build");
        this.gui = new CInventory(getText("title"), InventorySize.SIX) {};

        for (int i = 0; i < InventorySize.TWO.getSize(); i++) {
            gui.addItem(i, BORDER);
        }

        landLocation = land.getLocation().copy();
        for (BuildState state : BuildState.values(BuildState.AppliesTo.LAND)) {
            if (!land.isPlot() && !state.appliesTo(BuildState.AppliesTo.get(land.getLandOwner()))) {
                continue;
            }

            gui.addItem(new ClickableItem() {
                @Override
                public ItemStack getItem(Player p) {
                    Map<String, String> replacements = Utils.toMap(
                            "state", state.getTag(),
                            "description", getMessage("build." + state.getKey()).getReplacedText("state", land.getLandOwner().getStateTag())
                    );
                    OwnedLand land = LandManager.getInstance().getLand(landLocation);
                    if (state == land.getBuildState()) {
                        if (land.isPlot() || state.appliesTo(BuildState.AppliesTo.get(land.getLandOwner()))) {
                            return new CItem(Material.LIME_WOOL)
                                    .setName(getMessage("selected-state").getReplacedText(replacements))
                                    .addLore(getMessage("description").getReplacedTextList(replacements))
                                    .build();
                        } else {
                            return new CItem(Material.WHITE_WOOL)
                                    .setName(getMessage("plot-state").getReplacedText(replacements))
                                    .addLore(getMessage("description").getReplacedTextList(replacements))
                                    .build();
                        }
                    } else if (state == land.getLandOwner().getBuildState() && !land.isPlot() && !land.getBuildState().appliesTo(BuildState.AppliesTo.get(land.getLandOwner()))) {
                        return new CItem(Material.LIGHT_BLUE_WOOL)
                                .setName(getMessage("inherited-state").getReplacedText(replacements))
                                .addLore(getMessage("description").getReplacedTextList(replacements))
                                .build();
                    }
                    return new CItem(Material.LIGHT_GRAY_WOOL)
                            .setName(getMessage("set-state").getReplacedText(replacements))
                            .addLore(getMessage("description").getReplacedTextList(replacements))
                            .build();
                }

                @Override
                public boolean click(Player p, ClickType clickType) {
                    OwnedLand land = LandManager.getInstance().getLand(landLocation);
                    if (state != land.getBuildState()) {
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

        for (int i = InventorySize.FOUR.getSize(); i < InventorySize.SIX.getSize(); i++) {
            gui.addItem(i, BORDER);
        }

        gui.addItem(InventorySize.FIVE.getSize() + 4, new ClickableItem() {
            public ItemStack getItem(final Player p) {
                return new CItem(Material.BARRIER).setName(getText("back")).build();
            }

            public boolean click(final Player p, final ClickType clickType) {
                new SettingsGUI(p);
                return true;
            }
        });
        gui.build(player);
    }
}
