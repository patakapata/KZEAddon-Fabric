package com.theboss.kzeaddonfabric.widgets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.enums.Anchor;
import com.theboss.kzeaddonfabric.enums.WeaponSlot;
import com.theboss.kzeaddonfabric.ingame.KZEInformation;
import com.theboss.kzeaddonfabric.screen.LiteralWidgetsScreen;
import com.theboss.kzeaddonfabric.screen.WidgetArrangementScreen;
import com.theboss.kzeaddonfabric.screen.WidgetsScreen;
import com.theboss.kzeaddonfabric.utils.Exclude;
import com.theboss.kzeaddonfabric.utils.ExcludeWithAnnotateStrategy;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.LowercaseEnumTypeAdapterFactory;
import net.minecraft.util.Util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class WidgetRenderer {
    @Exclude
    private static final Gson GSON = Util.make(() -> {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.addSerializationExclusionStrategy(new ExcludeWithAnnotateStrategy());
        gsonBuilder.disableHtmlEscaping();
        gsonBuilder.registerTypeHierarchyAdapter(Text.class, new Text.Serializer());
        gsonBuilder.registerTypeHierarchyAdapter(Style.class, new Style.Serializer());
        // gsonBuilder.registerTypeAdapter(Widget.class, new Widget.Serializer()); FIXME
        gsonBuilder.registerTypeAdapterFactory(new LowercaseEnumTypeAdapterFactory());
        return gsonBuilder.create();
    });

    @Exclude
    private final File configFile;

    private WeaponWidget primaryWidget;
    private WeaponWidget secondaryWidget;
    private WeaponWidget meleeWidget;
    private ReloadTimeWidget reloadTimeWidget;
    private TotalAmmoWidget totalAmmoWidget;
    private List<LiteralWidget> literalWidgets;
    @Exclude
    private List<Widget> customWidgets;

    private WidgetRenderer() {
        this.configFile = null;
    }

    public WidgetRenderer(File configDir) {
        this.configFile = new File(configDir, "widgets.json");
        this.customWidgets = new ArrayList<>();
        this.restoreDefault();
        this.load();
    }

    private void restoreDefault() {
        KZEInformation kzeInfo = KZEAddon.kzeInfo;
        this.primaryWidget = new WeaponWidget(kzeInfo.getWeapon(WeaponSlot.PRIMARY), 0xFF0000, 0xAA0000, 0x00FF00, 0xFF, -80.0F, -25.0F, 1F, Anchor.MIDDLE_DOWN, Anchor.MIDDLE_DOWN);
        this.secondaryWidget = new WeaponWidget(kzeInfo.getWeapon(WeaponSlot.SECONDARY), 0xFF0000, 0xAA0000, 0x00FF00, 0xFF, -60.0F, -25.0F, 1F, Anchor.MIDDLE_DOWN, Anchor.MIDDLE_DOWN);
        this.meleeWidget = new WeaponWidget(kzeInfo.getWeapon(WeaponSlot.MELEE), 0xFF0000, 0xAA0000, 0x00FF00, 0xFF, -40.0F, -25.0F, 1F, Anchor.MIDDLE_DOWN, Anchor.MIDDLE_DOWN);
        this.reloadTimeWidget = new ReloadTimeWidget(0F, 15F, 1F, Anchor.MIDDLE_MIDDLE, Anchor.MIDDLE_MIDDLE, 0xFFFFFF, 0xFF);
        this.totalAmmoWidget = new TotalAmmoWidget(-95F, -10F, 1F, Anchor.MIDDLE_DOWN, Anchor.RIGHT_MIDDLE, 0xFFFFFF, 0xFF);
        this.literalWidgets = new ArrayList<>();
    }

    public void openOtherWidgetsScreen() {
        MinecraftClient mc = MinecraftClient.getInstance();
        List<Widget> widgets = new ArrayList<>();
        widgets.addAll(this.literalWidgets);
        widgets.addAll(this.customWidgets);
        LiteralWidgetsScreen screen = new LiteralWidgetsScreen(widgets);
        screen.setParent(mc.currentScreen);
        screen.open(mc);
    }

    protected boolean canIOFile(File file) {
        try {
            if (file.exists()) {
                return true;
            } else {
                if (file.getParentFile().exists() || file.getParentFile().mkdirs()) {
                    return file.createNewFile();
                } else {
                    return false;
                }
            }
        } catch (Exception ex) {
            return false;
        }
    }

    private void copy(WidgetRenderer other) throws NullPointerException {
        this.primaryWidget.copy(other.primaryWidget);
        this.secondaryWidget.copy(other.secondaryWidget);
        this.meleeWidget.copy(other.meleeWidget);
        this.reloadTimeWidget.copy(other.reloadTimeWidget);
        this.totalAmmoWidget.copy(other.totalAmmoWidget);
        this.literalWidgets = other.literalWidgets;
    }

    public void load() {
        if (this.canIOFile(this.configFile)) {
            try (FileReader reader = new FileReader(this.configFile)) {
                WidgetRenderer loaded = GSON.fromJson(reader, WidgetRenderer.class);
                this.copy(loaded);
            } catch (Exception ex) {
                ex.printStackTrace();
                KZEAddon.LOGGER.error("Encounter error while loading config. Restore default settings");
                this.restoreDefault();
            }
        } else {
            KZEAddon.LOGGER.fatal("Can't create or load widgets file");
        }
    }

    public void save() {
        if (this.canIOFile(this.configFile)) {
            try (FileWriter writer = new FileWriter(this.configFile, false)) {
                GSON.toJson(this, writer);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            KZEAddon.LOGGER.fatal("Can't create or load widgets file");
        }
    }

    public void addLiteral(LiteralWidget widget) {
        this.literalWidgets.add(widget);
    }

    public void removeLiteral(int index) {
        this.literalWidgets.remove(index);
    }

    public void removeLiteral(LiteralWidget widget) {
        this.literalWidgets.remove(widget);
    }

    public boolean containsLiteral(LiteralWidget widget) {
        return this.literalWidgets.contains(widget);
    }

    public int literalSize() {
        return this.literalWidgets.size();
    }

    public LiteralWidget getLiteral(int index) {
        return this.literalWidgets.get(index);
    }

    public void forEachLiteral(Consumer<? super Widget> consumer) {
        this.literalWidgets.forEach(consumer);
    }

    public void addCustom(Widget widget) {
        this.customWidgets.add(widget);
    }

    public void removeCustom(int index) {
        this.customWidgets.remove(index);
    }

    public void removeCustom(Widget widget) {
        this.customWidgets.remove(widget);
    }

    public boolean containsCustom(Widget widget) {
        return this.customWidgets.contains(widget);
    }

    public void render(MatrixStack matrices, float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        TextRenderer textRenderer = mc.textRenderer;
        Window window = mc.getWindow();
        int width = window.getScaledWidth();
        int height = window.getScaledHeight();
        this.literalWidgets.forEach(it -> it.render(width, height, textRenderer, matrices, delta));
        this.customWidgets.forEach(it -> it.render(width, height, textRenderer, matrices, delta));

        this.primaryWidget.render(width, height, textRenderer, matrices, delta);
        this.secondaryWidget.render(width, height, textRenderer, matrices, delta);
        this.meleeWidget.render(width, height, textRenderer, matrices, delta);
        this.reloadTimeWidget.render(width, height, textRenderer, matrices, delta);
        this.totalAmmoWidget.render(width, height, textRenderer, matrices, delta);
    }

    public void openArrangementScreen(Widget widget) {
        MinecraftClient mc = MinecraftClient.getInstance();
        WidgetArrangementScreen screen = new WidgetArrangementScreen(widget);
        screen.setParent(mc.currentScreen);
        screen.open(mc);
    }

    public void openArrangementScreen(String name) {
        Widget widget;

        switch (name) {
            case "primary":
                widget = this.primaryWidget;
                break;
            case "secondary":
                widget = this.secondaryWidget;
                break;
            case "melee":
                widget = this.meleeWidget;
                break;
            case "reload_time":
                widget = this.reloadTimeWidget;
                break;
            default:
                widget = this.totalAmmoWidget;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        WidgetArrangementScreen screen = new WidgetArrangementScreen(widget);
        screen.setParent(mc.currentScreen);
        screen.open(mc);
    }

    public void openWidgetsScreen() {
        MinecraftClient mc = MinecraftClient.getInstance();
        WidgetsScreen screen = new WidgetsScreen();
        screen.setParent(mc.currentScreen);
        mc.openScreen(screen);
    }
}
