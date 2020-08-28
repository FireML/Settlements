package me.crz.minecraft.kingdoms.village.gui;

import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.chat.ChatManager;
import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.command.village.VillageTitlesCommand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class TitlesGUI extends KingdomsGUI {

    private final Resident resident;
    public CInventory gui;

    public TitlesGUI(Resident resident, VillageRank rank) {
        super("titles");
        this.resident = resident;
        gui = new CInventory(getText("title",
                "state", resident.hasVillage() ? resident.getVillage().getStateTag() : "",
                "rank", rank.getTag(resident.getVillage())),
                InventorySize.SIX) {};

        for (int i = 0; i < InventorySize.TWO.getSize(); i++) {
            gui.addItem(i, BORDER);
        }

        for (String title : resident.getVillage().getTitles(rank)) {
            gui.addItem(new ClickableItem() {
                @Override
                public ItemStack getItem(Player p) {
                    CItem item = new CItem(Material.PAPER).setName(getMessage("entry.title").getReplacedText(
                            "title", title,
                            "rank", rank.getTag(resident.getVillage())
                    ));
                    if (title.equals(resident.getVillage().getDefaultTitle(rank))) {
                        item.addLore(getMessage("entry.default").getReplacedTextList(
                                "title", title,
                                "rank",  rank.getTag(resident.getVillage())
                        ));
                        item.glow(true);
                    } else if (resident.hasPermission("village.titles.set")) {
                        item.addLore(getMessage("entry.set").getReplacedTextList(
                                "title", title,
                                "rank",  rank.getTag(resident.getVillage())
                        ));
                    }
                    if (resident.hasPermission("village.titles.remove") && !rank.above(resident.getVillageRank())) {
                        item.addLore(getMessage("entry.remove").getReplacedTextList(
                                "title", title,
                                "rank",  rank.getTag(resident.getVillage())
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
                                if (resident.hasPermission("village.titles.set")) {
                                    String oldRankName = rank.getTag(resident.getVillage());
                                    resident.getVillage().setDefaultTitle(rank, title);
                                    Message.get("command.village.titles.set.success").tell(resident, "title", title, "rank", oldRankName);
                                    new TitlesGUI(resident, rank).open();
                                }
                                return true;
                            case RIGHT:
                            case SHIFT_RIGHT:
                                if (resident.hasPermission("village.titles.remove")) {
                                    resident.getVillage().removeTitle(rank, title);
                                    Message.get("command.village.titles.remove.success").tell(resident, "title", title, "rank", rank.getTag(resident.getVillage()));
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

        if (resident.hasPermission("village.titles.add") && !rank.above(resident.getVillageRank()) && resident.getVillage().getTitles(rank).size() < Utils.getTitlesPerRank()) {
            gui.addItem(InventorySize.FIVE.getSize() + 8, new ClickableItem() {
                @Override
                public ItemStack getItem(Player p) {
                    return new CItem(Material.WRITABLE_BOOK).setName(getMessage("add.title").getReplacedText(
                            "rank", rank.getTag(resident.getVillage()),
                            "state", resident.getVillage().getStateTag()
                    )).build();
                }

                @Override
                public boolean click(Player p, ClickType c) {
                    if (resident.hasPermission("village.titles.add")) {
                        new AnvilGUI.Builder()
                                .item(new ItemStack(Material.WRITABLE_BOOK))
                                .title(getMessage("add.anvil.title").getReplacedText(
                                        "rank", rank.getTag(resident.getVillage()),
                                        "state", resident.getVillage().getStateTag()
                                ))
                                .text(getMessage("add.anvil.placeholder").getReplacedText(
                                        "rank", rank.getTag(resident.getVillage()),
                                        "state", resident.getVillage().getStateTag()
                                ))
                                .onClose(player -> Utils.runLater(() -> new TitlesGUI(resident, rank).open(), 1))
                                .onComplete((player, text) -> {
                                    text = ChatColor.stripColor(text);
                                    if (rank.above(resident.getVillageRank())) {
                                        return AnvilGUI.Response.text(getError("cant-edit-higher").getReplacedText("rank", resident.getVillageRank().getTag(resident.getVillage())));
                                    } else if (resident.getVillage().getTitles(rank).size() >= Utils.getTitlesPerRank()) {
                                        return AnvilGUI.Response.text(getError("too-many").getReplacedText(
                                                "rank", resident.getVillageRank().getTag(),
                                                "max", String.valueOf(Utils.getTitlesPerRank())
                                        ));
                                    } else if (text.length() > 16) {
                                        return AnvilGUI.Response.text(getError("too-long").getReplacedText("input", text, "amount", "16"));
                                    } else if (!VillageTitlesCommand.TITLE_PATTERN.matcher(text).matches()) {
                                        return AnvilGUI.Response.text(getError("invalid-characters").getReplacedText("input", text));
                                    } else if (ChatManager.getInstance().containsAnyCurses(text)) {
                                        return AnvilGUI.Response.text(getError("prohibited-words").getReplacedText("input", text));
                                    } else if (resident.getVillage().addTitle(rank, text)) {
                                        Message.get("command.village.titles.add.success").tell(resident, "title", text, "rank", rank.getTag(resident.getVillage()));
                                        return AnvilGUI.Response.close();
                                    } else {
                                        return AnvilGUI.Response.text(getError("already-exists").getReplacedText("input", text, "rank", rank.getTag(resident.getVillage())));
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
