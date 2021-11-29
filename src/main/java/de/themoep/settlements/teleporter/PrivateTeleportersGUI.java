package de.themoep.settlements.teleporter;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.themoep.inventorygui.GuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.GuiPageElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.CommandManager;
import de.themoep.settlements.command.empire.EmpireHomeCommand;
import de.themoep.settlements.command.settlement.SettlementHomeCommand;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.land.LandAccess;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.utils.IdObject;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.PathedConfigMessages;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.world.NamedLocation;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class PrivateTeleportersGUI extends SettlementsGUI {
    private final static String[] SETUP = {
            "ggggggggg",
            "ggggggggg",
            "ggggggggg",
            "ggggggggg",
            "         ",
            "p   b   n"
    };
    private final Cache<IdObject, List<Supplier<GuiElement>>> suppliers = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES).build();
    private final InventoryGui gui;

    public PrivateTeleportersGUI(Player player) {
        super("private-teleports");
        Settlement settlement = SettlementManager.getInstance().getFromPlayer(player);
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title"), SETUP);
        gui.setCloseAction(close -> false);
        gui.setFiller(BORDER_ITEM);
        GuiElementGroup group = new GuiElementGroup('g');
        try {
            for (Supplier<GuiElement> supplier : getSuppliers(settlement)) {
                group.addElement(supplier.get());
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        gui.addElement(group);

        gui.addElement(new GuiPageElement(
                'p',
                ItemUtils.toIcon(Material.OAK_SIGN),
                GuiPageElement.PageAction.PREVIOUS,
                getText("page.previous", "page", "%prevpage%")
        ));
        gui.addElement(new GuiPageElement(
                'n',
                ItemUtils.toIcon(Material.OAK_SIGN),
                GuiPageElement.PageAction.NEXT,
                getText("page.previous", "page", "%nextpage%")
        ));

        gui.addElement(new StaticGuiElement('b', ItemUtils.toIcon(Material.BARRIER), click -> {
            new TeleportersGUI().open((Player) click.getEvent().getWhoClicked());
            return true;
        }, getText("back")));

        gui.show(player);
    }

    private List<Supplier<GuiElement>> getSuppliers(Settlement settlement) throws ExecutionException {
        return suppliers.get(settlement != null ? settlement.getId() : IdObject.get(null), () -> {
            List<Supplier<GuiElement>> suppliers = new ArrayList<>();

            Set<LandOwner> entries = new LinkedHashSet<>();
            if (settlement != null) {
                for (Map.Entry<String, NamedLocation> outpost : settlement.getOutpostHomes().entrySet()) {
                    String text = getText("outpost",
                            "name", outpost.getValue().getName(),
                            "state", settlement.getStateTag(),
                            "owner", settlement.getName()
                    );
                    suppliers.add(() -> new StaticGuiElement(
                            'g',
                            ItemUtils.toIcon(Material.RED_BANNER),
                            click -> {
                                click.getGui().close();
                                if (settlement.getOutpostHomes().containsKey(outpost.getKey())) {
                                    Utils.teleport(
                                            new PathedConfigMessages("command.settlement.outpost.teleport"),
                                            (Player) click.getEvent().getWhoClicked(),
                                            settlement.getOutpostHomes().get(outpost.getKey()).toBukkitLocation(),
                                            "outpost",
                                            "name", outpost.getValue().getName()
                                    );
                                }
                                return true;
                            },
                            text
                    ));
                }

                settlement.getAllies().stream()
                        .filter(v -> v.getAccess().atLeast(LandAccess.ALLY))
                        .forEachOrdered(entries::add);

                if (settlement.hasEmpire()) {
                    settlement.getEmpire().getMembers().stream()
                            .filter(v -> !v.equals(settlement) && v.getAccess().atLeast(LandAccess.TRUSTED))
                            .sorted(Settlement::compareTo)
                            .forEachOrdered(entries::add);
                }
            }

            EmpireManager.getInstance().getEmpires().stream()
                    .filter(e -> (settlement == null || !e.equals(settlement.getEmpire())) && e.getAccess().atLeast(LandAccess.PUBLIC))
                    .sorted(Empire::compareTo)
                    .forEachOrdered(entries::add);

            SettlementManager.getInstance().getSettlements().stream()
                    .filter(v -> !v.equals(settlement) && v.getAccess().atLeast(LandAccess.PUBLIC))
                    .sorted(Settlement::compareTo)
                    .forEachOrdered(entries::add);

            for (LandOwner landOwner : entries) {
                Resident chief = ResidentManager.getInstance().getResident(landOwner.getChiefId());
                ItemStack icon = ItemUtils.getHead(chief.getUUID(), chief.getName());
                String text = getText("entry", "name", landOwner.getName(), "state", landOwner.getStateTag());
                suppliers.add(() -> new StaticGuiElement(
                        'g',
                        icon,
                        click -> {
                            if (landOwner.hasHome()) {
                                click.getGui().close();
                                if (landOwner instanceof Empire) {
                                    ((EmpireHomeCommand) CommandManager.getInstance().getCommand("empire").getSubCommands().get("home")).run(
                                            ResidentManager.getInstance().getResident((Player) click.getEvent().getWhoClicked()),
                                            "empire",
                                            "home",
                                            landOwner.getName()
                                    );
                                } else if (landOwner instanceof Settlement) {
                                    ((SettlementHomeCommand) CommandManager.getInstance().getCommand("settlement").getSubCommands().get("home")).run(
                                            ResidentManager.getInstance().getResident((Player) click.getEvent().getWhoClicked()),
                                            "settlement",
                                            "home",
                                            landOwner.getName()
                                    );
                                }
                            }
                            return true;
                        },
                        text
                ));
            }

            return suppliers;
        });
    }
}
