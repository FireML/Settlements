package me.crz.minecraft.kingdoms.landflags;

import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.KingdomsGUI;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PlotUseGui extends KingdomsGUI {
    private static PlotUseGui instance;
    private CInventory gui;

    public PlotUseGui() {
        super("plot-use");
        gui = new CInventory(getText("title"), InventorySize.FOUR) {};
        for (Use u : Use.values()) {
            gui.addItem(new ClickableItem() {
                public ItemStack getItem(Player p) {
                    OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                    if (land != null) {
                        PlotBuildState state = land.getUseState(u);
                        return u.getItem().setName(getMessage("entry.title").getReplacedText(
                                "flag", u.getTag(),
                                "state", state.getTag()
                        )).build();
                    }
                    return null;
                }

                public boolean click(Player p, ClickType clickType) {
                    OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                    if (land != null && land.isPlotOwner(p)) {
                        PlotBuildState next = PlotBuildState.next(land.getUseState(u));
                        land.setUseState(u, next);
                        MessageUtils.tell(p, Message.get("command.village.plot.use.success"),
                                "flag", u.getTag(),
                                "state", next.getTag()
                        );
                        LandManager.getInstance().syncAndSave(land);
                        gui.build(p);
                        return true;
                    }
                    return false;
                }
            });
        }

        int rest = gui.getSize() % 9;
        int slot = rest == 0 ? gui.getSize() : gui.getSize() + (9 - rest);
        for (PlotBuildState state : PlotBuildState.values()) {
            gui.addItem(slot++, new ClickableItem() {
                @Override
                public ItemStack getItem(Player p) {
                    OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                    if (land != null && land.isPlotOwner(p)) {
                        return state.getItem()
                                .setName(getMessage("toggle-all").getReplacedText("state", state.getTag()))
                                .setLore(getMessage("toggle-all").getReplacedTextList("state", state.getTag()).stream().skip(1).collect(Collectors.toList()))
                                .build();
                    }
                    return null;
                }

                @Override
                public boolean click(Player p, ClickType clickType) {
                    OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                    if (land != null && land.isPlotOwner(p)) {
                        for (Use u : Use.values()) {
                            land.setUseState(u, state);
                        }
                        MessageUtils.tell(p, Message.get("command.village.plot.use.success-all"),
                                "state", state.getTag()
                        );
                        LandManager.getInstance().syncAndSave(land);
                        gui.build(p);
                        return true;
                    }
                    return false;
                }
            });
        }

        gui.addItem((slot / 9) * 9 + 8, new ClickableItem() {
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
    }

    public void open(final Player p) {
        gui.build(p);
    }

    public static PlotUseGui getInstance() {
        if (instance == null) {
            instance = new PlotUseGui();
        }
        return instance;
    }
}
