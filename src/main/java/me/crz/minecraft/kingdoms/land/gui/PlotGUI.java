package me.crz.minecraft.kingdoms.land.gui;

import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
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
import java.util.LinkedHashSet;
import java.util.List;
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
                if (land != null && land.isPlotOwner(p)) {
                    List<String> description = new ArrayList<>();
                    CItem item = new CItem(Material.MAP);
                    if (land.hasLink()) {
                        description.add(getMessage("linked").getReplacedText("amount", String.valueOf(land.getLinked().size() - 1)));
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
                if (land != null) {
                    Resident resident = ResidentManager.getInstance().getResident(p);
                    boolean canAdmin = resident.hasVillage() && land.isOwnedBy(resident.getVillage()) && resident.getVillageRank().atLeast(VillageRank.ELDER);
                    if (land.isPlotOwner(p) || canAdmin) {
                        Set<OwnedLand> toSave = new LinkedHashSet<>();
                        if (land.hasLink()) {
                            int linked = land.getLinked().size() - 1;
                            if (land.removeLink()) {
                                toSave.add(land);
                            }
                            Message.get("command.village.plot.unlink.disconnected").tell(p, "amount", String.valueOf(linked));
                        } else {
                            Collection<OwnedLand> connected = LandManager.getInstance().getAllConnectedLand(land, search -> {
                                if (!search.isPlotOwner(p)) {
                                    return LandManager.FilterResponse.SKIP_ENTRY;
                                }
                                return LandManager.FilterResponse.ACCEPT;
                            });

                            UUID link = connected.stream()
                                    .filter(OwnedLand::hasLink)
                                    .map(OwnedLand::getLink)
                                    .findAny().orElse(UUID.randomUUID());
                            Message.get("command.village.plot.link.success").tell(p, "amount", String.valueOf(connected.size()));
                            connected.add(land); // add current to save
                            for (OwnedLand c : connected) {
                                if (LandManager.getInstance().addLink(c, link)) {
                                    toSave.add(c);
                                }
                            }
                        }
                        if (!toSave.isEmpty()) {
                            Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(toSave));
                            gui.build(p);
                            return true;
                        }
                    }
                }
                return false;
            }
        };
    }
}
