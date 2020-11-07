package me.crz.minecraft.kingdoms.empire.gui;

import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
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

    public RankSelectGui(Resident resident, Consumer<EmpireRank> rankConsumer) {
        super("empire.rank-select");
        this.resident = resident;
        gui = new InventoryGui(KingdomsPlugin.getInstance(),
                getText("title", "state", resident.hasEmpire() ? resident.getVillage().getEmpire().getStateTag() : ""),
                SETUP);
        gui.setCloseAction(close -> false);
        gui.setFiller(BORDER_ITEM);

        GuiElementGroup group = new GuiElementGroup('g');
        for (EmpireRank rank : EmpireRank.values()) {
            group.addElement(new StaticGuiElement('g',
                    ItemUtils.setGlowing(rank.getItem(), rank == resident.getEmpireRank()),
                    click -> {
                        if (resident.hasEmpire()) {
                            rankConsumer.accept(rank);
                        }
                        return true;
                    },  getText("entry",
                            "state", resident.hasEmpire() ? resident.getVillage().getEmpire().getStateTag() : "",
                            "rank", resident.hasEmpire() ? rank.getTag(resident.getVillage().getEmpire()) : rank.getTag()
                    )
            ));
        }
        gui.addElement(group);
    }

    public void open() {
        gui.show(resident.getPlayer());
    }
}
