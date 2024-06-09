package com.lucidplugins.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ItemID;
import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum Weapon
{
    NOTHING(List.of(-1), WeaponType.OTHER),
    BOW_OF_FAERDHINEN(List.of(
        ItemID.BOW_OF_FAERDHINEN,
        ItemID.BOW_OF_FAERDHINEN_27187,
        ItemID.BOW_OF_FAERDHINEN_C,
        ItemID.BOW_OF_FAERDHINEN_C_25869,
        ItemID.BOW_OF_FAERDHINEN_C_25884,
        ItemID.BOW_OF_FAERDHINEN_C_25886,
        ItemID.BOW_OF_FAERDHINEN_C_25888,
        ItemID.BOW_OF_FAERDHINEN_C_25890,
        ItemID.BOW_OF_FAERDHINEN_C_25892,
        ItemID.BOW_OF_FAERDHINEN_C_25894,
        ItemID.BOW_OF_FAERDHINEN_C_25896
    ), WeaponType.RANGED),
    TWISTED_BOW(List.of(
            ItemID.TWISTED_BOW
    ), WeaponType.RANGED),
    DRAGON_CROSSBOW(List.of(
            ItemID.DRAGON_CROSSBOW,
            ItemID.DRAGON_CROSSBOW_CR
    ), WeaponType.RANGED),
    RUNE_CROSSBOW(List.of(
            ItemID.RUNE_CROSSBOW,
            ItemID.RUNE_CROSSBOW_OR
    ), WeaponType.RANGED),
    TOXIC_BLOWPIPE(List.of(
            ItemID.TOXIC_BLOWPIPE,
            ItemID.BLAZING_BLOWPIPE
    ), WeaponType.RANGED),
    ARMADYL_CROSSBOW(List.of(
            ItemID.ARMADYL_CROSSBOW,
            ItemID.ARMADYL_CROSSBOW_23611
    ), WeaponType.RANGED),
    DRAGON_HUNTER_CROSSBOW(List.of(
            ItemID.DRAGON_HUNTER_CROSSBOW,
            ItemID.DRAGON_HUNTER_CROSSBOW_B,
            ItemID.DRAGON_HUNTER_CROSSBOW_T
    ), WeaponType.RANGED),
    KODAI_WAND(List.of(
            ItemID.KODAI_WAND,
            ItemID.KODAI_WAND_23626
    ), WeaponType.MAGIC),
    ELDRITCH_NIGHTMARE_STAFF(List.of(
            ItemID.ELDRITCH_NIGHTMARE_STAFF
    ), WeaponType.MAGIC),
    HARMONIZED_NIGHTMARE_STAFF(List.of(
            ItemID.HARMONISED_NIGHTMARE_STAFF
    ), WeaponType.MAGIC),
    VOLATILE_NIGHTMARE_STAFF(List.of(
            ItemID.VOLATILE_NIGHTMARE_STAFF,
            ItemID.VOLATILE_NIGHTMARE_STAFF_25517
    ), WeaponType.MAGIC),
    NIGHTMARE_STAFF(List.of(
            ItemID.NIGHTMARE_STAFF
    ), WeaponType.MAGIC),
    ANCIENT_SCEPTRE(List.of(
            ItemID.ANCIENT_SCEPTRE,
            ItemID.ANCIENT_SCEPTRE_L
    ), WeaponType.MAGIC),
    BLOOD_ANCIENT_SCEPTRE(List.of(
            ItemID.BLOOD_ANCIENT_SCEPTRE,
            ItemID.BLOOD_ANCIENT_SCEPTRE_L,
            ItemID.BLOOD_ANCIENT_SCEPTRE_28260
    ), WeaponType.MAGIC),
    ICE_ANCIENT_SCEPTRE(List.of(
            ItemID.ICE_ANCIENT_SCEPTRE,
            ItemID.ICE_ANCIENT_SCEPTRE_L,
            ItemID.ICE_ANCIENT_SCEPTRE_28262
    ), WeaponType.MAGIC),
    ANCIENT_STAFF(List.of(
            ItemID.ANCIENT_STAFF,
            ItemID.ANCIENT_STAFF_20431
    ), WeaponType.MAGIC),
    MASTER_WAND(List.of(
            ItemID.MASTER_WAND,
            ItemID.MASTER_WAND_20560
    ), WeaponType.MAGIC),
    TOXIC_TRIDENT(List.of(
            ItemID.TRIDENT_OF_THE_SWAMP,
            ItemID.TRIDENT_OF_THE_SWAMP_E
    ), WeaponType.MAGIC),
    TRIDENT(List.of(
            ItemID.TRIDENT_OF_THE_SEAS,
            ItemID.TRIDENT_OF_THE_SEAS_FULL,
            ItemID.TRIDENT_OF_THE_SEAS_E
    ), WeaponType.MAGIC),
    WARPED_SCEPTRE(List.of(
            ItemID.WARPED_SCEPTRE
    ), WeaponType.MAGIC);
    final List<Integer> ids;
    final WeaponType weaponType;

    public static Weapon nothing()
    {
        return NOTHING;
    }

    public static Weapon getWeaponForId(int id)
    {
        return Arrays.stream(Weapon.values()).filter(wep -> wep.getIds().stream().anyMatch(i -> i == id)).findFirst().orElse(NOTHING);
    }
}

