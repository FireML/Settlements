package me.crz.minecraft.kingdoms.land.gui;

import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SettingsGUI extends KingdomsGUI {
    private static SettingsGUI instance;
    public CInventory gui;

    private SettingsGUI() {
        super("plot.settings");
        gui = new CInventory(getText("title"), InventorySize.ONE) {};
        gui.addItem(new ClickableItem() {
            public ItemStack getItem(Player p) {
                return new CItem(Material.DIAMOND_PICKAXE).setName(getText("build")).hideAllFlags(true).build();
            }

            public boolean click(Player p, ClickType clickType) {
                BuildGui.getInstance().open(p);
                return true;
            }
        });
        gui.addItem(new ClickableItem() {
            public ItemStack getItem(Player p) {
                return new CItem(Material.LEVER).setName(getText("use")).hideAllFlags(true).build();
            }

            public boolean click(Player p, ClickType clickType) {
                UseGui.getInstance().open(p);
                return true;
            }
        });
        gui.addItem(new ClickableItem() {
            public ItemStack getItem(Player p) {
                return new CItem(Material.REDSTONE).setName(getText("mode")).hideAllFlags(true).build();
            }

            public boolean click(Player p, ClickType clickType) {
                ModeGUI.getInstance().open(p);
                return true;
            }
        });
        gui.addItem(new ClickableItem() {
            @Override
            public ItemStack getItem(Player p) {
                OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                if (land != null && land.isPlotOwner(p)) {
                    List<String> description = new ArrayList<>();
                    CItem item = new CItem(Material.MAP);
                    if (land.getLinkedTo() != null) {
                        description.add(getMessage("linked-to").getReplacedText("location", land.getLinkedTo().getSimpleKey()));
                    }
                    if (!land.getLinked().isEmpty()) {
                        description.add(getMessage("linked").getReplacedText("amount", String.valueOf(land.getLinked().size())));
                    }
                    if (description.isEmpty()) {
                        description.add(getText("link"));
                    } else {
                        description.add(getText("unlink"));
                        item.glow(true);
                    }
                    return item.setName(String.join("\n", description)).build();
                }
                return null;
            }

            @Override
            public boolean click(Player p, ClickType type) {
                OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                if (land != null && land.isPlotOwner(p)) {
                    Set<OwnedLand> toSave = new LinkedHashSet<>();
                    if (!land.getLinked().isEmpty()) {
                        for (SimpleChunkLocation c : land.getLinked()) {
                            OwnedLand linked = LandManager.getInstance().getLand(c);
                            if (linked != null && linked.isLinkedTo(land)) {
                                linked.setLinkedTo(null);
                                toSave.add(linked);
                            }
                        }
                        Message.get("command.village.plot.unlink.disconnected.others").tell(p, "amount", String.valueOf(toSave.size()));
                    }
                    if (land.getLinkedTo() != null) {
                        OwnedLand linkedTo = LandManager.getInstance().getLand(land.getLinkedTo());
                        if (linkedTo != null && linkedTo.removeLinked(land)) {
                            toSave.add(linkedTo);
                        }
                        Message.get("command.village.plot.unlink.disconnected.self").tell(p, "location", land.getLinkedTo().getSimpleKey());
                        land.setLinkedTo(null);
                    }
                    if (!toSave.isEmpty()) {
                        toSave.add(land);
                        Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(toSave));
                        gui.build(p);
                        return true;
                    }
                }
                return false;
            }
        });
        gui.addItem(8, new ClickableItem() {
            public ItemStack getItem(Player p) {
                OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                if (land == null) {
                    return null;
                }

                return new CItem(Material.GREEN_BANNER).setName(
                        getMessage("entry").getReplacedText(land.getReplacements())
                ).build();
            }

            public boolean click(Player p, ClickType clickType) {
                return false;
            }
        });
    }

    public void open(Player p) {
        gui.build(p);
    }

    public static SettingsGUI getInstance() {
        if (instance == null) {
            instance = new SettingsGUI();
        }
        return instance;
    }
}
