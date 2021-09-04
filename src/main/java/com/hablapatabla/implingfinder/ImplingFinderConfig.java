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
            name = "Cool Kid",
            description = "Only cool kids leave this checked"
    )
    default boolean showImpType() {
        return true;
    }
}