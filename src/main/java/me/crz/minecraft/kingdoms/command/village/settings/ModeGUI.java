package me.crz.minecraft.kingdoms.command.village.settings;

import me.crz.minecraft.kingdoms.command.village.Mode;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.KingdomsGUI;
import org.bukkit.event.inventory.ClickType;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import org.bukkit.Material;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.cinventory.CInventory;

public class ModeGUI extends KingdomsGUI {
    private static ModeGUI instance;
    public CInventory gui;

    public String yesOrNo(final boolean b) {
        return b ? "Yes" : "No";
    }

    private ModeGUI() {
        super("mode");
        gui = new CInventory(getText("title"), InventorySize.ONE) {};

        for (Mode m : Mode.values()) {
            gui.addItem(new ClickableItem() {
                public ItemStack getItem(Player p) {
                    Village village = VillageManager.getInstance().getVillageFromPlayer(p);
                    if (village != null) {
                        String state = yesOrNo(m.get(village));
                        return m.getItem().setName(getMessage("entry.title").getReplacedText(
                                "mode", m.getTag(),
                                "value", state
                        )).setLore(getMessage("entry.lore").getReplacedTextList(
                                "mode", m.getTag(),
                                "value", state
                        )).build();
                    }
                    return null;
                }

                public boolean click(Player p, ClickType clickType) {
                    Resident res = ResidentManager.getInstance().getResident(p);
                    if (res.hasVillage() && res.getVillageRank().atLeast(VillageRank.CHIEF)) {
                        boolean state = m.get(res.getVillage());
                        m.set(state, res.getVillage());
                        MessageUtils.tell(p, Message.get("command.village.mode.success"),
                                "mode", m.getTag(),
                                "value", yesOrNo(!state)
                        );
                        gui.build(p);
                        return true;
                    }
                    return false;
                }
            });
        }
        gui.addItem((gui.getMaxSlot() / 9 ) * 9 + 8, new ClickableItem() {
            public ItemStack getItem(final Player p) {
                return new CItem(Material.BARRIER).setName(getText("back")).build();
            }

            public boolean click(final Player p, final ClickType clickType) {
                SettingsGUI.getInstance().open(p);
                return true;
            }
        });
    }

    public void open(final Player p) {
        gui.build(p);
    }

    public static ModeGUI getInstance() {
        if (instance == null) {
            instance = new ModeGUI();
        }
        return instance;
    }
}
