package de.themoep.settlements.land.gui;

import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.db.DatabaseSerializable;
import de.themoep.settlements.leveling.Levelable;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.HumanReadable;
import de.themoep.settlements.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import uk.firedev.poleislib.libs.themoep.inventorygui.GuiStorageElement;
import uk.firedev.poleislib.libs.themoep.inventorygui.InventoryGui;
import uk.firedev.poleislib.utils.ComponentUtils;

import java.util.HashMap;
import java.util.Map;

public class DepositGUI<L extends Levelable<?> & DatabaseSerializable> extends SettlementsGUI {

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
    private final L levelable;

    public DepositGUI(String namespace, Resident player, L levelable) {
        super(namespace + ".deposit");
        this.resident = player;
        this.levelable = levelable;

        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title"), SETUP);

        inv = Bukkit.createInventory(null, SIZE);

        gui.addElement(new GuiStorageElement('d', inv));

        gui.setCloseAction(close -> {
            ItemStack[] contents = inv.getContents();
            if (contents == null || contents.length == 0 || close.getPlayer() != resident.getPlayer()) {
                return true;
            }

            deposit(contents, resident);
            inv.clear();
            levelable.checkLevelUp(resident);
            return true;
        });
    }

    public void open() {
        gui.show(resident.getPlayer());
    }

    private void deposit(ItemStack[] contents, Resident resident) {
        Map<Material, Long> fail = new HashMap<>();
        Player player = resident.getPlayer();
        if (!levelable.isMaxLevel()) {
            Map<String, Long> nextLevelReqs = levelable.getNextLevelRequirements();
            Map<String, Long> total = new HashMap<>();
            Map<String, Long> deposited = new HashMap<>();

            for (String type : nextLevelReqs.keySet()) {
                total.put(type, levelable.getAmount(type));
            }

            for (ItemStack item : contents) {
                if (item != null) {
                    String material = item.getType().name();
                    if (nextLevelReqs.containsKey(material)) {
                        long required = nextLevelReqs.get(material);
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
                for (Resident onlineResident : levelable.getOnlineResidents()) {
                    onlineResident.tell(buildSuccessDepositMessage(resident, player, deposited));
                }
                levelable.add(deposited);
                levelable.save();
                levelable.logDeposit(resident, deposited);
            }
            if (!fail.isEmpty()) {
                player.sendMessage(ComponentUtils.parseComponent(buildFailDepositMessage(resident, fail, "")));
            }
        } else if (contents.length > 0) {
            for (ItemStack item2 : contents) {
                if (item2 != null) {
                    Utils.addAmount(fail, item2.getType(), (long) item2.getAmount());
                    addToInventory(player, item2);
                }
            }

            player.sendMessage(ComponentUtils.parseComponent(buildFailDepositMessage(resident, fail, getMessage("max-level-reached").getText())));
        }
    }

    private void addToInventory(Player player, ItemStack itemStack) {
        for (ItemStack rest : player.getInventory().addItem(itemStack).values()) {
            Item item = player.getWorld().dropItem(player.getLocation(), rest);
            SettlementsPlugin.getInstance().getServerAdapter().markOwned(item, player);
        }
    }

    private String buildFailDepositMessage(Resident resident, Map<Material, Long> fail, String reason) {
        StringBuilder message = new StringBuilder();
        message.append(getMessage("fail-deposit").getReplacedText("reason", reason));
        for (Map.Entry<Material, Long> entry : fail.entrySet()) {
            message.append(getMessage("list-entry").getReplacedText(
                    "state", levelable.getTag(),
                    "amount", String.valueOf(entry.getValue()),
                    "icon", "",
                    "material", HumanReadable.humanizedName(entry.getKey().name())
            ));
        }
        return message.toString();
    }

    private String buildSuccessDepositMessage(Resident resident, Player p, Map<String, Long> deposited) {
        StringBuilder message = new StringBuilder();
        message.append(getMessage("success-deposit").getReplacedText(
                "player", ComponentUtils.toString(p.displayName()),
                "state", levelable.getTag()
        ));
        for (Map.Entry<String, Long> entry : deposited.entrySet()) {
            message.append(getMessage("list-entry").getReplacedText(
                    "amount", String.valueOf(entry.getValue()),
                    "icon", "",
                    "material", HumanReadable.humanizedName(entry.getKey())
            ));
        }
        return message.toString();
    }
}
