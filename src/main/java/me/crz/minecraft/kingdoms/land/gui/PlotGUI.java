package me.crz.minecraft.kingdoms.land.gui;

import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.ChatMap;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public abstract class PlotGUI extends KingdomsGUI {
    public PlotGUI(String name) {
        super(name);
    }

    protected ClickableItem getPlotLinkItem(CInventory gui) {
        return new ClickableItem() {
            @Override
            public ItemStack getItem(Player p) {
                OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                Resident resident = ResidentManager.getInstance().getResident(p);
                List<String> description = new ArrayList<>();
                CItem item = new CItem(Material.MAP);
                if (land.hasLink()) {
                    description.add(getMessage("linked").getReplacedText("amount", String.valueOf(land.getLinked().size() - 1)));
                    item.glow(true);
                }
                if (land.isPlotOwner(p) || (land.canAdmin(resident) && (land.isPlot() || land.hasLink()))) {
                    if (description.isEmpty()) {
                        description.add(getText("link"));
                    } else {
                        description.add(getText("unlink"));
                    }
                }
                if (!description.isEmpty()) {
                    return item.setName(String.join("\n", description)).build();
                }
                return null;
            }

            @Override
            public boolean click(Player p, ClickType type) {
                OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                if (land != null) {
                    Resident resident = ResidentManager.getInstance().getResident(p);
                    boolean canAdmin = land.canAdmin(resident);
                    if (land.isPlotOwner(p) || canAdmin) {
                        if (land.hasLink()) {
                            Collection<OwnedLand> linkedLands = land.getLinked();
                            int linked = linkedLands.size() - 1;
                            Set<OwnedLand> toSave = new HashSet<>();
                            if (linked == 1) {
                                OwnedLand otherLand = linkedLands.stream().filter(o -> !o.equals(land)).findAny().get();
                                otherLand.removeLink();
                                toSave.add(otherLand);
                            }
                            if (land.removeLink()) {
                                toSave.add(land);
                                Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(toSave));
                                gui.build(p);
                            }
                            Message.get("command.plot.unlink.disconnected").tell(p, "amount", String.valueOf(linked));
                        } else {
                            Utils.runLater(p::closeInventory, 1);
                            Utils.runAsync(() -> new ChatMap(p, land.getMapKey()));
                        }
                        return true;
                    }
                }
                return false;
            }
        };
    }
}
