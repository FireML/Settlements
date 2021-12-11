package de.themoep.settlements.settlement.gui;

import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.land.gui.AccessGUI;
import de.themoep.settlements.land.gui.AlliesGUI;
import de.themoep.settlements.land.gui.BanishedGUI;
import de.themoep.settlements.land.gui.BuildGUI;
import de.themoep.settlements.land.gui.PermsGUI;
import de.themoep.settlements.land.gui.RankPermsGUI;
import de.themoep.settlements.land.gui.RecruitmentGUI;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.SignGui;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.entity.Player;

import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ManagementGUI extends SettlementsGUI {
    private final Resident resident;
    public InventoryGui gui;

    public ManagementGUI(Player player) {
        this(ResidentManager.getInstance().getResident(player));
    }

    public ManagementGUI(Resident resident) {
        super("settlement.management");
        this.resident = resident;
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title",
                "name", resident.getSettlement().getName(),
                "state", resident.getSettlement().getStateTag()
        ), getSetup());
        gui.setCloseAction(close -> false);
        gui.addElements(getConfiguredElements());
        gui.setFiller(getIcon("filler"));

        if (resident.hasPermission("settlement.reslist")) {
            Resident chief = resident.getSettlement().getChief();
            gui.addElement(new StaticGuiElement('r', ItemUtils.getHead(chief.getUUID(), chief.getName()), click -> {
                if (resident.hasPermission("settlement.reslist")) {
                    new ResListGUI(resident, resident.getSettlement()).open();
                }
                return true;
            }, getText("reslist", "state", resident.getSettlement().getStateTag())));
        }

        if (resident.hasPermission("settlement.banished")) {
            gui.addElement(new StaticGuiElement('d', getIcon("banished"), click -> {
                if (resident.hasPermission("settlement.banished")) {
                    new BanishedGUI("settlement", resident, resident.getSettlement()).open();
                }
                return true;
            }, getText("banished", "state", resident.getSettlement().getStateTag())));
        }

        if (resident.hasPermission("settlement.allies")) {
            gui.addElement(new StaticGuiElement('a', getIcon("allies"), click -> {
                if (resident.hasPermission("settlement.allies")) {
                    new AlliesGUI("settlement", resident, resident.getSettlement()).open();
                }
                return true;
            }, getText("allies", "state", resident.getSettlement().getStateTag())));
        }

        if (resident.hasPermission("settlement.ranks")) {
            gui.addElement(new StaticGuiElement('t', getIcon("ranks"), click -> {
                if (resident.hasPermission("settlement.ranks")) {
                    new RankPermsGUI<>("settlement", resident, resident.getSettlement()).open();
                }
                return true;
            }, getText("ranks", "state", resident.getSettlement().getStateTag())));
        }

        if (resident.hasPermission("settlement.settings")) {
            gui.addElement(new StaticGuiElement('t', getIcon("settings"), click -> {
                if (resident.hasPermission("settlement.settings")) {
                    new SettingsGUI(resident).open();
                }
                return true;
            }, getText("settings", "state", resident.getSettlement().getStateTag())));
        }

        if (resident.hasPermission("settlement.perms")) {
            gui.addElement(new StaticGuiElement('t', getIcon("land"), click -> {
                if (resident.hasPermission("settlement.perms")) {
                    Supplier<Boolean> permCheck = () -> resident.getSettlement().hasPermission(resident, "perms");
                    new PermsGUI("settlement",
                            resident.getSettlement(),
                            permCheck,
                            (flagAble, c) -> new BuildGUI("empire", flagAble, permCheck).open(c.getWhoClicked())
                    ).open(resident.getPlayer());
                }
                return true;
            }, getText("land", "state", resident.getSettlement().getStateTag())));
        }

        gui.addElement(new GuiBackElement('b', getIcon("back"), getText("back")));
    }

    public void open() {
        gui.show(resident.getPlayer());
    }
}
