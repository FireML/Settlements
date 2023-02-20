package de.themoep.settlements.land.gui;

import de.themoep.inventorygui.*;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.leveling.LevelInfo;
import de.themoep.settlements.rank.Rank;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.ConfirmationGui;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.Message;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.stream.Collectors;

public class RankPermsGUI<L extends LandOwner<R, ? extends LevelInfo>, R extends Rank<L, R>> extends SettlementsGUI {
    private final Resident res;
    private final InventoryGui gui;

    private R rank;

    public RankPermsGUI(@NonNull String type, Resident res, L landOwner) {
        super(type + ".rank-perms");
        this.res = res;
        List<String> perms = landOwner.getToggleablePermissions().stream()
                .filter(s -> !s.contains(".help"))
                .collect(Collectors.toList());
        rank = landOwner.getDefaultRank();
        if (landOwner.useGuestRank()) {
            rank = (R) rank.getParentRank();
        }
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title",
                "state", landOwner.getStateTag(),
                "name", landOwner.getName()
        ), getSetup());
        gui.setCloseAction(close -> false);
        gui.setFiller(getIcon("filler"));
        gui.addElements(getConfiguredElements());

        GuiElementGroup permsGroup = createGroup('p');

        for (String perm : perms) {
            permsGroup.addElement(new DynamicGuiElement('p', h -> {
                if (!landOwner.isMember(res)) {
                    return null;
                }

                String permName = Message.get("command." + perm + ".permission.name").orElse(WordUtils.capitalizeFully(perm.replace('.', ' ').replace('-', ' ')));
                if (permName.toLowerCase(Locale.ROOT).startsWith(type + " ")) {
                    permName = permName.substring(type.length() + 1);
                }
                ItemStack item;
                Optional<Boolean> value = landOwner.getEffectivePermissionValue(rank, perm);
                List<String> description = new ArrayList<>();

                R fromRank = rank;
                ChatColor color;
                if (value.orElse(false)) {
                    for (Object r : rank.getInheritedRanks()) {
                        if (landOwner.getEffectivePermissionValue((R) r, perm).orElse(false)) {
                            fromRank = (R) r;
                        }
                    }
                    if (fromRank == rank) {
                        item = getIcon("set");
                        color = ChatColor.GREEN;
                    } else {
                        item = getIcon("inherited");
                        description.add(getText("entry.inherited",
                                "perm", permName,
                                "rank", rank.getTag(landOwner),
                                "from", fromRank.getTag(landOwner)
                        ));
                        color = ChatColor.DARK_GREEN;
                    }
                } else {
                    item = getIcon("not-set");
                    color = ChatColor.GRAY;
                }

                boolean defaultValue = rank.getPermissionValue(perm);
                if (fromRank == rank && landOwner.getPermissionValue(rank, perm).orElse(false) != defaultValue) {
                    if (landOwner.getRank(res).above(rank) && res.hasPermission("settlement.perms.ranks.unset")) {
                        description.add(getText("entry.unset",
                                "perm", permName,
                                "rank", rank.getTag(landOwner),
                                "default", String.valueOf(defaultValue)
                        ));
                    }
                }
                if (rank.below(landOwner.getRank(res)) && res.hasPermission("settlement.ranks.perms.set") && res.hasPermission(perm)) {
                    if (value.orElse(false)) {
                        if (fromRank == rank) {
                            description.add(getText("entry.revoke",
                                    "perm", permName,
                                    "rank", rank.getTag(landOwner)
                            ));
                        }
                    } else {
                        description.add(getText("entry.grant",
                                "perm", permName,
                                "rank", rank.getTag(landOwner)
                        ));
                    }
                }

                List<String> text = new ArrayList<>();
                text.add(getText("entry.title",
                        "color", color.toString(),
                        "perm", permName,
                        "rank", rank.getTag(landOwner),
                        "description", Message.orElse("command." + perm + ".permission.description", Message.get("command." + perm + ".description")).getText()
                ));
                text.addAll(description);
                return new StaticGuiElement('g', item, click -> {
                    if (rank.below(landOwner.getRank(res))) {
                        if (click.getType() != ClickType.MIDDLE) {
                            if (landOwner.hasPermission(res, "ranks.perms.set") && landOwner.hasPermission(res, perm)) {
                                Optional<Boolean> currentValue = landOwner.getEffectivePermissionValue(rank, perm);
                                R currentFromRank = rank;
                                if (currentValue.orElse(false)) {
                                    for (Object r : rank.getInheritedRanks()) {
                                        if (landOwner.getEffectivePermissionValue((R) r, perm).orElse(false)) {
                                            currentFromRank = (R) r;
                                        }
                                    }
                                }
                                if (!currentValue.orElse(false) || currentFromRank == rank) {
                                    if (landOwner.setPermission(rank, perm, !currentValue.orElse(false))) {
                                        landOwner.save();
                                        gui.draw();
                                    }
                                }
                            }
                        } else {
                            if (landOwner.hasPermission(res, "ranks.perms.unset")) {
                                if (landOwner.setDefaultPermission(rank, perm)) {
                                    landOwner.save();
                                    gui.draw();
                                }
                            }
                        }
                    }
                    return true;
                }, text.toArray(new String[0]));
            }));
        }

        gui.addElement(permsGroup);

        GuiElementGroup ranksGroup = createGroup('r');

        landOwner.getRanks().stream().sorted(Comparator.reverseOrder()).forEachOrdered(r -> {
            if (landOwner.useGuestRank() && r == landOwner.getDefaultRank()) {
                // Can't configure guest rank
                return;
            }
            ranksGroup.addElement(new DynamicGuiElement('r', h -> {
                ItemStack item = r.getItem();
                boolean selected = r == rank;
                if (selected) {
                    ItemUtils.setGlowing(item, true);
                }
                return new StaticGuiElement('r', item, click -> {
                    rank = r;
                    click.getGui().draw();
                    return true;
                }, getText("rank-entry." + (selected ? "selected" : "selectable"),
                        "name", r.getTag(landOwner),
                        "state", landOwner.getStateTag()
                ));
            }));
        });

        gui.addElements(ranksGroup);

        gui.addElement(new DynamicGuiElement('t', h -> new StaticGuiElement('t', getIcon("titles"), click -> {
            if (landOwner.hasPermission(res, "titles")) {
                new TitlesGUI<>(type, res, landOwner, rank).open();
            }
            return true;
        }, getText("titles",
                "rank", rank.getTag(landOwner),
                "state", landOwner.getStateTag(),
                "action", landOwner.hasPermission(res, "titles") ? getText("unlockables.unlocked.change") : getText("no-permission")
        ))));

        gui.addElement(new DynamicGuiElement('o', h -> new StaticGuiElement('o', getIcon("reset"), click -> {
            new ConfirmationGui(type + ".reset-rank-perms", c -> {
                if (landOwner.hasPermission(res, "perms.reset")) {
                    boolean changed = false;
                    for (String permission : rank.getPermissions()) {
                        changed |= landOwner.setDefaultPermission(rank, permission);
                    }
                    if (changed) {
                        landOwner.save();
                    }
                }
                InventoryGui.goBack(c.getWhoClicked());
            }, c -> InventoryGui.goBack(c.getWhoClicked()),
                    "state", landOwner.getStateTag(),
                    "name", landOwner.getName(),
                    "rank", rank.getTag(landOwner)
            ).open((Player) click.getEvent().getWhoClicked());
            return true;
        }, getText("reset",
                "rank", rank.getTag(landOwner),
                "state", landOwner.getStateTag(),
                "action", landOwner.hasPermission(res, "perms.reset") ? getText("unlockables.unlocked.reset") : getText("no-permission")
        ))));

        gui.addElement(new GuiBackElement('b', getIcon("back"), true, getText("back")));
    }

    public void open() {
        gui.show(res.getPlayer());
    }
}
