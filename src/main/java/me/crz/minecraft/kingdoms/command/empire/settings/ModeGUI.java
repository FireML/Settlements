package me.crz.minecraft.kingdoms.command.empire.settings;

import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CInventoryManager;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.command.empire.EmpireMode;
import me.crz.minecraft.kingdoms.command.village.settings.SettingsGUI;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageGUI;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;


public class ModeGUI extends VillageGUI {
    private static ModeGUI instance;
    public CInventory gui;

    public String yesOrNo(final boolean b) {
        return b ? "Yes" : "No";
    }

    public ModeGUI() {
        super("mode");
        gui = new CInventory(getText("title"), InventorySize.ONE) {};

        for (EmpireMode m : EmpireMode.values()) {
            gui.addItem(new ClickableItem() {
                public ItemStack getItem(Player p) {
                    Village village = VillageManager.getInstance().getVillageFromPlayer(p);
                    if (village != null && village.hasEmpire()) {
                        String state = yesOrNo(m.get(village.getEmpire()));
                        return m.getItem().setName(getMessage("entry.title").getReplacedText(
                                "flag", m.getTag(),
                                "state", state
                        )).setLore(getMessage("entry.lore").getReplacedTextList(
                                "flag", m.getTag(),
                                "state", state
                        )).build();
                    }
                    return null;
                }

                public boolean click(Player p, ClickType clickType) {
                    Resident res = ResidentManager.getInstance().getResident(p);
                    if (res.hasEmpire() && res.getEmpireRank().atLeast(VillageRank.CHIEF)) {
                        boolean state = m.get(res.getVillage().getEmpire());
                        m.set(state, res.getVillage().getEmpire());
                        MessageUtils.tell(p, Message.get("command.village.mode.success"),
                                "flag", m.getTag(),
                                "state", yesOrNo(!state)
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
        CInventoryManager.getInstance().addCInvetory(gui);
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
