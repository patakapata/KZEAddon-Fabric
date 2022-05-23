package me.patakapata.kzeaddon;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class KzeUtils {
    public static final String BUKKIT_PERSISTENT_DATA = "PublicBukkitValues";
    public static final String WEAPON_ATTACH_ID = "puddingshooter:attach_id";
    public static final String CUSTOM_ITEM = "refresh:custom_item";

    public static boolean isWeapon(ItemStack item) {
        NbtCompound publicBukkitValues = item.getSubNbt(BUKKIT_PERSISTENT_DATA);
        return publicBukkitValues != null && !publicBukkitValues.getString(WEAPON_ATTACH_ID).equals("") ;
    }

    public static boolean isCustomItem(ItemStack item) {
        NbtCompound publicBukkitValues = item.getSubNbt(BUKKIT_PERSISTENT_DATA);
        return publicBukkitValues != null && !publicBukkitValues.getString(CUSTOM_ITEM).equals("");
    }

}
