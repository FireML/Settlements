package de.themoep.settlements.land.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.land.Banished;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.land.Unbanished;
import de.themoep.settlements.leveling.LevelInfo;
import de.themoep.settlements.rank.Rank;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.gui.ResidentGUI;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.NameTag;
import de.themoep.settlements.utils.Next;
import de.themoep.settlements.utils.Targetable;
import de.themoep.settlements.utils.Utils;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

public class BanishedGUI extends SettlementsGUI {

    private final InventoryGui gui;
    private final Resident resident;
    private Banished.Sort sort = Banished.Sort.DATE;

    public BanishedGUI(@NonNull String type, Resident resident, LandOwner<?, ?> landOwner) {
        super(type + ".banished");
        this.resident = resident;
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title"), getSetup());
        gui.setCloseAction(close -> false);
        gui.setFiller(getIcon("filler"));
        gui.addElements(getConfiguredElements());

        DynamicGuiElement sortedGroup = new DynamicGuiElement('r', who -> {
            GuiElementGroup group = new GuiElementGroup('r');
            landOwner.getBanished().stream().sorted((b1, b2) -> b1.compareTo(sort, b2)).forEach(b -> {
                Targetable t = b.getTargetable();
                ItemStack icon;
                if (t instanceof Resident) {
                    icon = ItemUtils.getHead(((Resident) t).getUUID(), t.getName());
                } else if (t instanceof LandOwner) {
                    icon = ItemUtils.getHead(((LandOwner<?, ?>) t).getChiefId(), ((LandOwner<?, ?>) t).getChiefId().toString());
                } else {
                    return;
                }
                group.addElement(
                        new StaticGuiElement('r', icon, click -> {
                            if (landOwner.hasPermission(resident, "unbanish")
                                    && landOwner.removeBanished(resident, t)) {
                                landOwner.save();
                                click.getGui().draw();
                                Message.get("command." + type + ".unbanish.unbanished").tell(landOwner.getOnlineResidents(), "target", t.getName());
                            }
                            return true;
                        }, getText("entry",
                                "name", t.getName(),
                                "date", Utils.DATE_FORMAT.format(b.getBanishDate()),
                                "reason", b.getReason(),
                                "banisher", b.getSenderId() != Utils.ZERO_UUID ? ResidentManager.getInstance().getResidentName(b.getSenderId()) : "[Auto]"
                        ))
                );
            });
            return group;
        });

        gui.addElement(sortedGroup);

        gui.addElement(getCycleElement('s', () -> sort, click -> {
            sort = sort.next();
            click.getGui().draw();
            return true;
        }, "sort"));

        gui.addElement(new StaticGuiElement('u', getIcon("unbanished"), click -> {
            new UnbanishedGUI(type, resident, landOwner).open();
            return true;
        }, getText("unbanished")));

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

        gui.addElement(new GuiBackElement('b', getIcon("back"), true));
    }

    public void open() {
        gui.show(resident.getPlayer());
    }
}
