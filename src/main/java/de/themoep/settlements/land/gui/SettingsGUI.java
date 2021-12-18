package de.themoep.settlements.land.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.ChatMap;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class SettingsGUI extends PlotGUI {

    public SettingsGUI(Resident resident) {
        super(resident, LandManager.getInstance().getLand(resident.getPlayer().getLocation()), "settings");
        OwnedLand openedOnLand = LandManager.getInstance().getLand(landLocation);

        gui.addElement(new StaticGuiElement('a', getIcon("access"), click -> {
            OwnedLand land = LandManager.getInstance().getLand(landLocation);
            if (land != null) {
                new BuildGUI(
                        "plot",
                        land,
                        () -> {
                            OwnedLand currentLand = LandManager.getInstance().getLand(landLocation);
                            if (currentLand != null) {
                                return currentLand.isPlotOwner(resident) || currentLand.canAdmin(resident);
                            }
                            return false;
                        }
                ).open(resident.getPlayer());
            }
            return true;
        }, getText("build", openedOnLand.getReplacements())));

        gui.addElement(new StaticGuiElement('p', getIcon("perms"), click -> {
            OwnedLand land = LandManager.getInstance().getLand(landLocation);
            if (land != null) {
                Supplier<Boolean> permCheck = () -> {
                    OwnedLand currentLand = LandManager.getInstance().getLand(landLocation);
                    if (currentLand != null) {
                        return currentLand.isPlotOwner(resident) || currentLand.canAdmin(resident);
                    }
                    return false;
                };
                new PermsGUI(
                        "plot",
                        land,
                        permCheck,
                        (flagAble, c) -> new BuildGUI("plot", flagAble, permCheck).open(c.getWhoClicked())
                ).open(resident.getPlayer());
            }
            return true;
        }, getText("use")));

        gui.addElement(new StaticGuiElement('m', getIcon("mode"), click -> {
            OwnedLand land = LandManager.getInstance().getLand(landLocation);
            if (land != null) {
                new ModeGUI(resident, land).open();
            }
            return true;
        }, getText("mode")));

        gui.addElement(new StaticGuiElement('n', getIcon("name"), click -> {
            OwnedLand land = LandManager.getInstance().getLand(landLocation);
            if (land != null) {
                new AnvilGUI.Builder()
                        .item(ItemUtils.toIcon(Material.WRITABLE_BOOK))
                        .title(getText("name.anvil.title", "name", land.getName()))
                        .text(getText("name.anvil.placeholder", "name", land.getName().isEmpty() ? " " : land.getName().replace(ChatColor.COLOR_CHAR, '&')))
                        .onClose(player -> Utils.runLater(() -> new SettingsGUI(resident).open(), 1))
                        .onComplete((player, text) -> {
                            OwnedLand currentLand = LandManager.getInstance().getLand(player.getLocation());
                            if (currentLand != null && (currentLand.isPlotOwner(player) || currentLand.canAdmin(ResidentManager.getInstance().getResident(player)))) {
                                text = MessageUtils.replaceColorCodes(Utils.filterAllCurses(ChatColor.stripColor(text.trim())));
                                int maxLength = Math.min(Config.Plot.NAME_LENGTH, 255);
                                if (ChatColor.stripColor(text).length() > maxLength) {
                                    getError("too-long").tell(player, "input", text, "amount", String.valueOf(maxLength));
                                    return AnvilGUI.Response.text(text);
                                }
                                currentLand.setName(text);
                                LandManager.getInstance().syncAndSave(currentLand);
                                Message.get("command.plot.name.success").tell(resident, "description", currentLand.getName(), "name", currentLand.getName());
                            }
                            Utils.runLater(() -> new SettingsGUI(resident).open(), 1);
                            return AnvilGUI.Response.close();
                        })
                        .plugin(SettlementsPlugin.getInstance())
                        .open(resident.getPlayer());
            }
            return true;
        }, getText("name.button", "name", openedOnLand.getName())));

        gui.addElement(new StaticGuiElement('d', getIcon("description"), click -> {
            OwnedLand land = LandManager.getInstance().getLand(landLocation);
            if (land != null) {
                new AnvilGUI.Builder()
                        .item(ItemUtils.toIcon(Material.WRITABLE_BOOK))
                        .title(getText("desc.anvil.title", "description", land.getDescription()))
                        .text(getText("desc.anvil.placeholder", "description", land.getDescription().isEmpty() ? " " : land.getDescription().replace(ChatColor.COLOR_CHAR, '&')))
                        .onClose(player -> Utils.runLater(() -> new SettingsGUI(resident).open(), 1))
                        .onComplete((player, text) -> {
                            OwnedLand currentLand = LandManager.getInstance().getLand(player.getLocation());
                            if (currentLand != null && (currentLand.isPlotOwner(player) || currentLand.canAdmin(ResidentManager.getInstance().getResident(player)))) {
                                text = MessageUtils.replaceColorCodes(Utils.filterAllCurses(ChatColor.stripColor(text.trim())));
                                if (ChatColor.stripColor(text).length() > Config.Plot.DESCRIPTION_LENGTH) {
                                    getError("too-long").tell(player, "input", text, "amount", String.valueOf(Config.Plot.DESCRIPTION_LENGTH));
                                    return AnvilGUI.Response.text(text);
                                }
                                currentLand.setDescription(text);
                                LandManager.getInstance().syncAndSave(currentLand);
                                Message.get("command.plot.desc.success").tell(resident, "description", currentLand.getDescription());
                            }
                            Utils.runLater(() -> new SettingsGUI(resident).open(), 1);
                            return AnvilGUI.Response.close();
                        })
                        .plugin(SettlementsPlugin.getInstance())
                        .open(resident.getPlayer());
            }
            return true;
        }, getText("desc.button", "description", openedOnLand.getDescription())));

        gui.addElement(new StaticGuiElement('e', getIcon("members"), click -> {
            OwnedLand land = LandManager.getInstance().getLand(landLocation);
            if (land != null) {
                new MembersGUI(resident, land).open();
            }
            return true;
        }, getText("members", "amount", String.valueOf(openedOnLand.getPlotMembers().size()))));

        if (!openedOnLand.isPlot() && openedOnLand.canAdmin(resident)) {
            gui.addElement(new DynamicGuiElement('s', () -> new StaticGuiElement('s', getIcon("sale"), click -> {
                OwnedLand land = LandManager.getInstance().getLand(landLocation);
                if (land != null) {
                    if (land.isForSale() && click.getType().isShiftClick()) {
                        land.setForSale(false);
                        land.setPrice(0);
                        land.setEmbassy(false);
                        gui.draw();
                        LandManager.getInstance().saveLand(land);
                        Message.get("command.plot.unsale.success").tell(resident);
                    } else {
                        new AnvilGUI.Builder()
                                .item(ItemUtils.toIcon(Material.EMERALD))
                                .title(getText("sale.anvil.title", "price", String.valueOf(land.getPrice())))
                                .text(getText("sale.anvil.placeholder", "price", land.getPrice() > 0 ? String.valueOf(land.getPrice()) : " "))
                                .onClose(player -> Utils.runLater(() -> new SettingsGUI(resident).open(), 1))
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
                                    Utils.runLater(() -> new SettingsGUI(resident).open(), 1);
                                    return AnvilGUI.Response.close();
                                })
                                .plugin(SettlementsPlugin.getInstance())
                                .open(resident.getPlayer());
                    }
                }
                return true;
            }, getText("sale.button" + (openedOnLand.isForSale() ? "" : "-not-for-sale"), "price", String.valueOf(openedOnLand.getPrice())))));
        }

        gui.addElement(new DynamicGuiElement('l', () -> {
            OwnedLand land = LandManager.getInstance().getLand(landLocation);
            if (land == null) {
                return null;
            }
            List<String> description = new ArrayList<>();
            ItemStack item = getIcon("link");
            if (land.hasLink()) {
                description.add(getMessage("linked").getReplacedText("amount", String.valueOf(land.getLinked().size() - 1)));
                ItemUtils.setGlowing(item, true);
            }
            if (land.isPlotOwner(resident) || (land.canAdmin(resident) && (land.isPlot() || land.hasLink()))) {
                if (description.isEmpty()) {
                    description.add(getText("link"));
                } else {
                    description.add(getText("unlink"));
                }
            }
            if (description.isEmpty()) {
                return null;
            }
            return new StaticGuiElement('l', item, click -> {
                OwnedLand currentLand = LandManager.getInstance().getLand(landLocation);
                if (currentLand != null) {
                    boolean canAdmin = currentLand.canAdmin(resident);
                    if (currentLand.isPlotOwner(resident) || canAdmin) {
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
                            Message.get("command.plot.unlink.disconnected").tell(resident, "amount", String.valueOf(linked));
                        } else if (currentLand.isPlot()) {
                            gui.close();
                            Utils.runAsync(() -> new ChatMap(resident.getPlayer(), currentLand.getMapKey()));
                        }
                    }
                }
                return true;
            }, description.toArray(new String[0]));
        }));

        gui.addElement(new DynamicGuiElement('i', () -> {
            OwnedLand land = LandManager.getInstance().getLand(landLocation);
            if (land == null) {
                return null;
            }
            return new StaticGuiElement('i', getIcon("info"), Utils.wrap(getText("entry", land.getReplacements()), 48));
        }));

        gui.addElement(new GuiBackElement('b', getIcon("back"), true, getText("back")));

    }
}
