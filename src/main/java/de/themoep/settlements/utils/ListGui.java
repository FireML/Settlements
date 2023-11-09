package de.themoep.settlements.utils;

import de.themoep.settlements.resident.Resident;
import org.bukkit.inventory.ItemStack;
import uk.firedev.poleislib.libs.themoep.inventorygui.*;
import uk.firedev.poleislib.utils.ItemUtils;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ListGui<T extends Sortable<T>> extends ConfigurableGui {

    private Sortable.Sort<T> sort = null;

    public ListGui(Resident resident, String type, Supplier<Collection<T>> sortablesProvider, BiConsumer<GuiElement.Click, T> onSelect, Function<T, String[]> replacementsProvider) {
        this(resident, type, false, sortablesProvider, onSelect, replacementsProvider);
    }

    public ListGui(Resident resident, String type, boolean async, Supplier<Collection<T>> sortablesProvider, BiConsumer<GuiElement.Click, T> onSelect, Function<T, String[]> replacementsProvider) {
        super(resident, type);

        Runnable create = () -> {
            Sortable.Sort<T>[] sorts;
            Collection<T> s = sortablesProvider.get();
            if (!s.isEmpty()) {
                sorts = s.iterator().next().getSorts();
                sort = sorts[0];
            }

            // Cache icon and replacements
            for (T t : s) {
                t.getIcon();
                t.getReplacements(this);
            }

            DynamicGuiElement sortedGroup = new DynamicGuiElement('e', who -> {
                GuiElementGroup group = new GuiElementGroup('e');

                if (sort != null) {
                    Collection<T> sortables = sortablesProvider.get();
                    sortables.stream().sorted(sort.getComparator()).forEachOrdered(sortable -> {
                        Message message = getMessage("entry." + sortable.getKey());
                        if (!message.exists()) {
                            message = getMessage("entry");
                        }
                        group.addElement(
                                new StaticGuiElement('e', sortable.getIcon(), click -> {
                                    onSelect.accept(click, sortable);
                                    return true;
                                }, Utils.wrapLore(message.getReplacedText(sortable.getReplacements(this, replacementsProvider.apply(sortable)))))
                        );
                    });
                }
                return group;
            });
            gui.addElement(sortedGroup);

            if (sort != null) {
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
            }

            GuiElementGroup pages = new GuiElementGroup('p');
            gui.addElement(pages);

            gui.draw(resident.getPlayer());

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
        };

        if (async) {
            Utils.runAsync(() -> {
                create.run();
                gui.draw();
            });
        } else {
            create.run();
        }

        gui.addElement(new GuiBackElement('b', getIcon("back"), true, getText("back")));
    }

    public void addElement(GuiElement element) {
        gui.addElement(element);
    }

    public void open() {
        gui.show(resident.getPlayer());
    }
}
