package de.themoep.settlements.land.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.Ally;
import de.themoep.settlements.land.AllyRequest;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.ListGui;
import de.themoep.settlements.utils.Utils;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;

public class AlliesGUI extends SettlementsGUI {

    private final InventoryGui gui;
    private final Resident resident;
    private Ally.Sort sort = Ally.Sort.ALLY_DATE;

    public AlliesGUI(Resident resident, LandOwner<?, ?> landOwner) {
        super(landOwner.getKey() + ".allies");
        this.resident = resident;
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title"), getSetup());
        gui.setCloseAction(close -> false);
        gui.setFiller(getIcon("filler"));
        gui.addElements(getConfiguredElements());

        DynamicGuiElement sortedGroup = new DynamicGuiElement('a', who -> {
            GuiElementGroup group = new GuiElementGroup('a');
            landOwner.getAllies().stream()
                    .filter(a -> a.getLandOwner() != null)
                    .sorted((a1, a2) -> a1.compareTo(sort, a2))
                    .forEach(a -> group.addElement(
                            new StaticGuiElement('a', ItemUtils.getHead(a.getLandOwner().getChief().getUUID(), resident.getName()), click -> {

                                if (landOwner.hasPermission(resident, "manageally")) {
                                    new AllyGUI(landOwner, resident, a).open();
                                }
                                return true;
                            }, getText("entry." + a.getType(), a.getLandOwner().getReplacements(this, "allydate", Utils.DATE_FORMAT.format(a.getCreationDate()))))
                    ));
            return group;
        });

        gui.addElement(sortedGroup);

        gui.addElement(getCycleElement('s', () -> sort, click -> {
            if (sort != null) {
                if (click.getType().isLeftClick()) {
                    sort = sort.next();
                } else if (click.getType().isRightClick()) {
                    sort = sort.previous();
                }
            }
            click.getGui().draw();
            return true;
        }, "sort"));

        Collection<AllyRequest> requests = landOwner.getAllyRequests();
        if (landOwner.hasPermission(resident, "manageally")) {
            gui.addElement(new StaticGuiElement('f', ItemUtils.setGlowing(getIcon("requests"), !requests.isEmpty()), click -> {
                new ListGui<>(resident, "general.requests", () -> new ArrayList<>(landOwner.getAllyRequests()), (c, request) -> {
                    // accept request
                    if (landOwner.hasPermission(resident, "manageally") && request instanceof AllyRequest) {
                        AllyRequest currentRequest = landOwner.removeAllyRequest(((AllyRequest) request));
                        if (currentRequest == null) {
                            // No request sent?
                            c.getGui().draw();
                            return;
                        }
                        c.getGui().close();
                        Utils.runAsync(() -> {
                            LandOwner<?, ?> sender = ((AllyRequest) request).getSource();
                            DatabaseManager.getInstance().delete(request);
                            AllyRequest reverseRequest = sender.removeAllyRequest(landOwner);
                            if (reverseRequest != null) {
                                DatabaseManager.getInstance().delete(reverseRequest);
                            }
                            Utils.runSync(() -> {
                                sender.addAlly(landOwner).save();
                                landOwner.addAlly(sender).save();
                                getMessage("success").tell(resident, "name", sender.getName());
                                getMessage("now-allied-to").tell(sender, "name", landOwner.getName(), "state", landOwner.getStateTag());
                                getMessage("now-allied-to").tell(landOwner, "name", sender.getName(), "state", sender.getStateTag());
                            });
                        });
                    }
                }, request -> request.getReplacements(this)).open();
                return true;
            }, getText("requests", "amount", String.valueOf(requests.size()))));
        }

        GuiElementGroup pages = new GuiElementGroup('p');
        gui.addElement(pages);

        for (int i = 0; i < gui.getPageAmount(resident.getPlayer()) && i < pages.getSlots().length; i++) {
            int page = i;
            pages.addElement(new DynamicGuiElement('p', who -> {
                ItemStack icon = getIcon("page");
                if (page == gui.getPageNumber(who)) {
                    ItemUtils.setGlowing(icon, true);
                }
                return new StaticGuiElement('p', icon, page + 1, click -> {
                    gui.setPageNumber(click.getWhoClicked(), page);
                    return true;
                }, getText("page", "page", String.valueOf(page + 1), "pages", String.valueOf(gui.getPageAmount(who))));
            }));
        }

        gui.addElement(new GuiBackElement('b', getIcon("back"), true, getText("back")));
    }

    public void open() {
        gui.show(resident.getPlayer());
    }
}
