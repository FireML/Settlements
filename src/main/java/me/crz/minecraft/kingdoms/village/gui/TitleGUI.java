package me.crz.minecraft.kingdoms.village.gui;

import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class TitleGUI extends KingdomsGUI {
    private final Resident resident;
    public CInventory gui;

    public TitleGUI(Resident resident) {
        super("title");
        this.resident = resident;
        gui = new CInventory(getText("title",
                "state", resident.hasVillage() ? resident.getVillage().getStateTag() : "",
                "title", resident.getTitle()),
                InventorySize.ONE) {};

        for (int i = 0; i < InventorySize.TWO.getSize(); i++) {
            gui.addItem(i, BORDER);
        }

        VillageRank rank = resident.getVillageRank();
        for (String title : resident.getVillage().getTitles(rank)) {
            gui.addItem(new ClickableItem() {
                @Override
                public ItemStack getItem(Player p) {
                    CItem item = new CItem(Material.PAPER).setName(getMessage("entry.title").getReplacedText(
                            "title", title,
                            "rank", rank.getTag(resident.getVillage())
                    ));
                    if (title.equals(resident.getTitle())) {
                        item.addLore(getMessage("entry.current").getReplacedTextList(
                                "title", title,
                                "rank",  rank.getTag(resident.getVillage())
                        ));
                        item.glow(true);
                    } else {
                        item.addLore(getMessage("entry.select").getReplacedTextList(
                                "title", title,
                                "rank",  rank.getTag(resident.getVillage())
                        ));
                    }
                    return item.build();
                }

                @Override
                public boolean click(Player p, ClickType type) {
                    if (resident.hasVillage() && resident.hasPermission("village.title") && rank == resident.getVillageRank() && resident.setTitle(title)) {
                        Utils.runAsync(() -> DatabaseManager.getInstance().saveResident(resident));
                        open();
                        Message message = Message.get("command.village.title.success." + resident.getVillage().getState().getKey() + "." + resident.getVillageRank().getKey());
                        if (!message.exists()) {
                            message = Message.get("command.village.title.success.default");
                        }
                        message.tell(resident.getVillage().getOnlineResidents(),
                                "rank", resident.getVillageRank().getTag(resident.getVillage()),
                                "title", resident.getTitle(),
                                "name", resident.getName(),
                                "villagename", resident.getVillage().getName(),
                                "villagestate", resident.getVillage().getState().getTag()
                        );
                        return true;
                    }
                    return false;
                }
            });
        }

        for (int i = InventorySize.FOUR.getSize(); i < InventorySize.SIX.getSize(); i++) {
            gui.addItem(i, BORDER);
        }

        gui.addItem(InventorySize.FIVE.getSize() + 8, new ClickableItem() {
            @Override
            public ItemStack getItem(Player p) {
                if (resident.hasVillage() && resident.hasPermission("village.titles")) {
                    return new CItem(Material.WRITABLE_BOOK).setName(getText("edit")).build();
                }
                return null;
            }

            @Override
            public boolean click(Player p, ClickType clickType) {
                if (resident.hasVillage() && resident.hasPermission("village.titles")) {
                    new RankSelectGui(resident, rank -> new TitlesGUI(resident, rank).open()).open();
                }
                return true;
            }
        });

        gui.addItem(InventorySize.FIVE.getSize() + 4, new ClickableItem() {
            public ItemStack getItem(final Player p) {
                return new CItem(Material.BARRIER).setName(getText("back")).build();
            }

            public boolean click(final Player p, final ClickType clickType) {
                new SettingsGUI(resident).open();
                return true;
            }
        });
    }

    public void open() {
        gui.build(resident.getPlayer());
    }
}
