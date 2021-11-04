package com.theboss.kzeaddonfabric.ingame;

import com.theboss.kzeaddonfabric.VanillaUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.profiler.Profiler;

import java.util.List;

public class Weapon {
    private static final TextColor RELOADING_COLOR = TextColor.parse("red");

    private String name;
    private int maxMagazineAmmo;
    private int reloadTime;
    private int inMagazineAmmo;
    private int totalAmmo;
    private boolean isReloading;

    public static boolean quickReloadCheck(ItemStack item) {
        List<Text> siblings = item.getName().getSiblings();
        if (siblings.isEmpty()) return false;
        TextColor color = item.getName().getSiblings().get(0).getStyle().getColor();
        return item.getItem().equals(Items.DIAMOND_HOE) && VanillaUtils.getCustomModelData(item) != -1 && (color != null && color.equals(RELOADING_COLOR));
    }

    private static Text[] getLore(ItemStack item) {
        NbtCompound display = item.getSubTag("display");
        if (display == null) return null;
        NbtList loreTag = display.getList("Lore", 8);

        Text[] lore = new Text[loreTag.size()];
        for (int i = 0; i < loreTag.size(); i++) {
            lore[i] = Text.Serializer.fromJson(loreTag.getString(i));
        }

        return lore;
    }

    public static byte styleToFlags(Style style) {
        int result = 0;

        if (style.isBold()) result = result | 0b1;
        if (style.isItalic()) result = result | 0b10;
        if (style.isUnderlined()) result = result | 0b100;
        if (style.isStrikethrough()) result = result | 0b1000;
        if (style.isObfuscated()) result = result | 0b10000;

        return (byte) result;
    }

    public Weapon() {
        this("", -1, -1, -1, -1, false);
    }

    public Weapon(String name, int inMagazineAmmo, int maxMagazineAmmo, int reloadTime, int totalAmmo, boolean isReloading) {
        this.name = name;
        this.maxMagazineAmmo = maxMagazineAmmo;
        this.reloadTime = reloadTime;

        this.inMagazineAmmo = inMagazineAmmo;
        this.totalAmmo = totalAmmo;
        this.isReloading = isReloading;
    }

    public int getInMagazineAmmo() {
        return this.inMagazineAmmo;
    }

    public int getMaxMagazineAmmo() {
        return this.maxMagazineAmmo;
    }

    public String getName() {
        return this.name;
    }

    public int getReloadTime() {
        return this.reloadTime;
    }

    public int getTotalAmmo() {
        return this.totalAmmo;
    }

    public double inMagazineAmmoPercentage() {
        return (double) this.inMagazineAmmo / this.maxMagazineAmmo;
    }

    public void init() {
        this.name = "";
        this.inMagazineAmmo = -1;
        this.maxMagazineAmmo = -1;
        this.reloadTime = -1;
        this.totalAmmo = -1;
        this.isReloading = false;
    }

    public boolean isReloading() {
        return this.isReloading;
    }

    public void setReloading(boolean reloading) {
        this.isReloading = reloading;
    }

    public boolean isValid() {
        return !this.name.equals("") && this.inMagazineAmmo != -1 && this.maxMagazineAmmo != -1 && this.reloadTime != -1 && this.totalAmmo != -1;
    }

    public void newParser(ItemStack item) {
        Profiler profiler = VanillaUtils.getProfiler();
        profiler.push("Declare variables");
        Text name = item.getName();
        List<Text> nameSiblings = name.getSiblings();


        String gunName;
        boolean isReloading;
        int inMagAmmo;
        int totalAmmo;
        int reloadDuration = -1;
        int reloadAmount = -1;

        profiler.swap("Get lore");
        Text[] lore = getLore(item);

        try {
            profiler.swap("Parse a name");
            gunName = nameSiblings.get(0).asString().replace(" ", "");
            TextColor gunNameColor = nameSiblings.get(0).getStyle().getColor();
            isReloading = gunNameColor != null && gunNameColor.getName().equals("red");
            profiler.swap("Parse a ammo");
            inMagAmmo = Integer.parseInt(nameSiblings.get(1).asString());
            totalAmmo = Integer.parseInt(nameSiblings.get(2).asString().replace(" ", ""));
            profiler.swap("Parse a lore");
            for (Text loreLine : lore) {
                List<Text> loreLineSiblings = loreLine.getSiblings();
                if (loreLineSiblings.size() < 2) continue;
                String itemName = loreLineSiblings.get(0).asString();
                String itemValue = loreLineSiblings.get(1).asString();

                if (itemName.equals("Reload Duration : ")) {
                    reloadDuration = Integer.parseInt(itemValue);
                } else if (itemName.equals("Reload Amount : ")) {
                    reloadAmount = Integer.parseInt(itemValue);
                }
            }
        } catch (Exception ex) {
            gunName = "";
            isReloading = false;
            inMagAmmo = -1;
            totalAmmo = -1;
            reloadDuration = -1;
            reloadAmount = -1;
        }

        profiler.swap("Set values");
        this.name = gunName;
        this.reloadTime = reloadDuration;
        this.maxMagazineAmmo = reloadAmount;
        this.inMagazineAmmo = inMagAmmo;
        this.isReloading = isReloading;
        this.totalAmmo = totalAmmo;

        profiler.pop();
    }

    public void parse(ItemStack item) {
        if (item.getItem().equals(Items.AIR)) {
            this.init();
            return;
        }

        String[] nameArray = item.getName().asString().split(" ");
        Text[] lore = getLore(item);

        try {
            this.isReloading = nameArray[0].startsWith("§c");

            this.name = nameArray[0].substring(2);
            this.inMagazineAmmo = Integer.parseInt(nameArray[3].substring(4, nameArray[3].length() - 4));
            this.totalAmmo = Integer.parseInt(nameArray[6]);

            this.reloadTime = Integer.parseInt(lore[1].asString().substring(22));
            this.maxMagazineAmmo = Integer.parseInt(lore[2].asString().substring(20));
        } catch (Exception ex) {
            this.init();
        }
    }

    @Override
    public String toString() {
        return String.format("{Name: \"%s\", InMagazineAmmo: %d, MaxMagazineAmmo: %d, TotalAmmo: %d, ReloadTime: %d}", this.name, this.inMagazineAmmo, this.maxMagazineAmmo, this.totalAmmo, this.reloadTime);
    }
}
