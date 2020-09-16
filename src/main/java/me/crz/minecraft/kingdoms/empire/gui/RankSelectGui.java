package me.crz.minecraft.kingdoms.empire.gui;

import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.village.Resident;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class RankSelectGui extends KingdomsGUI {
    private final Resident resident;
    private final CInventory gui;

    public RankSelectGui(Resident resident, Consumer<EmpireRank> rankConsumer) {
        super("rank-select");
        this.resident = resident;
        gui = new CInventory(getText("title", "state", resident.hasEmpire() ? resident.getVillage().getEmpire().getStateTag() : ""), InventorySize.SIX) {};

        for (int i = 0; i < InventorySize.TWO.getSize(); i++) {
            gui.addItem(i, BORDER);
        }

        for (EmpireRank rank : EmpireRank.values()) {
            gui.addItem(new ClickableItem() {
                @Override
                public ItemStack getItem(Player p) {
                    if (resident.hasEmpire()) {
                        CItem item = new CItem(rank.getItem()).setName(getMessage("entry").getReplacedText(
                                "rank", rank.getTag(resident.getVillage().getEmpire())
                        ));
                        if (rank == resident.getEmpireRank()) {
                            item.glow(true);
                        }
                        return item.build();
                    }
                    return null;
                }

                @Override
                public boolean click(Player p, ClickType type) {
                    if (resident.hasEmpire()) {
                        rankConsumer.accept(rank);
                        return true;
                    }
                    return false;
                }
            });
        }

        for (int i = InventorySize.FOUR.getSize(); i < InventorySize.SIX.getSize(); i++) {
            gui.addItem(i, BORDER);
        }
    }

    public void open() {
        gui.build(resident.getPlayer());
    }
}
