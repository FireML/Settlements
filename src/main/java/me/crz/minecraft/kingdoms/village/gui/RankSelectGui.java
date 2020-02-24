package me.crz.minecraft.kingdoms.village.gui;

import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.Resident;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class RankSelectGui extends KingdomsGUI {
    private final Resident resident;
    private final CInventory gui;

    public RankSelectGui(Resident resident, Consumer<VillageRank> rankConsumer) {
        super("rank-select");
        this.resident = resident;
        gui = new CInventory(getText("title", "state", resident.hasVillage() ? resident.getVillage().getStateTag() : ""), InventorySize.ONE) {};

        for (VillageRank rank : VillageRank.values()) {
            gui.addItem(new ClickableItem() {
                @Override
                public ItemStack getItem(Player p) {
                    if (resident.hasVillage()) {
                        CItem item = rank.getItem().setName(getMessage("entry").getReplacedText(
                                "rank", rank.getTag(resident.getVillage())
                        ));
                        if (rank == resident.getVillageRank()) {
                            item.glow(true);
                        }
                        return item.build();
                    }
                    return null;
                }

                @Override
                public boolean click(Player p, ClickType type) {
                    if (resident.hasVillage()) {
                        rankConsumer.accept(rank);
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    public void open() {
        gui.build(resident.getPlayer());
    }
}
