package me.crz.minecraft.kingdoms.village.gui;

import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.ItemUtils;
import me.crz.minecraft.kingdoms.village.Resident;

import java.util.function.Consumer;

public class RankSelectGui extends KingdomsGUI {
    private final static String[] SETUP = {
            "         ",
            "         ",
            "ggggggggg",
            "ggggggggg",
            "         ",
            "    b    "
    };
    private final Resident resident;
    private final InventoryGui gui;

    public RankSelectGui(Resident resident, Consumer<VillageRank> rankConsumer) {
        super("rank-select");
        this.resident = resident;
        gui = new InventoryGui(KingdomsPlugin.getInstance(),
                getText("title", "state", resident.hasVillage() ? resident.getVillage().getStateTag() : ""),
                SETUP);
        gui.setCloseAction(close -> false);
        gui.setFiller(BORDER_ITEM);

        GuiElementGroup group = new GuiElementGroup('g');
        for (VillageRank rank : VillageRank.values()) {
            group.addElement(new StaticGuiElement('g',
                    ItemUtils.setGlowing(rank.getItem(), rank == resident.getVillageRank()),
                    click -> {
                        if (resident.hasVillage()) {
                            rankConsumer.accept(rank);
                        }
                        return true;
                    }, getText("entry",
                            "state", resident.hasVillage() ? resident.getVillage().getStateTag() : "",
                            "rank", resident.hasVillage() ? rank.getTag(resident.getVillage()) : rank.getTag()
                    )
            ));
        }
        gui.addElement(group);
    }

    public void open() {
        gui.show(resident.getPlayer());
    }
}
