package me.crz.minecraft.kingdoms.village.gui;

import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.landflags.BuildState;
import me.crz.minecraft.kingdoms.landflags.Use;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.stream.Collectors;

public class UseGui extends KingdomsGUI {
    private static UseGui instance;
    private CInventory gui;

    public UseGui() {
        super("use");
        gui = new CInventory(getText("title"), InventorySize.FOUR) {};

        for (Use u : Use.values()) {
            gui.addItem(new ClickableItem() {
                public ItemStack getItem(Player p) {
                    Village village = VillageManager.getInstance().getVillageFromPlayer(p);
                    if (village != null) {
                        BuildState state = village.getUseState(u);
                        return u.getItem().setName(getMessage("entry").getReplacedText(
                                "flag", u.getTag(),
                                "state", state.getTag()
                        )).build();
                    }
                    return null;
                }

                public boolean click(Player p, ClickType clickType) {
                    Resident res = ResidentManager.getInstance().getResident(p);
                    if (res.hasVillage() && res.hasPermission("village.use")) {
                        BuildState changeTo = clickType.isLeftClick()
                                ? res.getVillage().getUseState(u).next(BuildState.AppliesTo.SETTLEMENT)
                                : res.getVillage().getUseState(u).previous(BuildState.AppliesTo.SETTLEMENT);
                        res.getVillage().setUseState(u, changeTo);
                        MessageUtils.tell(p, Message.get("command.village.use.success"),
                                "flag", u.getTag(),
                                "state", changeTo.getTag()
                        );
                        Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(res.getVillage()));
                        gui.build(p);
                        return true;
                    }
                    return false;
                }
            });
        }

        int rest = gui.getSize() % 9;
        int slot = rest == 0 ? gui.getSize() : gui.getSize() + (9 - rest);
        for (BuildState state : BuildState.values(BuildState.AppliesTo.SETTLEMENT)) {
            gui.addItem(slot++, new ClickableItem() {
                @Override
                public ItemStack getItem(Player p) {
                    if (ResidentManager.getInstance().doesResidentHaveVillage(p)) {
                        return state.getItem()
                                .setName(getMessage("toggle-all").getReplacedText("state", state.getTag()))
                                .setLore(getMessage("toggle-all").getReplacedTextList("state", state.getTag()).stream().skip(1).collect(Collectors.toList()))
                                .build();
                    }
                    return null;
                }

                @Override
                public boolean click(Player p, ClickType clickType) {
                    Resident res = ResidentManager.getInstance().getResident(p);
                    if (res.hasVillage() && res.hasPermission("village.use")) {
                        for (Use u : Use.values()) {
                            res.getVillage().setUseState(u, state);
                        }
                        MessageUtils.tell(p, Message.get("command.village.use.success-all"),
                                "state", state.getTag()
                        );
                        Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(res.getVillage()));
                        gui.build(p);
                        return true;
                    }
                    return false;
                }
            });
        }

        gui.addItem((slot / 9 ) * 9 + 8, new ClickableItem() {
            public ItemStack getItem(Player p) {
                return new CItem(Material.BARRIER)
                        .setName(getText("back"))
                        .setLore(getMessage("back").getTextList().stream().skip(1).collect(Collectors.toList()))
                        .build();
            }

            public boolean click(Player p, ClickType clickType) {
                new SettingsGUI(p).open();
                return true;
            }
        });
    }

    public void open(Player p) {
        gui.build(p);
    }

    public static UseGui getInstance() {
        if (instance == null) {
            instance = new UseGui();
        }
        return instance;
    }
}
