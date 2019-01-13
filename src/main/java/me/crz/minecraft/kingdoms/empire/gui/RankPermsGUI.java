package me.crz.minecraft.kingdoms.empire.gui;

import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RankPermsGUI extends KingdomsGUI {
    private static Map<String, RankPermsGUI> instances = new HashMap<>();
    public CInventory gui;

    private RankPermsGUI(String rankName) {
        super("rank-perms");
        gui = new CInventory(getText("title", "rank", rankName), InventorySize.ONE) {};

        EmpireManager.getInstance().getLeaderRank().getInheritedPermissions().keySet().stream().sorted(Comparator.naturalOrder()).forEachOrdered(perm -> {
            gui.addItem(new ClickableItem() {
                @Override
                public ItemStack getItem(Player p) {
                    EmpireRank rank = EmpireRank.get(rankName);
                    Resident res = ResidentManager.getInstance().getResident(p);
                    if (rank != null && res.hasEmpire()) {
                        String permName = WordUtils.capitalizeFully(perm.replace('.', ' '));
                        if (permName.startsWith("Empire ")) {
                            permName = permName.substring("Empire ".length());
                        }
                        CItem item;
                        Optional<Boolean> value = res.getVillage().getEmpire().getInheritedPermissionValue(rank, perm);
                        Optional<Boolean> rankValue = rank.getInheritedPermissionValue(perm);
                        if (value.isPresent()) {
                            if (value.get()) {
                                item = new CItem(Material.LIME_WOOL);
                            } else {
                                item = new CItem(Material.RED_WOOL);
                            }
                            if (res.hasPermission("kingdoms.command.empire.perms.unset")) {
                                item.addLore(getMessage("entry.unset").getReplacedTextList(
                                        "perm", permName,
                                        "rank", rank.getTag()
                                ));
                            }
                        } else {
                            if (rankValue.isPresent()) {
                                if (rankValue.get()) {
                                    item = new CItem(Material.LIGHT_BLUE_WOOL);
                                } else {
                                    item = new CItem(Material.ORANGE_WOOL);
                                }
                            } else {
                                // This shouldn't be possible...
                                return null;
                            }
                        }
                        if (res.getVillageRank().above(rank)) {
                            if (res.hasPermission("kingdoms.command.empire.perms.set") && res.hasPermission("kingdoms.command." + perm)) {
                                if (value.orElse(false) || rankValue.orElse(false)) {
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
                            }
                        }

                        return item.setName(getText("entry.title",
                                "color", value.orElse(false) || rankValue.orElse(false) ? ChatColor.GREEN.toString() : ChatColor.RED.toString(),
                                "perm", permName,
                                "rank", rank.getTag(),
                                "description", Message.get("command." + perm + ".description").getText()
                        )).build();
                    }
                    return null;
                }

                @Override
                public boolean click(Player p, ClickType type) {
                    EmpireRank rank = EmpireRank.get(rankName);
                    Resident res = ResidentManager.getInstance().getResident(p);
                    if (rank != null && res.hasEmpire() && res.getEmpireRank().above(rank)) {
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
                                if (res.getVillage().getEmpire().unsetPermission(rank, perm)) {
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
        });

        gui.addItem((gui.getMaxSlot() / 9) * 9 + 8, new ClickableItem() {
            public ItemStack getItem(final Player p) {
                return new CItem(Material.BARRIER).setName(getText("back")).build();
            }

            public boolean click(final Player p, final ClickType clickType) {
                PermsGUI.getInstance().open(p);
                return true;
            }
        });
    }

    public void open(final Player p) {
        gui.build(p);
    }

    public static RankPermsGUI getInstance(EmpireRank rank) {
        return instances.computeIfAbsent(rank.name(), RankPermsGUI::new);
    }
}
