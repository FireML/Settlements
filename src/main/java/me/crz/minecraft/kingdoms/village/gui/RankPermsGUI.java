package me.crz.minecraft.kingdoms.village.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.GuiPageElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.rank.PermissionGroup;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.ItemUtils;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.resident.Resident;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RankPermsGUI extends KingdomsGUI {
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

    public RankPermsGUI(Resident res, VillageRank rank, PermissionGroup group) {
        super("rank-perms");
        this.res = res;
        List<String> perms = group.getPermissions().stream()
                .filter(s -> !s.contains(".help"))
                .collect(Collectors.toList());
        gui = new InventoryGui(KingdomsPlugin.getInstance(), getText("title",
                "rank", rank.getTag(res.getVillage()),
                "group", group.getName()
        ), SETUP);
        gui.setCloseAction(close -> false);
        gui.setFiller(BORDER_ITEM);
        gui.addElement(new GuiPageElement('p', ItemUtils.toIcon(Material.OAK_SIGN), GuiPageElement.PageAction.PREVIOUS, getText("previous")));
        gui.addElement(new GuiPageElement('n', ItemUtils.toIcon(Material.OAK_SIGN), GuiPageElement.PageAction.NEXT, getText("next")));

        GuiElementGroup g = new GuiElementGroup('g');

        for (String perm : perms) {
            g.addElement(new DynamicGuiElement('g', (h) -> {
                if (!res.hasVillage()) {
                    return null;
                }

                String permName = Message.get("command." + perm + ".permission.name").orElse(WordUtils.capitalizeFully(perm.replace('.', ' ')));
                if (permName.startsWith("Village ")) {
                    permName = permName.substring("Village ".length());
                }
                Material item;
                Optional<Boolean> value = res.getVillage().getEffectivePermissionValue(rank, perm);
                List<String> description = new ArrayList<>();

                VillageRank fromRank = rank;
                ChatColor color;
                if (value.orElse(false)) {
                    for (VillageRank r : rank.getInheritedRanks()) {
                        if (res.getVillage().getEffectivePermissionValue(r, perm).orElse(false)) {
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
                                "rank", rank.getTag(res.getVillage()),
                                "from", fromRank.getTag(res.getVillage())
                        ));
                        color = ChatColor.DARK_GREEN;
                    }
                } else {
                    item = Material.RED_WOOL;
                    color = ChatColor.RED;
                }

                boolean defaultValue = rank.getPermissionValue(perm);
                if (fromRank == rank && res.getVillage().getPermissionValue(rank, perm).orElse(false) != defaultValue) {
                    if (res.getVillageRank().above(rank) && res.hasPermission("village.perms.unset")) {
                        description.add(getText("entry.unset",
                                "perm", permName,
                                "rank", rank.getTag(res.getVillage()),
                                "default", String.valueOf(defaultValue)
                        ));
                    }
                }
                if (res.getVillageRank().above(rank) && res.hasPermission("village.perms.set") && res.hasPermission(perm)) {
                    if (value.orElse(false)) {
                        if (fromRank == rank) {
                            description.add(getText("entry.revoke",
                                    "perm", permName,
                                    "rank", rank.getTag(res.getVillage())
                            ));
                        }
                    } else {
                        description.add(getText("entry.grant",
                                "perm", permName,
                                "rank", rank.getTag(res.getVillage())
                        ));
                    }
                }

                List<String> text = new ArrayList<>();
                text.add(getText("entry.title",
                        "color", color.toString(),
                        "perm", permName,
                        "rank", rank.getTag(res.getVillage()),
                        "description", Message.orElse("command." + perm + ".permission.description", Message.get("command." + perm + ".description")).getText()
                ));
                text.addAll(description);
                return new StaticGuiElement('g', ItemUtils.toIcon(item), click -> {
                    if (res.hasVillage() && res.getVillageRank().above(rank)) {
                        if (click.getType() != ClickType.MIDDLE) {
                            if (res.hasPermission("village.perms.set") && res.hasPermission(perm)) {
                                Optional<Boolean> currentValue = res.getVillage().getEffectivePermissionValue(rank, perm);
                                VillageRank currentFromRank = rank;
                                if (currentValue.orElse(false)) {
                                    for (VillageRank r : rank.getInheritedRanks()) {
                                        if (res.getVillage().getEffectivePermissionValue(r, perm).orElse(false)) {
                                            currentFromRank = r;
                                        }
                                    }
                                }
                                if (!currentValue.orElse(false) || currentFromRank == rank) {
                                    if (res.getVillage().setPermission(rank, perm, !currentValue.orElse(false))) {
                                        DatabaseManager.getInstance().save(res.getVillage());
                                        gui.draw();
                                    }
                                }
                            }
                        } else {
                            if (res.hasPermission("village.perms.unset")) {
                                if (res.getVillage().setDefaultPermission(rank, perm)) {
                                    DatabaseManager.getInstance().save(res.getVillage());
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
