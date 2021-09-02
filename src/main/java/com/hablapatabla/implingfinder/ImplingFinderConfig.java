package com.hablapatabla.implingfinder;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(ImplingFinderPlugin.CONFIG_GROUP)
public interface ImplingFinderConfig extends Config
{
    @ConfigItem(
            position = 1,
            keyName = "showImpType",
            name = "Show the current impling type",
            description = "Toggle the display of the current imp type"
    )
    default boolean showImpType()
    {
        return false;
    }

    @ConfigItem(
            position = 2,
            keyName = "babyImpling",
            name = "Baby Impling",
            description = "Track baby implings"
    )
    default boolean implingFinderBabyImpling()
    {
        return false;
    }

    @ConfigItem(
            position = 3,
            keyName = "youngImpling",
            name = "Young Impling",
            description = "Track young implings"
    )
    default boolean implingFinderYoungImpling()
    {
        return false;
    }

    @ConfigItem(
            position = 4,
            keyName = "gourmetImpling",
            name = "Gourmet Impling",
            description = "Track gourmet implings"
    )
    default boolean implingFinderGourmetImpling()
    {
        return false;
    }

    @ConfigItem(
            position = 5,
            keyName = "earthImpling",
            name = "Earth Impling",
            description = "Track earth implings"
    )
    default boolean implingFinderEarthImpling()
    {
        return false;
    }

    @ConfigItem(
            position = 6,
            keyName = "essenceImpling",
            name = "Essence Impling",
            description = "Track essence implings"
    )
    default boolean implingFinderEssenceImpling()
    {
        return false;
    }

    @ConfigItem(
            position = 7,
            keyName = "eclecticImpling",
            name = "Eclectic Impling",
            description = "Track eclectic implings"
    )
    default boolean implingFinderEclecticImpling()
    {
        return true;
    }

    @ConfigItem(
            position = 8,
            keyName = "natureImpling",
            name = "Nature Impling",
            description = "Track nature implings"
    )
    default boolean implingFinderNatureImpling()
    {
        return false;
    }

    @ConfigItem(
            position = 9,
            keyName = "magpieImpling",
            name = "Magpie Impling",
            description = "Track magpie implings"
    )
    default boolean implingFinderMagpieImpling()
    {
        return false;
    }

    @ConfigItem(
            position = 10,
            keyName = "ninjaImpling",
            name = "Ninja Impling",
            description = "Track ninja implings"
    )
    default boolean implingFinderNinjaImpling()
    {
        return true;
    }

    @ConfigItem(
            position = 11,
            keyName = "crystalImpling",
            name = "Crystal Impling",
            description = "Track crystal implings"
    )
    default boolean implingFinderCrystalImpling()
    {
        return true;
    }

    @ConfigItem(
            position = 12,
            keyName = "dragonImpling",
            name = "Dragon Impling",
            description = "Track dragon implings"
    )
    default boolean implingFinderDragonImpling()
    {
        return true;
    }

    @ConfigItem(
            position = 13,
            keyName = "luckyImpling",
            name = "Lucky Impling",
            description = "Track lucky implings"
    )
    default boolean implingFinderLuckyImpling()
    {
        return true;
    }

    @ConfigItem(
            position = 14,
            keyName = "fungi",
            name = "Ancient fungi",
            description = "Track ancient fungi"
    )
    default boolean implingFinderFungiImpling()
    {
        return false;
    }

    @ConfigItem(
            position = 15,
            keyName = "maxImplings",
            name = "Max implings",
            description = "How many implings are remembered"
    )
    default int implingFinderMaxImplings()
    {
        return 10;
    }
}