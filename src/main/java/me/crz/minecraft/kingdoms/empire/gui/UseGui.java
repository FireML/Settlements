package me.crz.minecraft.kingdoms.empire.gui;

import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.landflags.BuildState;
import me.crz.minecraft.kingdoms.landflags.Use;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
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
                    Empire empire = EmpireManager.getInstance().getEmpireFromPlayer(p);
                    if (empire != null) {
                        BuildState state = empire.getUseState(u);
                        return u.getItem().setName(getMessage("entry").getReplacedText(
                                "flag", u.getTag(),
                                "state", state.getTag()
                        )).build();
                    }
                    return null;
                }

                public boolean click(Player p, ClickType clickType) {
                    Resident res = ResidentManager.getInstance().getResident(p);
                    if (res.hasEmpire() && res.getEmpireRank().atLeast(EmpireRank.LEADER)) {
                        BuildState changeTo = clickType.isLeftClick()
                                ? res.getVillage().getUseState(u).next(BuildState.AppliesTo.EMPIRE)
                                : res.getVillage().getUseState(u).previous(BuildState.AppliesTo.EMPIRE);
                        res.getVillage().getEmpire().setUseState(u, changeTo);
                        MessageUtils.tell(p, Message.get("command.empire.use.success"),
                                "flag", u.getTag(),
                                "state", changeTo.getTag()
                        );
                        Utils.runAsync(() -> DatabaseManager.getInstance().saveEmpire(res.getVillage().getEmpire()));
                        gui.build(p);
                        return true;
                    }
                    return false;
                }
            });
        }

        int rest = gui.getSize() % 9;
        int slot = rest == 0 ? gui.getSize() : gui.getSize() + (9 - rest);
        for (BuildState state : BuildState.values(BuildState.AppliesTo.EMPIRE)) {
            gui.addItem(slot++, new ClickableItem() {
                @Override
                public ItemStack getItem(Player p) {
                    if (ResidentManager.getInstance().doesResidentHaveEmpire(p)) {
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
                    if (res.hasEmpire() && res.getVillageRank().atLeast(EmpireRank.LEADER)) {
                        for (Use u : Use.values()) {
                            res.getVillage().getEmpire().setUseState(u, state);
                        }
                        MessageUtils.tell(p, Message.get("command.empire.use.success-all"),
                                "state", state.getTag()
                        );
                        Utils.runAsync(() -> DatabaseManager.getInstance().saveEmpire(res.getVillage().getEmpire()));
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
                SettingsGUI.getInstance().open(p);
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
