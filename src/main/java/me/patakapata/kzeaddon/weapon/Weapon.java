package me.patakapata.kzeaddon.weapon;

public interface Weapon {

    /**
     * 名前
     */
    String getName();

    /**
     * 減衰度
     */
    float getAttenuation();

    /**
     * 射程
     */
    float maxRange();

    /**
     * 最大貫通数
     */
    int getPenetrability();

    /**
     * 貫通率
     */
    float getPiercingPercent();

    /**
     * スコープレベル
     * -1 はスコープ無し
     */
    float scopeLevel();

    /**
     * リロード時間(tick)
     */
    int getReloadDuration();

    /**
     * ダメージ
     */
    int getDamage();

    /**
     * HSダメージ
     */
    int getHeadshotDamage();

    /**
     * ノクバ
     */
    float getKnockBack();

    /**
     * クールダウン(?)
     */
    int getCooldown();

    /**
     * スピード(?)
     */
    float getSpeed();

    /**
     * 最大弾薬数
     */
    int getTotalAmmo();

    /**
     * 精度
     */
    float getAccuracy();

}
