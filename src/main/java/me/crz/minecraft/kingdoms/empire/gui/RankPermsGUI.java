package me.crz.minecraft.kingdoms.empire.gui;

import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.rank.RankManager;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RankPermsGUI extends KingdomsGUI {
    private static Map<String, RankPermsGUI> instances = new ConcurrentHashMap<>();
    public CInventory gui;
    private Map<UUID, Integer> playerPage = new HashMap<>();

    private RankPermsGUI(EmpireRank rank) {
        super("rank-perms");
        gui = new CInventory(getText("title", "rank", rank.getTag()), InventorySize.SIX) {};

        List<String> perms = EmpireRank.getToggleablePermissions().stream()
                .filter(s -> !s.contains(".help"))
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        boolean paged = perms.size() > InventorySize.SIX.getSize() - 1;
        for (int i = 0; i < (paged ? InventorySize.FIVE.getSize() : InventorySize.SIX.getSize()); i++) {
            int finalI = i;
            gui.addItem(i, new ClickableItem() {
                @Override
                public ItemStack getItem(Player p) {
                    int permIndex = getPage(p) * InventorySize.FIVE.getSize() + finalI;
                    if (perms.size() <= permIndex) {
                        return null;
                    }
                    String perm = perms.get(permIndex);
                    Resident res = ResidentManager.getInstance().getResident(p);
                    if (res.hasEmpire()) {
                        String permName = WordUtils.capitalizeFully(perm.replace('.', ' '));
                        if (permName.startsWith("Empire ")) {
                            permName = permName.substring("Empire ".length());
                        }
                        CItem item;

                        Optional<Boolean> value = res.getVillage().getEmpire().getEffectivePermissionValue(rank, perm);

                        EmpireRank fromRank = rank;
                        ChatColor color;
                        if (value.orElse(false)) {
                            for (EmpireRank r : rank.getInheritedRanks()) {
                                if (res.getVillage().getEmpire().getEffectivePermissionValue(r, perm).orElse(false)) {
                                    fromRank = r;
                                }
                            }
                            if (fromRank == rank) {
                                item = new CItem(Material.LIME_WOOL);
                                color = ChatColor.GREEN;
                            } else {
                                item = new CItem(Material.GREEN_WOOL).addLore(getMessage("entry.inherited").getReplacedTextList(
                                        "perm", permName,
                                        "rank", rank.getTag(),
                                        "from", fromRank.getTag()
                                ));
                                color = ChatColor.DARK_GREEN;
                            }
                        } else {
                            item = new CItem(Material.RED_WOOL);
                            color = ChatColor.RED;
                        }

                        boolean defaultValue = rank.getInheritedPermissionValue(perm).orElse(false);
                        if (res.getVillage().getEffectivePermissionValue(rank, perm).orElse(false) != defaultValue) {
                            if (res.getVillageRank().above(rank) && res.hasPermission("kingdoms.command.empire.perms.unset")) {
                                item.addLore(getMessage("entry.unset").getReplacedTextList(
                                        "perm", permName,
                                        "rank", rank.getTag(),
                                        "default", String.valueOf(defaultValue)
                                ));
                            }
                        }
                        if (res.getEmpireRank().above(rank) && res.hasPermission("kingdoms.command.empire.perms.set") && res.hasPermission("kingdoms.command." + perm)) {
                            if (value.orElse(false)) {
                                item.addLore(getMessage("entry.revoke").getReplacedTextList(
                                        "perm", permName,
                                        "rank", rank.getTag()
                                ));
                            } else {
                                item.addLore(getMessage("entry.grant").getReplacedTextList(
                                        "perm", permName,
                                        "rank", rank.getTag()
                                ));
                            }
                        } else {
                            item.addLore(new ArrayList<>());
                        }

                        return item.setName(getText("entry.title",
                                "color", color.toString(),
                                "perm", permName,
                                "rank", rank.getTag(),
                                "description", Message.get("command." + perm + ".description").getText()
                        )).build();
                    }
                    return null;
                }

                @Override
                public boolean click(Player p, ClickType type) {
                    int permIndex = getPage(p) * InventorySize.FIVE.getSize() + finalI;
                    if (perms.size() < permIndex) {
                        return false;
                    }
                    String perm = perms.get(permIndex);
                    Resident res = ResidentManager.getInstance().getResident(p);
                    if (res.hasEmpire() && res.getEmpireRank().above(rank)) {
                        if (type != ClickType.MIDDLE) {
                            if (res.hasPermission("kingdoms.command.empire.perms.set") && res.hasPermission("kingdoms.command." + perm)) {
                                Optional<Boolean> value = res.getVillage().getEmpire().getEffectivePermissionValue(rank, perm);
                                if (value.isPresent() && res.getVillage().getEmpire().setPermission(rank, perm, !value.get())) {
                                    Utils.runAsync(() -> DatabaseManager.getInstance().save(res.getVillage().getEmpire()));
                                    gui.build(p);
                                    return true;
                                }
                            }
                        } else {
                            if (res.hasPermission("kingdoms.command.empire.perms.unset")) {
                                if (res.getVillage().getEmpire().setDefaultPermission(rank, perm)) {
                                    Utils.runAsync(() -> DatabaseManager.getInstance().save(res.getVillage().getEmpire()));
                                    gui.build(p);
                                    return true;
                                }
                            }
                        }
                    }
                    return false;
                }
            });
        }

        if (paged) {
            gui.addItem(InventorySize.FIVE.getSize(), new ClickableItem() {
                public ItemStack getItem(final Player p) {
                    if (getPage(p) > 0) {
                        return new CItem(Material.SIGN).setName(getText("previous")).build();
                    }
                    return null;
                }

                public boolean click(final Player p, final ClickType clickType) {
                    int page = getPage(p);
                    if (page > 0) {
                        setPage(p, page - 1);
                        gui.build(p);
                        return true;
                    }
                    return false;
                }
            });

            gui.addItem(InventorySize.FIVE.getSize() + 4, new ClickableItem() {
                public ItemStack getItem(final Player p) {
                    return new CItem(Material.BARRIER).setName(getText("back")).build();
                }

                public boolean click(final Player p, final ClickType clickType) {
                    PermsGUI.getInstance().open(p);
                    return true;
                }
            });

            gui.addItem(InventorySize.FIVE.getSize() + 8, new ClickableItem() {
                public ItemStack getItem(final Player p) {
                    if (perms.size() > (getPage(p) + 1) * InventorySize.FIVE.getSize()) {
                        return new CItem(Material.SIGN).setName(getText("next")).build();
                    }
                    return null;
                }

                public boolean click(final Player p, final ClickType clickType) {
                    int page = getPage(p);
                    if (perms.size() > (page + 1) * InventorySize.FIVE.getSize()) {
                        setPage(p, page + 1);
                        gui.build(p);
                        return true;
                    }
                    return false;
                }
            });
        } else {
            gui.addItem(InventorySize.FIVE.getSize() + 8, new ClickableItem() {
                public ItemStack getItem(final Player p) {
                    return new CItem(Material.BARRIER).setName(getText("back")).build();
                }

                public boolean click(final Player p, final ClickType clickType) {
                    PermsGUI.getInstance().open(p);
                    return true;
                }
            });
        }
    }

    private void setPage(Player p, int page) {
        if (page == 0) {
            playerPage.remove(p.getUniqueId());
        } else {
            playerPage.put(p.getUniqueId(), page);
        }
    }

    private int getPage(Player p) {
        return playerPage.getOrDefault(p.getUniqueId(), 0);
    }

    public void open(final Player p) {
        playerPage.remove(p.getUniqueId());
        gui.build(p);
    }

    public static RankPermsGUI getInstance(EmpireRank rank) {
        return instances.computeIfAbsent(rank.name(), n -> new RankPermsGUI(rank));
    }
}
