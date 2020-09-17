package me.crz.minecraft.kingdoms.land.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.chat.ChatManager;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.ChatMap;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SettingsGUI extends PlotGUI {
    private final static String[] SETUP = {
            "ggggigggg",
            "         ",
            " um s nd ",
            " bl      ",
            "         ",
            "ggggcgggg"
    };

    public InventoryGui gui;

    public SettingsGUI(Player p) {
        super("settings");
        OwnedLand openedOnLand = LandManager.getInstance().getLand(p.getLocation());
        gui = new InventoryGui(KingdomsPlugin.getInstance(), getText("title"), SETUP);
        gui.setCloseAction(close -> false);

        gui.addElement(new StaticGuiElement('g', BORDER_ITEM, " "));

        gui.addElement(new StaticGuiElement('b', new CItem(Material.DIAMOND_PICKAXE).build(), click -> {
            OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
            if (land != null) {
                new BuildGui(land).open(p);
            }
            return true;
        }, getText("build")));

        gui.addElement(new StaticGuiElement('u', new ItemStack(Material.LEVER), click -> {
            OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
            if (land != null) {
                new UseGui(land).open(p);
            }
            return true;
        }, getText("use")));

        gui.addElement(new StaticGuiElement('m', new ItemStack(Material.REDSTONE), click -> {
            OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
            if (land != null) {
                new ModeGUI(land).open(p);
            }
            return true;
        }, getText("mode")));

        gui.addElement(new StaticGuiElement('n', new ItemStack(Material.NAME_TAG), click -> {
            OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
            if (land != null) {
                new AnvilGUI.Builder()
                        .item(new ItemStack(Material.WRITABLE_BOOK))
                        .title(getText("name.anvil.title", "name", land.getName()))
                        .text(getText("name.anvil.placeholder", "name", land.getName().isEmpty() ? " " : land.getName().replace(ChatColor.COLOR_CHAR, '&')))
                        .onClose(player -> Utils.runLater(() -> new SettingsGUI(player), 1))
                        .onComplete((player, text) -> {
                            OwnedLand currentLand = LandManager.getInstance().getLand(player.getLocation());
                            if (currentLand != null && (currentLand.isPlotOwner(player) || currentLand.canAdmin(ResidentManager.getInstance().getResident(player)))) {
                                text = MessageUtils.replaceColorCodes(ChatManager.getInstance().filterAllCurses(ChatColor.stripColor(text.trim())));
                                if (ChatColor.stripColor(text).length() > Utils.getPlotNameLength()) {
                                    getError("too-long").tell(player, "input", text, "amount", String.valueOf(Utils.getPlotNameLength()));
                                    return AnvilGUI.Response.text(text);
                                }
                                currentLand.setName(text);
                                LandManager.getInstance().syncAndSave(currentLand);
                                Message.get("command.plot.name.success").tell(p, "description", currentLand.getName(), "name", currentLand.getName());
                            }
                            Utils.runLater(() -> new SettingsGUI(player), 1);
                            return AnvilGUI.Response.close();
                        })
                        .plugin(KingdomsPlugin.getInstance())
                        .open(p);
            }
            return true;
        }, getText("name.button", "name", openedOnLand != null ? openedOnLand.getName() : "none")));

        gui.addElement(new StaticGuiElement('d', new ItemStack(Material.WRITABLE_BOOK), click -> {
            OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
            if (land != null) {
                new AnvilGUI.Builder()
                        .item(new ItemStack(Material.WRITABLE_BOOK))
                        .title(getText("desc.anvil.title", "description", land.getDescription()))
                        .text(getText("desc.anvil.placeholder", "description", land.getDescription().isEmpty() ? " " : land.getDescription().replace(ChatColor.COLOR_CHAR, '&')))
                        .onClose(player -> Utils.runLater(() -> new SettingsGUI(player), 1))
                        .onComplete((player, text) -> {
                            OwnedLand currentLand = LandManager.getInstance().getLand(player.getLocation());
                            if (currentLand != null && (currentLand.isPlotOwner(player) || currentLand.canAdmin(ResidentManager.getInstance().getResident(player)))) {
                                text = MessageUtils.replaceColorCodes(ChatManager.getInstance().filterAllCurses(ChatColor.stripColor(text.trim())));
                                if (ChatColor.stripColor(text).length() > Utils.getPlotDescriptionLength()) {
                                    getError("too-long").tell(player, "input", text, "amount", String.valueOf(Utils.getPlotDescriptionLength()));
                                    return AnvilGUI.Response.text(text);
                                }
                                currentLand.setDescription(text);
                                LandManager.getInstance().syncAndSave(currentLand);
                                Message.get("command.plot.desc.success").tell(p, "description", currentLand.getDescription());
                            }
                            Utils.runLater(() -> new SettingsGUI(player), 1);
                            return AnvilGUI.Response.close();
                        })
                        .plugin(KingdomsPlugin.getInstance())
                        .open(p);
            }
            return true;
        }, getText("desc.button", "description", openedOnLand != null ? openedOnLand.getDescription() : "none")));

        if (openedOnLand != null && !openedOnLand.isPlot() && openedOnLand.canAdmin(ResidentManager.getInstance().getResident(p))) {
            gui.addElement(new DynamicGuiElement('s', () -> new StaticGuiElement('s', new ItemStack(Material.EMERALD), click -> {
                OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                if (land != null) {
                    if (land.isForSale() && click.getType().isShiftClick()) {
                        land.setForSale(false);
                        land.setPrice(0);
                        land.setEmbassy(false);
                        gui.draw();
                        LandManager.getInstance().saveLand(land);
                        Message.get("command.plot.unsale.success").tell(p);
                    } else {
                        new AnvilGUI.Builder()
                                .item(new ItemStack(Material.EMERALD))
                                .title(getText("sale.anvil.title", "price", String.valueOf(land.getPrice())))
                                .text(getText("sale.anvil.placeholder", "price", land.getPrice() > 0 ? String.valueOf(land.getPrice()) : " "))
                                .onClose(player -> Utils.runLater(() -> new SettingsGUI(player), 1))
                                .onComplete((player, text) -> {
                                    OwnedLand currentLand = LandManager.getInstance().getLand(player.getLocation());
                                    if (currentLand != null && currentLand.canAdmin(ResidentManager.getInstance().getResident(player))) {
                                        if (currentLand.isPlot()) {
                                            Message.get("command.plot.sale.already-owned").tell(player);
                                            return AnvilGUI.Response.close();
                                        }
                                        try {
                                            int price = Integer.parseInt(text.trim());
                                            if (price < 0) {
                                                getError("not-a-number").tell(player, "input", text);
                                                return AnvilGUI.Response.text(text);
                                            }
                                            currentLand.setPrice(price);
                                            currentLand.setForSale(true);
                                            LandManager.getInstance().saveLand(currentLand);
                                            Message.get("command.plot.sale.success").tell(player, "price", String.valueOf(price));
                                        } catch (NumberFormatException e) {
                                            getError("not-a-number").tell(player, "input", text);
                                            return AnvilGUI.Response.text(text);
                                        }
                                    }
                                    Utils.runLater(() -> new SettingsGUI(player), 1);
                                    return AnvilGUI.Response.close();
                                })
                                .plugin(KingdomsPlugin.getInstance())
                                .open(p);
                    }
                }
                return true;
            }, getText("sale.button" + (openedOnLand.isForSale() ? "" : "-not-for-sale"), "price", String.valueOf(openedOnLand.getPrice())))));
        }

        gui.addElement(new DynamicGuiElement('l', () -> {
            OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
            if (land == null) {
                return null;
            }
            List<String> description = new ArrayList<>();
            CItem item = new CItem(Material.MAP);
            if (land.hasLink()) {
                description.add(getMessage("linked").getReplacedText("amount", String.valueOf(land.getLinked().size() - 1)));
                item.glow(true);
            }
            if (land.isPlotOwner(p) || (land.canAdmin(ResidentManager.getInstance().getResident(p)) && (land.isPlot() || land.hasLink()))) {
                if (description.isEmpty()) {
                    description.add(getText("link"));
                } else {
                    description.add(getText("unlink"));
                }
            }
            if (description.isEmpty()) {
                return null;
            }
            return new StaticGuiElement('l', item.build(), click -> {
                OwnedLand currentLand = LandManager.getInstance().getLand(p.getLocation());
                if (currentLand != null) {
                    Resident resident = ResidentManager.getInstance().getResident(p);
                    boolean canAdmin = currentLand.canAdmin(resident);
                    if (currentLand.isPlotOwner(p) || canAdmin) {
                        if (currentLand.hasLink()) {
                            Collection<OwnedLand> linkedLands = currentLand.getLinked();
                            int linked = linkedLands.size() - 1;
                            Set<OwnedLand> toSave = new HashSet<>();
                            if (linked == 1) {
                                OwnedLand otherLand = linkedLands.stream().filter(o -> !o.equals(currentLand)).findAny().get();
                                otherLand.removeLink();
                                toSave.add(otherLand);
                            }
                            if (currentLand.removeLink()) {
                                toSave.add(currentLand);
                                Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(toSave));
                                gui.draw();
                            }
                            Message.get("command.plot.unlink.disconnected").tell(p, "amount", String.valueOf(linked));
                        } else if (currentLand.isPlot()) {
                            gui.close();
                            Utils.runAsync(() -> new ChatMap(p, currentLand.getMapKey()));
                        }
                    }
                }
                return true;
            }, description.toArray(new String[0]));
        }));

        gui.addElement(new DynamicGuiElement('i', () -> {
            OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
            if (land == null) {
                return null;
            }
            return new StaticGuiElement('i', new ItemStack(Material.GREEN_BANNER), getMessage("entry").getReplacedText(land.getReplacements()));
        }));

        gui.addElement(new StaticGuiElement('c', new ItemStack(Material.BARRIER), click -> {
            click.getGui().close();
            return true;
        }, getText("close")));

        // One last check if we can really open this GUI
        OwnedLand currentLand = LandManager.getInstance().getLand(p.getLocation());
        if (currentLand != null && (currentLand.isPlotOwner(p) || currentLand.canAdmin(ResidentManager.getInstance().getResident(p)))) {
            gui.show(p);
        } else {
            getError("not-your-plot").tell(p);
        }
    }
}
