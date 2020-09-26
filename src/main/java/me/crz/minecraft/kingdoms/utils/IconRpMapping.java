package me.crz.minecraft.kingdoms.utils;

/*
 * IconRpMapping utils for mapping of Bukkit materials to WolfieMario's
 * Custom Text Icons Resourcepack (https://s.moep.tv/iconrp)
 * Copyright (C) 2018 Max Lee aka Phoenix616 (mail@moep.tv)
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

import me.crz.minecraft.kingdoms.utils.config.ConfigAccessor;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Phoenix616 on 07.03.2015.
 */
public class IconRpMapping {

    private Map<String, Integer> encodingNames = new HashMap<String, Integer>();

    private final String[] potionids = {
            "unused", 
            "regen",
            "speed",
            "fire_resistance", 
            "poison", 
            "instant_heal",
            "night_vision", 
            "unused", 
            "weakness", 
            "strength", 
            "slowness", 
            "jump",
            "instant_damage",
            "water_breathing", 
            "invisibility" 
    };
    
    private int offset;

    public IconRpMapping(JavaPlugin plugin) {
        plugin.getLogger().info("Loading Text Icon Resourcepack mapping...");
        String fileName = "iconrpmapping.yml";

        ConfigAccessor iconconfig = new ConfigAccessor(plugin, fileName);
        
        offset = iconconfig.getConfig().getInt("offset");
        
        List<String> matlist = iconconfig.getConfig().getStringList("map");
        for(int i = 0; i < matlist.size(); i++) {
            String name = matlist.get(i).toLowerCase();
            /*if(Material.getMaterial(s.toUpperCase()) == null && !s.equalsIgnoreCase("unused"))
                plugin.getLogger().warning("[IconRpMapping] " + s + " is not a valid Bukkit material name!");*/
            encodingNames.put(name, i);
        }

        plugin.getLogger().info("Text Icon Resourcepack mapping loaded.");
    }

    /**
     * Get the unicode character that corresponds with the inputted item
     * @param item The item to get the character of
     * @param escape Make the icon use the white chat color?
     * @return A string of the unicode character which represents the item in the icon rp. Empty string if none was found.
     */
    public String getIcon(ItemStack item, boolean escape) {
        ItemMeta meta = item.getItemMeta();
        String iconstr = null;
        if (meta instanceof LeatherArmorMeta) {
            iconstr = ColorUtils.getNearestChatColor(((LeatherArmorMeta) meta).getColor()) + getIcon("white_" + item.getType().toString(), false) + ChatColor.RESET;
        } else if (meta instanceof PotionMeta) {
            PotionMeta pm = (PotionMeta) meta;
            try {
                iconstr = getIcon(item.getType().toString() + "_" + pm.getBasePotionData().getType().toString(), escape);
            } catch (NoSuchMethodError e) {
                if (pm.hasCustomEffects()) {
                    iconstr = getIcon(item.getType().toString() + "_" + pm.getCustomEffects().get(0).getType().toString(), escape);
                }
            }
            if (iconstr == null) {
                iconstr = getIcon(item.getType().toString() + "_water", escape);
            }
        } else if (item.getType() == Material.POTION) {
            String key = "water_potion";
            if (item.getDurability() == 0) {
                iconstr = getIcon(key, escape);
            } else {
                String splash = "";

                boolean[] bits = BitUtils.getBits(item.getDurability());
                int id = BitUtils.getRange(item.getDurability(), 0, 3);
                if (id < potionids.length)
                    key = potionids[id].toLowerCase();

                if (bits[1])
                    splash = "splash_";
                iconstr = getIcon(key + "_" + splash + "potion", true);
                if (iconstr == null)
                    iconstr = getIcon(splash + key + "_potion", true);
            }
        } else if (meta instanceof FireworkEffectMeta) {
            FireworkEffect fe = ((FireworkEffectMeta) meta).getEffect();
            if (fe != null && !fe.getColors().isEmpty()) {
                Color median = fe.getColors().get(0).mixColors((Color[]) fe.getColors().stream().skip(1).toArray());
                iconstr = getIcon(item.getType().toString() + "_" + ColorUtils.getNearestChatColor(median).name(), escape);
            }
        } else if (item.getType() == Material.SHIELD && meta instanceof BannerMeta) {
            BannerMeta bm = (BannerMeta) meta;
            iconstr = getIcon(item.getType().toString() + "_" + bm.getBaseColor(), escape);
            if (iconstr == null) {
                iconstr = getIcon(item.getType() + "_" + DyeColor.getByDyeData(item.getData().getData()), escape);
            }
        }
        boolean isUnbreakable = false;
        if (iconstr == null) {
            isUnbreakable = item.getItemMeta().isUnbreakable();
            if (isUnbreakable) {
                iconstr = getIcon(item.getType().toString() + ":unbreakable:" + item.getDurability(), escape);
            }
        }
        if (iconstr == null) {
            iconstr = getIcon(item.getType().toString() + ":" + item.getDurability(), escape);
        }
        if (iconstr == null && isUnbreakable) {
            iconstr = getIcon(item.getType().toString() + ":unbreakable", escape);
        }
        if (iconstr == null) {
            iconstr = getIcon(item.getType().toString(), escape);
        }
        return iconstr != null ? iconstr : "";
    }

    /**
     * Get the unicode character of the inputed icon
     * @param iconname The name of the icon
     * @param escape Make the icon use the white chat color?
     * @return A string of the unicode character which represents the icon in the icon rp. <tt>null</tt> if none was found.
     */
    public String getIcon(String iconname, boolean escape) {
        if(encodingNames.containsKey(iconname.toLowerCase())) {
            String symbol = Character.toString((char) (offset + encodingNames.get(iconname.toLowerCase())));
            if(escape)
                symbol = ChatColor.WHITE + symbol + ChatColor.RESET;
            return symbol;
        } else
            return null;
    }
}
