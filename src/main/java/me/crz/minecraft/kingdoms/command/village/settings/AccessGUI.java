package me.crz.minecraft.kingdoms.command.village.settings;

/*
 * Kingdoms - kingdoms
 * Copyright (c) 2018 Max Lee aka Phoenix616 (mail@moep.tv)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CInventoryManager;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageAccess;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class AccessGUI {
    private static AccessGUI instance;
    public CInventory gui;

    public static String cfc(final String string) {
        if (string == null || string.length() == 0) {
            return string;
        }
        return String.valueOf(string.substring(0, 1).toUpperCase()) + string.substring(1, string.length());
    }

    public String yesOrNo(final boolean b) {
        return b ? "Yes" : "No";
    }

    public AccessGUI() {
        super();
        gui = new CInventory("&3&lSettlement Access", InventorySize.ONE) {};

        gui.addItem(8, new ClickableItem() {
            public ItemStack getItem(final Player p) {
                return new CItem(Material.BARRIER).setName("&c&lBack To Settings").build();
            }

            public boolean click(final Player p, final ClickType clickType) {
                SettingsGUI.getInstance().open(p);
                return true;
            }
        });
        for (VillageAccess access : VillageAccess.values()) {
            gui.addItem(new ClickableItem() {
                @Override
                public ItemStack getItem(Player p) {
                    Village village = VillageManager.getInstance().getVillageFromPlayer(p);
                    if (village != null && village.getAccess() == access) {
                        return new CItem(Material.LIME_WOOL).setName("&e&l" + access.humanizedName()).build();
                    }
                    return new CItem(Material.LIGHT_GRAY_WOOL).setName("&7" + access.humanizedName()).build();
                }

                @Override
                public boolean click(Player p, ClickType clickType) {
                    Resident resident = ResidentManager.getInstance().getResident(p);
                    if (resident.getVillage() != null && resident.getVillageRank().atLeast(VillageRank.CHIEF)) {
                        if (resident.getVillage().getAccess() != access) {
                            resident.getVillage().setAccess(access);
                            MessageUtils.tell(p, "&7Access changed to &6" + access.humanizedName());
                            Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(resident.getVillage()));
                            gui.build(p);
                            return true;
                        }
                    }
                    return false;
                }
            });
        }
        CInventoryManager.getInstance().addCInvetory(gui);
    }

    public void open(final Player p) {
        gui.build(p);
    }

    public static AccessGUI getInstance() {
        if (instance == null) {
            instance = new AccessGUI();
        }
        return instance;
    }
}
