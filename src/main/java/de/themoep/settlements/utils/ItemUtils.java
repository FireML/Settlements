package de.themoep.settlements.utils;

import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;

import java.util.UUID;

public class ItemUtils {

    public static ItemStack getHead(String textures) {
        ItemStack skull = ItemType.PLAYER_HEAD.createItemStack();
        skull.setData(
            DataComponentTypes.PROFILE,
            ResolvableProfile.resolvableProfile()
                .uuid(UUID.randomUUID())
                .name("SettlementsSkull")
                .addProperty(new ProfileProperty("textures", textures))
                .build()
        );
        return skull;
    }

    public static ItemStack getHead(UUID uuid, String string) {
        ItemStack skull = ItemType.PLAYER_HEAD.createItemStack();
        skull.setData(
            DataComponentTypes.PROFILE,
            ResolvableProfile.resolvableProfile()
                .uuid(uuid)
                .name(string)
                .build()
        );
        skull.setData(
            DataComponentTypes.CUSTOM_NAME,
            ComponentUtils.parseComponent(string)
        );
        return skull;
    }

    public static void hideAllFlags(ItemStack item) {
        item.editMeta(meta -> meta.addItemFlags(ItemFlag.values()));
    }

    public static ItemStack setGlowing(ItemStack item, boolean value) {
        item.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, value);
        return item;
    }

}
