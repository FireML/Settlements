package me.crz.minecraft.kingdoms.land.gui;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.chat.ChatManager;
import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class SettingsGUI extends PlotGUI {
    private static SettingsGUI instance;
    public CInventory gui;

    public SettingsGUI(Player p) {
        super("plot.settings");
        OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
        gui = new CInventory(getText("title"), InventorySize.ONE) {};
        gui.addItem(new ClickableItem() {
            public ItemStack getItem(Player p) {
                return new CItem(Material.DIAMOND_PICKAXE).setName(getText("build")).build();
            }

            public boolean click(Player p, ClickType clickType) {
                OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                if (land != null) {
                    new BuildGui(p, land);
                    return true;
                }
                return false;
            }
        });
        gui.addItem(new ClickableItem() {
            public ItemStack getItem(Player p) {
                return new CItem(Material.LEVER).setName(getText("use")).build();
            }

            public boolean click(Player p, ClickType clickType) {
                OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                if (land != null) {
                    new UseGui(p, land);
                    return true;
                }
                return false;
            }
        });
        gui.addItem(new ClickableItem() {
            public ItemStack getItem(Player p) {
                return new CItem(Material.REDSTONE).setName(getText("mode")).build();
            }

            public boolean click(Player p, ClickType clickType) {
                ModeGUI.getInstance().open(p);
                return true;
            }
        });
        gui.addItem(new ClickableItem() {
            @Override
            public ItemStack getItem(Player p) {
                OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                if (land != null) {
                    return new CItem(Material.NAME_TAG).setName(getText("name.button", "name", land.getName())).build();
                }
                return null;
            }

            @Override
            public boolean click(Player p, ClickType ct) {
                OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                if (land != null) {
                    new AnvilGUI.Builder()
                            .item(new ItemStack(Material.WRITABLE_BOOK))
                            .title(getText("name.anvil.title", "name", land.getName()))
                            .text(getText("name.anvil.placeholder", "name", land.getName().isEmpty() ? " " : land.getName()))
                            .onClose(player -> Utils.runLater(() -> new SettingsGUI(player), 1))
                            .onComplete((player, text) -> {
                                OwnedLand currentLand = LandManager.getInstance().getLand(player.getLocation());
                                if (currentLand != null && (currentLand.isPlotOwner(player) || currentLand.canAdmin(ResidentManager.getInstance().getResident(player)))) {
                                    text = ChatManager.getInstance().filterAllCurses(ChatColor.stripColor(text.trim()));
                                    if (text.length() > 16) {
                                        getError("too-long").tell(player, "input", text, "amount", "16");
                                        return AnvilGUI.Response.text(text);
                                    }
                                    currentLand.setName(text);
                                    LandManager.getInstance().syncAndSave(currentLand);
                                    Message.get("command.plot.name.success").tell(p, "description", currentLand.getName(), "name", currentLand.getName());
                                }
                                return AnvilGUI.Response.close();
                            })
                            .plugin(KingdomsPlugin.getInstance())
                            .open(p);
                    return true;
                }
                return false;
            }
        });
        gui.addItem(new ClickableItem() {
            @Override
            public ItemStack getItem(Player p) {
                OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                if (land != null) {
                    return new CItem(Material.WRITABLE_BOOK).setName(getText("desc.button", "description", land.getDescription())).build();
                }
                return null;
            }

            @Override
            public boolean click(Player p, ClickType ct) {
                OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                if (land != null) {
                    new AnvilGUI.Builder()
                            .item(new ItemStack(Material.WRITABLE_BOOK))
                            .title(getText("desc.anvil.title", "description", land.getDescription()))
                            .text(getText("desc.anvil.placeholder", "description", land.getDescription().isEmpty() ? " " : land.getDescription()))
                            .onClose(player -> Utils.runLater(() -> new SettingsGUI(player), 1))
                            .onComplete((player, text) -> {
                                OwnedLand currentLand = LandManager.getInstance().getLand(player.getLocation());
                                if (currentLand != null && (currentLand.isPlotOwner(player) || currentLand.canAdmin(ResidentManager.getInstance().getResident(player)))) {
                                    text = ChatManager.getInstance().filterAllCurses(ChatColor.stripColor(text.trim()));
                                    if (text.length() > 255) {
                                        getError("too-long").tell(player, "input", text, "amount", "255");
                                        return AnvilGUI.Response.text(text);
                                    }
                                    currentLand.setDescription(text);
                                    LandManager.getInstance().syncAndSave(currentLand);
                                    Message.get("command.plot.desc.success").tell(p, "description", currentLand.getDescription());
                                }
                                return AnvilGUI.Response.close();
                            })
                            .plugin(KingdomsPlugin.getInstance())
                            .open(p);
                    return true;
                }
                return false;
            }
        });
        if (land != null && !land.isPlot() && land.canAdmin(ResidentManager.getInstance().getResident(p))) {
            gui.addItem(new ClickableItem() {
                @Override
                public ItemStack getItem(Player p) {
                    OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                    if (land != null) {
                        return new CItem(Material.EMERALD).setName(getText("sale.button" + (land.isForSale() ? "" : "-not-for-sale"), "price", String.valueOf(land.getPrice()))).build();
                    }
                    return null;
                }

                @Override
                public boolean click(Player p, ClickType ct) {
                    OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                    if (land != null) {
                        if (land.isForSale() && ct.isShiftClick()) {
                            land.setForSale(false);
                            land.setPrice(0);
                            land.setEmbassy(false);
                            gui.build(p);
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
                                        return AnvilGUI.Response.close();
                                    })
                                    .plugin(KingdomsPlugin.getInstance())
                                    .open(p);
                        }
                        return true;
                    }
                    return false;
                }
            });
        }
        gui.addItem(getPlotLinkItem(gui));
        gui.addItem(8, new ClickableItem() {
            public ItemStack getItem(Player p) {
                OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                if (land == null) {
                    return null;
                }

                return new CItem(Material.GREEN_BANNER).setName(
                        getMessage("entry").getReplacedText(land.getReplacements())
                ).build();
            }

            public boolean click(Player p, ClickType clickType) {
                return false;
            }
        });
        gui.build(p);
    }
}
