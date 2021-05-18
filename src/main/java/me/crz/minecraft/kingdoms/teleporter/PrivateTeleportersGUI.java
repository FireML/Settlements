package me.crz.minecraft.kingdoms.teleporter;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.themoep.inventorygui.GuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.GuiPageElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.command.CommandManager;
import me.crz.minecraft.kingdoms.command.empire.EmpireHomeCommand;
import me.crz.minecraft.kingdoms.command.village.VillageHomeCommand;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.land.LandAccess;
import me.crz.minecraft.kingdoms.land.LandOwner;
import me.crz.minecraft.kingdoms.utils.IdObject;
import me.crz.minecraft.kingdoms.utils.ItemUtils;
import me.crz.minecraft.kingdoms.utils.PathedConfigMessages;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.resident.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.world.NamedLocation;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class PrivateTeleportersGUI extends KingdomsGUI {
    private final static String[] SETUP = {
            "ggggggggg",
            "ggggggggg",
            "ggggggggg",
            "ggggggggg",
            "         ",
            "p   b   n"
    };
    private final Cache<IdObject, List<Supplier<GuiElement>>> suppliers = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES).build();
    private final InventoryGui gui;

    public PrivateTeleportersGUI(Player player) {
        super("private-teleports");
        Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        gui = new InventoryGui(KingdomsPlugin.getInstance(), getText("title"), SETUP);
        gui.setCloseAction(close -> false);
        gui.setFiller(BORDER_ITEM);
        GuiElementGroup group = new GuiElementGroup('g');
        try {
            for (Supplier<GuiElement> supplier : getSuppliers(village)) {
                group.addElement(supplier.get());
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        gui.addElement(group);

        gui.addElement(new GuiPageElement(
                'p',
                ItemUtils.toIcon(Material.OAK_SIGN),
                GuiPageElement.PageAction.PREVIOUS,
                getText("page.previous", "page", "%prevpage%")
        ));
        gui.addElement(new GuiPageElement(
                'n',
                ItemUtils.toIcon(Material.OAK_SIGN),
                GuiPageElement.PageAction.NEXT,
                getText("page.previous", "page", "%nextpage%")
        ));

        gui.addElement(new StaticGuiElement('b', ItemUtils.toIcon(Material.BARRIER), click -> {
            new TeleportersGUI().open((Player) click.getEvent().getWhoClicked());
            return true;
        }, getText("back")));

        gui.show(player);
    }

    private List<Supplier<GuiElement>> getSuppliers(Village village) throws ExecutionException {
        return suppliers.get(village != null ? village.getId() : IdObject.get(null), () -> {
            List<Supplier<GuiElement>> suppliers = new ArrayList<>();

            Set<LandOwner> entries = new LinkedHashSet<>();
            if (village != null) {
                for (Map.Entry<String, NamedLocation> outpost : village.getOutpostHomes().entrySet()) {
                    String text = getText("outpost",
                            "name", outpost.getValue().getName(),
                            "state", village.getStateTag(),
                            "owner", village.getName()
                    );
                    suppliers.add(() -> new StaticGuiElement(
                            'g',
                            ItemUtils.toIcon(Material.RED_BANNER),
                            click -> {
                                click.getGui().close();
                                if (village.getOutpostHomes().containsKey(outpost.getKey())) {
                                    Utils.teleport(
                                            new PathedConfigMessages("command.village.outpost.teleport"),
                                            (Player) click.getEvent().getWhoClicked(),
                                            village.getOutpostHomes().get(outpost.getKey()).toBukkitLocation(),
                                            "outpost",
                                            "name", outpost.getValue().getName()
                                    );
                                }
                                return true;
                            },
                            text
                    ));
                }

                village.getAllies().stream()
                        .filter(v -> v.getAccess().atLeast(LandAccess.ALLY))
                        .forEachOrdered(entries::add);

                if (village.hasEmpire()) {
                    village.getEmpire().getMembers().stream()
                            .filter(v -> !v.equals(village) && v.getAccess().atLeast(LandAccess.TRUSTED))
                            .sorted(Village::compareTo)
                            .forEachOrdered(entries::add);
                }
            }

            EmpireManager.getInstance().getEmpires().stream()
                    .filter(e -> (village == null || !e.equals(village.getEmpire())) && e.getAccess().atLeast(LandAccess.PUBLIC))
                    .sorted(Empire::compareTo)
                    .forEachOrdered(entries::add);

            VillageManager.getInstance().getVillages().stream()
                    .filter(v -> !v.equals(village) && v.getAccess().atLeast(LandAccess.PUBLIC))
                    .sorted(Village::compareTo)
                    .forEachOrdered(entries::add);

            for (LandOwner landOwner : entries) {
                Resident chief = ResidentManager.getInstance().getResident(landOwner.getChiefId());
                ItemStack icon = ItemUtils.getHead(chief.getUUID(), chief.getName());
                String text = getText("entry", "name", landOwner.getName(), "state", landOwner.getStateTag());
                suppliers.add(() -> new StaticGuiElement(
                        'g',
                        icon,
                        click -> {
                            if (landOwner.hasHome()) {
                                click.getGui().close();
                                if (landOwner instanceof Empire) {
                                    ((EmpireHomeCommand) CommandManager.getInstance().getCommand("empire").getSubCommands().get("home")).run(
                                            ResidentManager.getInstance().getResident((Player) click.getEvent().getWhoClicked()),
                                            "empire",
                                            "home",
                                            landOwner.getName()
                                    );
                                } else if (landOwner instanceof Village) {
                                    ((VillageHomeCommand) CommandManager.getInstance().getCommand("village").getSubCommands().get("home")).run(
                                            ResidentManager.getInstance().getResident((Player) click.getEvent().getWhoClicked()),
                                            "village",
                                            "home",
                                            landOwner.getName()
                                    );
                                }
                            }
                            return true;
                        },
                        text
                ));
            }

            return suppliers;
        });
    }
}
