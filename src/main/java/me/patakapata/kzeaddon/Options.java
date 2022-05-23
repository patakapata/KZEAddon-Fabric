package me.patakapata.kzeaddon;

import gg.essential.vigilance.Vigilant;
import gg.essential.vigilance.data.Property;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;

import java.awt.Color;
import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static gg.essential.vigilance.data.PropertyType.*;
import static me.patakapata.kzeaddon.KzeAddonClientMod.*;

@SuppressWarnings("FieldMayBeFinal")
public class Options extends Vigilant {
    private static final String CATEGORY_GENERAL = "category.kzeaddon.option.general";
    private static final String NAME_PREFIX = "option.kzeaddon.";
    private static final String DESC_PREFIX = "desc.kzeaddon.option.";

    @Property(
            category = "general",
            i18nCategory = CATEGORY_GENERAL,
            name = "overrideZombieTeamColor",
            i18nName = NAME_PREFIX + "overrideZombieTeamColor",
            description = DESC_PREFIX + "overrideZombieTeamColor",
            type = SWITCH
    )
    public boolean overrideZombieTeamColor = false;

    @Property(
            category = "general",
            i18nCategory = CATEGORY_GENERAL,
            name = "zombieTeamColor",
            i18nName = NAME_PREFIX + "zombieTeamColor",
            description = DESC_PREFIX + "zombieTeamColor",
            type = COLOR
    )
    public Color zombieTeamColor = Color.GREEN;

    @Property(
            category = "general",
            i18nCategory = CATEGORY_GENERAL,
            name = "overrideGunfireVolume",
            i18nName = NAME_PREFIX + "overrideGunfireVolume",
            description = DESC_PREFIX + "overrideGunfireVolume",
            type = SWITCH
    )
    public boolean overrideGunfireVolume = true;

    @Property(
            category = "general",
            i18nCategory = CATEGORY_GENERAL,
            name = "gunfireVolume",
            i18nName = NAME_PREFIX + "gunfireVolume",
            description = DESC_PREFIX + "gunfireVolume",
            type = DECIMAL_SLIDER,
            maxF = 1f
    )
    public float gunfireVolume = 0.5f;

    @Property(
            category = "general",
            i18nCategory = CATEGORY_GENERAL,
            name = "playNotificationSoundWhenStartVote",
            i18nName = NAME_PREFIX + "playNotificationSoundWhenStartVote",
            description = DESC_PREFIX + "playNotificationSoundWhenStartVote",
            type = SWITCH
    )
    public boolean playNotificationSoundWhenStartVote = true;

    @Property(
            category = "general",
            i18nCategory = CATEGORY_GENERAL,
            name = "hideAlly",
            i18nName = NAME_PREFIX + "hideAlly",
            description = DESC_PREFIX + "hideAlly",
            type = SWITCH
    )
    public boolean hideAlly = false;

    @Property(
            category = "general",
            i18nCategory = CATEGORY_GENERAL,
            name = "overrideShowFriendlyInvisibles",
            i18nName = NAME_PREFIX + "overrideShowFriendlyInvisibles",
            description = DESC_PREFIX + "overrideShowFriendlyInvisibles",
            type = SWITCH
    )
    public boolean overrideShowFriendlyInvisibles = true;

    @Property(
            category = "general",
            i18nCategory = CATEGORY_GENERAL,
            name = "showFriendlyInvisibles",
            i18nName = NAME_PREFIX + "showFriendlyInvisibles",
            description = DESC_PREFIX + "showFriendlyInvisibles",
            type = SWITCH
    )
    public boolean showFriendlyInvisibles = false;

    @Property(
            category = "general",
            i18nCategory = CATEGORY_GENERAL,
            name = "hideFriendlyInvisibleFeatures",
            i18nName = NAME_PREFIX + "hideFriendlyInvisibleFeatures",
            description = DESC_PREFIX + "hideFriendlyInvisibleFeatures",
            type = SWITCH
    )
    public boolean hideFriendlyInvisibleFeatures = false;

    public Options() {
        super(new File("./config/" + KzeAddonClientMod.MOD_ID + ".toml"));
    }

    /**
     * @return 0xRRGGBB
     */
    public int getZombieTeamColor() {
        return this.zombieTeamColor.getRGB() & 0xFFFFFF;
    }
}
