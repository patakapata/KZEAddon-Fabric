package com.theboss.kzeaddonfabric.widgets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.theboss.kzeaddonfabric.KZEAddon;
import com.theboss.kzeaddonfabric.enums.Anchor;
import com.theboss.kzeaddonfabric.enums.WeaponSlot;
import com.theboss.kzeaddonfabric.ingame.KZEInformation;
import com.theboss.kzeaddonfabric.screen.WidgetEditScreen;
import com.theboss.kzeaddonfabric.screen.WidgetListScreen;
import com.theboss.kzeaddonfabric.screen.WidgetsScreen;
import com.theboss.kzeaddonfabric.utils.Exclude;
import com.theboss.kzeaddonfabric.utils.ExcludeWithAnnotateStrategy;
import com.theboss.kzeaddonfabric.utils.ModUtils;
import net.minecraft.client.MinecraftClient;
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
    private List<TextWidget> textWidgets;
    @Exclude
    private List<Widget> customWidgets;

    private WidgetRenderer() {
        this.configFile = null;
    }

    public WidgetRenderer(File configDir) {
        this.configFile = new File(configDir, "widgets.json");
        this.customWidgets = new ArrayList<>();
        this.textWidgets = new ArrayList<>();
        this.restoreDefault();
        this.load();
    }

    private void restoreDefault() {
        KZEInformation kzeInfo = KZEAddon.kzeInfo;
        this.primaryWidget = new WeaponWidget(kzeInfo.getWeapon(WeaponSlot.PRIMARY), "Main weapon", 0xFF0000, 0xAA0000, 0x00FF00, 0xFF, 1F, new Offset(Anchor.MIDDLE_DOWN, -80, -30), Anchor.MIDDLE_DOWN);
        this.secondaryWidget = new WeaponWidget(kzeInfo.getWeapon(WeaponSlot.SECONDARY), "Sub weapon", 0xFF0000, 0xAA0000, 0x00FF00, 0xFF, 1F, new Offset(Anchor.MIDDLE_DOWN, -60, -30), Anchor.MIDDLE_DOWN);
        this.meleeWidget = new WeaponWidget(kzeInfo.getWeapon(WeaponSlot.MELEE), "Melee Weapon", 0xFF0000, 0xAA0000, 0x00FF00, 0xFF, 1F, new Offset(Anchor.MIDDLE_DOWN, -40, -30), Anchor.MIDDLE_DOWN);
        this.reloadTimeWidget = new ReloadTimeWidget(1F, new Offset(Anchor.MIDDLE_MIDDLE, 5, 5), Anchor.LEFT_UP, 0xFFFFFF, 0xFF);
        this.totalAmmoWidget = new TotalAmmoWidget(1F, new Offset(Anchor.MIDDLE_DOWN, -100, -10), Anchor.RIGHT_MIDDLE, 0xFFFFFF, 0xFF);
        this.textWidgets.clear();
    }

    public void openWidgetListScreen() {
        MinecraftClient mc = MinecraftClient.getInstance();
        List<Widget> widgets = new ArrayList<>();
        widgets.addAll(this.textWidgets);
        widgets.addAll(this.customWidgets);
        widgets.add(this.primaryWidget);
        widgets.add(this.secondaryWidget);
        widgets.add(this.meleeWidget);
        widgets.add(this.reloadTimeWidget);
        widgets.add(this.totalAmmoWidget);
        WidgetListScreen screen = new WidgetListScreen(widgets);
        screen.setParent(mc.currentScreen);
        screen.open(mc);
    }

    private void copy(WidgetRenderer other) throws NullPointerException {
        this.primaryWidget.copy(other.primaryWidget);
        this.secondaryWidget.copy(other.secondaryWidget);
        this.meleeWidget.copy(other.meleeWidget);
        this.reloadTimeWidget.copy(other.reloadTimeWidget);
        this.totalAmmoWidget.copy(other.totalAmmoWidget);
        this.textWidgets.clear();
        this.textWidgets.addAll(other.textWidgets);
    }

    public void load() {
        if (ModUtils.prepareIO(this.configFile)) {
            try (FileReader reader = new FileReader(this.configFile)) {
                WidgetRenderer loaded = GSON.fromJson(reader, WidgetRenderer.class);
                this.copy(loaded);
            } catch (Exception ex) {
                KZEAddon.LOGGER.error("Encounter error while loading widget configuration. Restore default settings");
                this.restoreDefault();
            }
        } else {
            KZEAddon.LOGGER.fatal("Can't create or load widgets file");
        }
    }

    public void save() {
        if (ModUtils.prepareIO(this.configFile)) {
            try (FileWriter writer = new FileWriter(this.configFile, false)) {
                GSON.toJson(this, writer);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            KZEAddon.LOGGER.fatal("Can't create or load widgets file");
        }
    }

    public void addText(TextWidget widget) {
        this.textWidgets.add(widget);
    }

    public boolean removeText(TextWidget widget) {
        return this.textWidgets.remove(widget);
    }

    public void add(Widget widget) {
        this.customWidgets.add(widget);
    }

    public boolean remove(Widget widget) {
        return this.customWidgets.remove(widget);
    }

    public Widget remove(int index) {
        return this.customWidgets.remove(index);
    }

    public boolean contains(Widget widget) {
        return this.customWidgets.contains(widget);
    }

    public int textWidgetCount() {
        return this.textWidgets.size();
    }

    public TextWidget getTextWidget(int index) {
        return this.textWidgets.get(index);
    }

    public Widget getCustomWidget(int index) {
        return this.customWidgets.get(index);
    }

    public void forEachText(Consumer<? super Widget> consumer) {
        this.textWidgets.forEach(consumer);
    }

    public void render(MatrixStack matrices, float delta) {
        Window window = MinecraftClient.getInstance().getWindow();

        this.internalRender(matrices, delta, this.textWidgets, window);
        this.internalRender(matrices, delta, this.customWidgets, window);
        // -------------------------------------------------- //
        // 武器
        this.internalRender(matrices, delta, this.primaryWidget, window);
        this.internalRender(matrices, delta, this.secondaryWidget, window);
        this.internalRender(matrices, delta, this.meleeWidget, window);
        // -------------------------------------------------- //
        // 他
        this.internalRender(matrices, delta, this.reloadTimeWidget, window);
        this.internalRender(matrices, delta, this.totalAmmoWidget, window);
    }

    private void internalRender(MatrixStack matrices, float delta, List<? extends Widget> widgets, Window window) {
        for (Widget widget : widgets) {
            this.internalRender(matrices, delta, widget, window);
        }
    }

    private void internalRender(MatrixStack matrices, float delta, Widget widget, Window window) {
        matrices.push();
        widget.transform(matrices, window);
        widget.render(matrices, delta);
        matrices.pop();
    }

    public void openEditScreen(Widget widget) {
        MinecraftClient mc = MinecraftClient.getInstance();
        WidgetEditScreen screen = WidgetEditScreen.create(widget, mc.currentScreen);
        screen.open(mc);
    }

    public void openEditScreen(BuiltInWidget type) {
        Widget widget;

        switch (type) {
            default:
            case PRIMARY:
                widget = this.primaryWidget;
                break;
            case SECONDARY:
                widget = this.secondaryWidget;
                break;
            case MELEE:
                widget = this.meleeWidget;
                break;
            case RELOAD_TIME:
                widget = this.reloadTimeWidget;
                break;
            case TOTAL_AMMO:
                widget = this.totalAmmoWidget;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        WidgetEditScreen screen = WidgetEditScreen.create(widget, mc.currentScreen);
        screen.open(mc);
    }

    public void openWidgetsScreen() {
        MinecraftClient mc = MinecraftClient.getInstance();
        WidgetsScreen screen = new WidgetsScreen();
        screen.setParent(mc.currentScreen);
        mc.openScreen(screen);
    }

    public enum BuiltInWidget {
        PRIMARY, SECONDARY, MELEE, RELOAD_TIME, TOTAL_AMMO
    }
}
