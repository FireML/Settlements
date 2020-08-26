package me.crz.minecraft.kingdoms.empire.gui;

import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.chat.ChatManager;
import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.command.village.VillageTitlesCommand;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class TitlesGUI extends KingdomsGUI {

    private final Resident resident;
    public CInventory gui;

    public TitlesGUI(Resident resident, EmpireRank rank) {
        super("titles");
        this.resident = resident;
        gui = new CInventory(getText("title",
                "state", resident.hasEmpire() ? resident.getVillage().getEmpire().getStateTag() : "",
                "rank", rank.getTag(resident.getVillage().getEmpire())),
                InventorySize.SIX) {};

        for (int i = 0; i < InventorySize.TWO.getSize(); i++) {
            gui.addItem(i, BORDER);
        }

        for (String title : resident.getVillage().getEmpire().getTitles(rank)) {
            gui.addItem(new ClickableItem() {
                @Override
                public ItemStack getItem(Player p) {
                    CItem item = new CItem(Material.PAPER).setName(getMessage("entry.title").getReplacedText(
                            "title", title,
                            "rank", rank.getTag(resident.getVillage().getEmpire())
                    ));
                    if (title.equals(resident.getVillage().getEmpire().getDefaultTitle(rank))) {
                        item.addLore(getMessage("entry.default").getReplacedTextList(
                                "title", title,
                                "rank",  rank.getTag(resident.getVillage().getEmpire())
                        ));
                        item.glow(true);
                    } else if (resident.hasPermission("empire.titles.set")) {
                        item.addLore(getMessage("entry.set").getReplacedTextList(
                                "title", title,
                                "rank",  rank.getTag(resident.getVillage().getEmpire())
                        ));
                    }
                    if (resident.hasPermission("empire.titles.remove") && !rank.above(resident.getEmpireRank())) {
                        item.addLore(getMessage("entry.remove").getReplacedTextList(
                                "title", title,
                                "rank",  rank.getTag(resident.getVillage().getEmpire())
                        ));
                    }
                    return item.build();
                }

                @Override
                public boolean click(Player p, ClickType type) {
                    if (resident.hasVillage()) {
                        switch (type) {
                            case LEFT:
                            case SHIFT_LEFT:
                                if (resident.hasPermission("empire.titles.set")) {
                                    String oldRankName = rank.getTag(resident.getVillage().getEmpire());
                                    resident.getVillage().getEmpire().setDefaultTitle(rank, title);
                                    Message.get("command.empire.titles.set.success").tell(resident, "title", title, "rank", oldRankName);
                                    new TitlesGUI(resident, rank).open();
                                }
                                return true;
                            case RIGHT:
                            case SHIFT_RIGHT:
                                if (resident.hasPermission("empire.titles.remove")) {
                                    resident.getVillage().getEmpire().removeTitle(rank, title);
                                    Message.get("command.empire.titles.remove.success").tell(resident, "title", title, "rank", rank.getTag(resident.getVillage().getEmpire()));
                                    new TitlesGUI(resident, rank).open();
                                    return true;
                                }
                                return false;
                        }
                        return true;
                    }
                    return false;
                }
            });
        }

        for (int i = InventorySize.FOUR.getSize(); i < InventorySize.SIX.getSize(); i++) {
            gui.addItem(i, BORDER);
        }

        if (resident.hasPermission("empire.titles.add") && !rank.above(resident.getEmpireRank()) && resident.getVillage().getEmpire().getTitles(rank).size() < Utils.getTitlesPerRank()) {
            gui.addItem(InventorySize.FIVE.getSize() + 8, new ClickableItem() {
                @Override
                public ItemStack getItem(Player p) {
                    return new CItem(Material.WRITABLE_BOOK).setName(getMessage("add.title").getReplacedText(
                            "rank", rank.getTag(resident.getVillage().getEmpire()),
                            "state", resident.getVillage().getEmpire().getStateTag()
                    )).build();
                }

                @Override
                public boolean click(Player p, ClickType c) {
                    if (resident.hasPermission("empire.titles.add")) {
                        new AnvilGUI.Builder()
                                .item(new ItemStack(Material.WRITABLE_BOOK))
                                .title(getMessage("add.anvil.title").getReplacedText(
                                        "rank", rank.getTag(resident.getVillage().getEmpire()),
                                        "state", resident.getVillage().getEmpire().getStateTag()
                                ))
                                .text(getMessage("add.anvil.placeholder").getReplacedText(
                                        "rank", rank.getTag(resident.getVillage().getEmpire()),
                                        "state", resident.getVillage().getEmpire().getStateTag()
                                ))
                                .onClose(player -> Utils.runLater(() -> open(), 1))
                                .onComplete((player, text) -> {
                                    text = ChatColor.stripColor(text);
                                    if (rank.above(resident.getEmpireRank())) {
                                        return AnvilGUI.Response.text(getError("cant-edit-higher").getReplacedText("rank", resident.getEmpireRank().getTag(resident.getVillage().getEmpire())));
                                    } else if (resident.getVillage().getEmpire().getTitles(rank).size() >= Utils.getTitlesPerRank()) {
                                        return AnvilGUI.Response.text(getError("too-many").getReplacedText(
                                                "rank", resident.getEmpireRank().getTag(),
                                                "max", String.valueOf(Utils.getTitlesPerRank())
                                        ));
                                    } else if (text.length() > 16) {
                                        return AnvilGUI.Response.text(getError("too-long").getReplacedText("input", text, "amount", "16"));
                                    } else if (!VillageTitlesCommand.TITLE_PATTERN.matcher(text).matches()) {
                                        return AnvilGUI.Response.text(getError("invalid-characters").getReplacedText("input", text));
                                    } else if (ChatManager.getInstance().containsAnyCurses(text)) {
                                        return AnvilGUI.Response.text(getError("prohibited-words").getReplacedText("input", text));
                                    } else if (resident.getVillage().getEmpire().addTitle(rank, text)) {
                                        Message.get("command.empire.titles.add.success").tell(resident, "title", text, "rank", rank.getTag(resident.getVillage().getEmpire()));
                                        Utils.runLater(() -> new TitlesGUI(resident, rank).open(), 1);
                                        return AnvilGUI.Response.close();
                                    } else {
                                        return AnvilGUI.Response.text(getError("already-exists").getReplacedText("input", text, "rank", rank.getTag(resident.getVillage().getEmpire())));
                                    }
                                })
                                .plugin(KingdomsPlugin.getInstance())
                                .open(p);
                        return true;
                    }
                    return false;
                }
            });
        }

        gui.addItem(InventorySize.FIVE.getSize() + 4, new ClickableItem() {
            public ItemStack getItem(final Player p) {
                return new CItem(Material.BARRIER).setName(getText("back")).build();
            }

            public boolean click(final Player p, final ClickType clickType) {
                new TitleGUI(resident).open();
                return true;
            }
        });
    }

    public void open() {
        gui.build(resident.getPlayer());
    }
}
