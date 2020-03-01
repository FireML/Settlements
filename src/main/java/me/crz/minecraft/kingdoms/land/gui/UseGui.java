package me.crz.minecraft.kingdoms.land.gui;

import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.landflags.Use;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.landflags.BuildState;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class UseGui extends PlotGUI {
    private final SimpleChunkLocation landLocation;
    private CInventory gui;

    public UseGui(Player player, OwnedLand land) {
        super("plot.use");
        this.landLocation = land.getLocation().copy();
        gui = new CInventory(getText("title"), InventorySize.FOUR) {};
        for (Use u : Use.values()) {
            gui.addItem(new ClickableItem() {
                public ItemStack getItem(Player p) {
                    OwnedLand land = LandManager.getInstance().getLand(landLocation);
                    if (land != null) {
                        BuildState state = land.getUseState(u);
                        if (!land.isPlot() && !state.appliesTo(BuildState.AppliesTo.get(land.getLandOwner()))) {
                            state = land.getLandOwner().getDefaultBuildState();
                        }
                        CItem item = u.getItem().setName(getMessage("entry").getReplacedText(
                                "flag", u.getTag(),
                                "state", state.getTag()
                        ));
                        if (land.hasUseState(u)) {
                            item.addLore(getMessage("entry.click-to-unset").getReplacedTextList(
                                    "parent", land.getLandOwner().getStateTag(),
                                    "value", land.getLandOwner().getUseState(u).getTag()
                            ));
                        } else {
                            item.addLore(getMessage("entry.parent").getReplacedTextList(
                                    "parent", land.getLandOwner().getStateTag(),
                                    "value", land.getLandOwner().getUseState(u).getTag()
                            ));
                        }
                        return item.build();
                    }
                    return null;
                }

                public boolean click(Player p, ClickType clickType) {
                    OwnedLand land = LandManager.getInstance().getLand(landLocation);
                    if (land != null) {
                        Resident resident = ResidentManager.getInstance().getResident(p);
                        if (land.isPlotOwner(p) || land.canAdmin(resident)) {
                            if (clickType == ClickType.MIDDLE) {
                                if (land.unsetUseState(u)) {
                                    MessageUtils.tell(p, Message.get("command.plot.use.unset"),
                                            "flag", u.getTag(),
                                            "parent", land.getLandOwner().getStateTag(),
                                            "state", land.getLandOwner().getUseState(u).getTag()
                                    );
                                } else {
                                    return false;
                                }
                            } else {
                                BuildState state = land.getUseState(u);
                                if (!land.isPlot() && !state.appliesTo(BuildState.AppliesTo.get(land.getLandOwner()))) {
                                    state = land.getLandOwner().getDefaultBuildState();
                                }
                                BuildState changeTo = clickType.isLeftClick()
                                        ? state.next(land.isPlot() ? BuildState.AppliesTo.LAND : BuildState.AppliesTo.get(land.getLandOwner()))
                                        : state.previous(land.isPlot() ? BuildState.AppliesTo.LAND : BuildState.AppliesTo.get(land.getLandOwner()));
                                land.setUseState(u, changeTo);
                                MessageUtils.tell(p, Message.get("command.plot.use.success"),
                                        "flag", u.getTag(),
                                        "state", changeTo.getTag()
                                );
                            }
                            LandManager.getInstance().syncAndSave(land);
                            gui.build(p);
                            return true;
                        }
                    }
                    return false;
                }
            });
        }

        int rest = gui.getSize() % 9;
        int slot = rest == 0 ? gui.getSize() : gui.getSize() + (9 - rest);
        for (BuildState state : BuildState.values(land.isPlot() ? BuildState.AppliesTo.LAND : BuildState.AppliesTo.get(land.getLandOwner()))) {
            gui.addItem(slot++, new ClickableItem() {
                @Override
                public ItemStack getItem(Player p) {
                    OwnedLand land = LandManager.getInstance().getLand(landLocation);
                    if (land != null) {
                        Resident resident = ResidentManager.getInstance().getResident(p);
                        if (land.isPlotOwner(p) || land.canAdmin(resident)) {
                            return state.getItem()
                                    .setName(getMessage("toggle-all").getReplacedText("state", state.getTag()))
                                    .build();
                        }
                    }
                    return null;
                }

                @Override
                public boolean click(Player p, ClickType clickType) {
                    OwnedLand land = LandManager.getInstance().getLand(landLocation);
                    if (land != null) {
                        Resident resident = ResidentManager.getInstance().getResident(p);
                        if (land.isPlotOwner(p) || land.canAdmin(resident)) {
                            for (Use u : Use.values()) {
                                land.setUseState(u, state);
                            }
                            MessageUtils.tell(p, Message.get("command.plot.use.success-all"),
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
        gui.addItem(slot, new ClickableItem() {
            @Override
            public ItemStack getItem(Player p) {
                OwnedLand land = LandManager.getInstance().getLand(landLocation);
                if (land != null) {
                    Resident resident = ResidentManager.getInstance().getResident(p);
                    if (land.isPlotOwner(p) || land.canAdmin(resident)) {
                        return new CItem(Material.EGG)
                                .setName(getMessage("unset-all").getReplacedText("parent", land.getLandOwner().getStateTag()))
                                .build();
                    }
                }
                return null;
            }

            @Override
            public boolean click(Player p, ClickType type) {
                OwnedLand land = LandManager.getInstance().getLand(landLocation);
                if (land != null) {
                    Resident resident = ResidentManager.getInstance().getResident(p);
                    if (land.isPlotOwner(p) || land.canAdmin(resident)) {
                        for (Use u : Use.values()) {
                            land.unsetUseState(u);
                        }
                        MessageUtils.tell(p, Message.get("command.plot.use.unset-all"),
                                "parent", land.getLandOwner().getStateTag()
                        );
                        LandManager.getInstance().syncAndSave(land);
                        gui.build(p);
                        return true;
                    }
                }
                return false;
            }
        });
        gui.addItem((gui.getMaxSlot() / 9) * 9 + 8, new ClickableItem() {
            public ItemStack getItem(final Player p) {
                return new CItem(Material.BARRIER).setName(getText("back")).build();
            }

            public boolean click(final Player p, final ClickType clickType) {
                SettingsGUI.getInstance().open(p);
                return true;
            }
        });
        gui.build(player);
    }
}
