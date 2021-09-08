package de.themoep.settlements.missions;

import de.themoep.inventorygui.GuiStorageElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.utils.HumanReadable;
import de.themoep.settlements.utils.Utils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TurnInGUI extends SettlementsGUI {

    private final static int SIZE = 54;

    private final static String[] SETUP = {
            "ddddddddd",
            "ddddddddd",
            "ddddddddd",
            "ddddddddd",
            "ddddddddd",
            "ddddddddd"
    };

    private final InventoryGui gui;
    private final Inventory inv;
    private final Resident resident;
    private final MissionData missionData;

    public TurnInGUI(Resident resident, MissionData missionData) {
        super("turnin");
        this.resident = resident;
        this.missionData = missionData;

        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title", "mission", missionData.getMission().getTag()), SETUP);

        inv = Bukkit.createInventory(null, SIZE);

        gui.addElement(new GuiStorageElement('d', inv));

        gui.setCloseAction(close -> {
            ItemStack[] contents = inv.getContents();
            if (contents == null || contents.length == 0 || close.getPlayer() != this.resident.getPlayer()) {
                return true;
            }

            deposit(contents, this.resident);
            inv.clear();
            return true;
        });
    }

    public void open() {
        gui.show(resident.getPlayer());
    }

    private void deposit(ItemStack[] contents, Resident resident) {
        Map<Material, Long> fail = new HashMap<>();
        Player player = resident.getPlayer();
        if (!missionData.isFinished()) {
            Map<String, Long> total = new HashMap<>();
            Map<String, Long> deposited = new HashMap<>();

            for (String type : missionData.getMission().getRequirements().keySet()) {
                total.put(type.toUpperCase(Locale.ROOT), missionData.getProgress(type));
            }

            for (ItemStack item : contents) {
                if (item != null) {
                    String material = item.getType().name();
                    long required = missionData.getMission().getRequirement(material);
                    if (required > 0) {
                        if (required <= total.get(material)) {
                            addToInventory(player, item);
                            Utils.addAmount(fail, item.getType(), (long) item.getAmount());
                        } else {
                            long totalAdd = item.getAmount() + total.get(material);
                            if (required - totalAdd >= 0) {
                                Utils.addAmount(total, material, (long) item.getAmount());
                                Utils.addAmount(deposited, material, (long) item.getAmount());
                            } else {
                                long rv = required - total.get(material);
                                long drv = item.getAmount() - rv;
                                Utils.addAmount(total, material, item.getAmount() - drv);
                                Utils.addAmount(deposited, material, item.getAmount() - drv);
                                ItemStack temp = item.clone();
                                temp.setAmount(Math.toIntExact(drv));
                                addToInventory(player, temp);
                                Utils.addAmount(fail, temp.getType(), (long) temp.getAmount());
                            }
                        }
                    } else {
                        addToInventory(player, item);
                        Utils.addAmount(fail, item.getType(), (long) item.getAmount());
                    }
                }
            }
            if (!deposited.isEmpty()) {
                for (BaseComponent[] components : buildSuccessDepositMessage(resident, player, deposited)) {
                    player.sendMessage(components);
                }
                missionData.addProgress(deposited);
                if (missionData.isFinished()) {
                    missionData.getMission().finish(resident);
                }
                DatabaseManager.getInstance().saveMissionData(resident.getVillage());
            }
            if (!fail.isEmpty()) {
                for (BaseComponent[] components : buildFailDepositMessage(resident, fail, TextComponent.fromLegacyText(""))) {
                    player.sendMessage(components);
                }
            }
        } else if (contents.length > 0) {
            for (ItemStack item2 : contents) {
                if (item2 != null) {
                    Utils.addAmount(fail, item2.getType(), (long) item2.getAmount());
                    addToInventory(player, item2);
                }
            }

            for (BaseComponent[] components : buildFailDepositMessage(resident, fail, getMessage("mission-finished")
                    .getReplacedComponents("mission", missionData.getMission().getTag()))) {
                player.sendMessage(components);
            }
        }
    }

    private void addToInventory(Player player, ItemStack itemStack) {
        for (ItemStack rest : player.getInventory().addItem(itemStack).values()) {
            Item item = player.getWorld().dropItem(player.getLocation(), rest);
            SettlementsPlugin.getInstance().getServerAdapter().markOwned(item, player);
        }
    }

    private List<BaseComponent[]> buildFailDepositMessage(Resident resident, Map<Material, Long> fail, BaseComponent[] reason) {
        List<BaseComponent[]> messages = new ArrayList<>();
        messages.add(getMessage("fail-turn-in").getReplacedComponents("reason", reason));
        for (Map.Entry<Material, Long> entry : fail.entrySet()) {
            messages.add(getMessage("list-entry").getReplacedComponents(
                    "amount", String.valueOf(entry.getValue()),
                    "icon", Utils.getIcon(resident, entry.getKey().name()),
                    "material", HumanReadable.humanizedName(entry.getKey().name())));
        }
        return messages;
    }

    private List<BaseComponent[]> buildSuccessDepositMessage(Resident resident, Player p, Map<String, Long> deposited) {
        List<BaseComponent[]> messages = new ArrayList<>();
        messages.add(getMessage("success-turn-in").getReplacedComponents("resident", p.getDisplayName()));
        for (Map.Entry<String, Long> entry : deposited.entrySet()) {
            messages.add(getMessage("list-entry").getReplacedComponents(
                    "amount", String.valueOf(entry.getValue()),
                    "icon", Utils.getIcon(resident, entry.getKey()),
                    "material", HumanReadable.humanizedName(entry.getKey())));
        }
        return messages;
    }
}
