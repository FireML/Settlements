package de.themoep.settlements.village.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.GuiPageElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.rank.PermissionGroup;
import de.themoep.settlements.rank.SettlementRank;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.resident.Resident;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RankPermsGUI extends SettlementsGUI {
    private final static String[] SETUP = {
            "ggggggggg",
            "ggggggggg",
            "ggggggggg",
            "ggggggggr",
            "ggggggggg",
            "p   b   n"
    };
    private final Resident res;
    private final InventoryGui gui;

    public RankPermsGUI(Resident res, SettlementRank rank, PermissionGroup group) {
        super("rank-perms");
        this.res = res;
        List<String> perms = group.getPermissions().stream()
                .filter(s -> !s.contains(".help"))
                .collect(Collectors.toList());
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title",
                "rank", rank.getTag(res.getSettlement()),
                "group", group.getName()
        ), SETUP);
        gui.setCloseAction(close -> false);
        gui.setFiller(BORDER_ITEM);
        gui.addElement(new GuiPageElement('p', ItemUtils.toIcon(Material.OAK_SIGN), GuiPageElement.PageAction.PREVIOUS, getText("previous")));
        gui.addElement(new GuiPageElement('n', ItemUtils.toIcon(Material.OAK_SIGN), GuiPageElement.PageAction.NEXT, getText("next")));

        GuiElementGroup g = new GuiElementGroup('g');

        for (String perm : perms) {
            g.addElement(new DynamicGuiElement('g', (h) -> {
                if (!res.hasSettlement()) {
                    return null;
                }

                String permName = Message.get("command." + perm + ".permission.name").orElse(WordUtils.capitalizeFully(perm.replace('.', ' ')));
                if (permName.startsWith("Settlement ")) {
                    permName = permName.substring("Settlement ".length());
                }
                Material item;
                Optional<Boolean> value = res.getSettlement().getEffectivePermissionValue(rank, perm);
                List<String> description = new ArrayList<>();

                SettlementRank fromRank = rank;
                ChatColor color;
                if (value.orElse(false)) {
                    for (SettlementRank r : rank.getInheritedRanks()) {
                        if (res.getSettlement().getEffectivePermissionValue(r, perm).orElse(false)) {
                            fromRank = r;
                        }
                    }
                    if (fromRank == rank) {
                        item = Material.LIME_WOOL;
                        color = ChatColor.GREEN;
                    } else {
                        item = Material.GREEN_WOOL;
                        description.add(getText("entry.inherited",
                                "perm", permName,
                                "rank", rank.getTag(res.getSettlement()),
                                "from", fromRank.getTag(res.getSettlement())
                        ));
                        color = ChatColor.DARK_GREEN;
                    }
                } else {
                    item = Material.RED_WOOL;
                    color = ChatColor.RED;
                }

                boolean defaultValue = rank.getPermissionValue(perm);
                if (fromRank == rank && res.getSettlement().getPermissionValue(rank, perm).orElse(false) != defaultValue) {
                    if (res.getSettlementRank().above(rank) && res.hasPermission("settlement.perms.ranks.unset")) {
                        description.add(getText("entry.unset",
                                "perm", permName,
                                "rank", rank.getTag(res.getSettlement()),
                                "default", String.valueOf(defaultValue)
                        ));
                    }
                }
                if (res.getSettlementRank().above(rank) && res.hasPermission("settlement.ranks.perms.set") && res.hasPermission(perm)) {
                    if (value.orElse(false)) {
                        if (fromRank == rank) {
                            description.add(getText("entry.revoke",
                                    "perm", permName,
                                    "rank", rank.getTag(res.getSettlement())
                            ));
                        }
                    } else {
                        description.add(getText("entry.grant",
                                "perm", permName,
                                "rank", rank.getTag(res.getSettlement())
                        ));
                    }
                }

                List<String> text = new ArrayList<>();
                text.add(getText("entry.title",
                        "color", color.toString(),
                        "perm", permName,
                        "rank", rank.getTag(res.getSettlement()),
                        "description", Message.orElse("command." + perm + ".permission.description", Message.get("command." + perm + ".description")).getText()
                ));
                text.addAll(description);
                return new StaticGuiElement('g', ItemUtils.toIcon(item), click -> {
                    if (res.hasSettlement() && res.getSettlementRank().above(rank)) {
                        if (click.getType() != ClickType.MIDDLE) {
                            if (res.hasPermission("settlement.ranks.perms.set") && res.hasPermission(perm)) {
                                Optional<Boolean> currentValue = res.getSettlement().getEffectivePermissionValue(rank, perm);
                                SettlementRank currentFromRank = rank;
                                if (currentValue.orElse(false)) {
                                    for (SettlementRank r : rank.getInheritedRanks()) {
                                        if (res.getSettlement().getEffectivePermissionValue(r, perm).orElse(false)) {
                                            currentFromRank = r;
                                        }
                                    }
                                }
                                if (!currentValue.orElse(false) || currentFromRank == rank) {
                                    if (res.getSettlement().setPermission(rank, perm, !currentValue.orElse(false))) {
                                        DatabaseManager.getInstance().save(res.getSettlement());
                                        gui.draw();
                                    }
                                }
                            }
                        } else {
                            if (res.hasPermission("settlement.ranks.perms.unset")) {
                                if (res.getSettlement().setDefaultPermission(rank, perm)) {
                                    DatabaseManager.getInstance().save(res.getSettlement());
                                    gui.draw();
                                }
                            }
                        }
                    }
                    return true;
                }, text.toArray(new String[0]));
            }));
        }

        gui.addElement(g);

        gui.addElement(new StaticGuiElement('b', ItemUtils.toIcon(Material.BARRIER), click -> {
            new RankPermGroupsGUI(res, rank).open();
            return true;
        }, getText("back")));
    }

    public void open() {
        gui.show(res.getPlayer());
    }
}
