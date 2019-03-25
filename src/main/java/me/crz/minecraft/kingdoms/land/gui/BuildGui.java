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
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class BuildGui extends PlotGUI {
    private CInventory gui;

    public BuildGui(OwnedLand land) {
        super("plot.build");
        gui = new CInventory(getText("title"), InventorySize.ONE) {};
        for (BuildState state : BuildState.values(BuildState.AppliesTo.LAND)) {
            if (land != null) {
                if (!land.isPlot() && !state.appliesTo(BuildState.AppliesTo.SETTLEMENT)) {
                    continue;
                }

                gui.addItem(new ClickableItem() {
                    @Override
                    public ItemStack getItem(Player p) {
                        Map<String, String> replacements = Utils.toMap(
                                "state", state.getTag(),
                                "description", getMessage("build." + state.getKey()).getReplacedText("state", land.getLandOwner().getStateTag())
                        );
                        if (state == land.getBuildState()) {
                            if (land.isPlot() || state.appliesTo(BuildState.AppliesTo.SETTLEMENT)) {
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
                        } else if (state == land.getLandOwner().getBuildState() && !land.isPlot() && !land.getBuildState().appliesTo(BuildState.AppliesTo.SETTLEMENT)) {
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

    public static void open(final Player p) {
        new BuildGui(LandManager.getInstance().getLand(p.getLocation())).gui.build(p);
    }
}
