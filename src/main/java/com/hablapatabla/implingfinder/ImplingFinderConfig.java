package com.hablapatabla.implingfinder;

import lombok.Getter;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.plugins.gpu.config.ColorBlindMode;

@ConfigGroup(ImplingFinderPlugin.CONFIG_GROUP)
public interface ImplingFinderConfig extends Config
{
    public String HIDE_BUTTON = "hideButton";
    public String POST_ENDPOINT_KEYNAME = "post endpoint";
    public String GET_ENDPOINT_KEYNAME = "get endpoint";
    public String SPLASH_SEEN = "splashSeen";

    @ConfigItem(
            keyName = POST_ENDPOINT_KEYNAME,
            position = 0,
            name = "POST endpoint",
            description = "Web endpoint to post star data to"
    )
    default String implingFinderPostEndpointConfig()
    {
        return "https://puos0bfgxc2lno5-implingdb.adb.us-phoenix-1.oraclecloudapps.com/ords/impling/imp/implings";
    }

    @ConfigItem(
            keyName = GET_ENDPOINT_KEYNAME,
            position = 1,
            name = "GET endpoint",
            description = "Web endpoint to get star data from, only \"Any\" option will work"
    )
    default String implingFinderGetEndpointConfig()
    {
        return "https://puos0bfgxc2lno5-implingdb.adb.us-phoenix-1.oraclecloudapps.com/ords/impling/imp/implings";
    }

    @ConfigItem(
            position = 2,
            keyName = "showImpType",
            name = "I'm a cool Kid",
            description = "Only cool kids leave this checked"
    )
    default boolean showImpType() {
        return false;
    }

    @ConfigItem(
            position = 3,
            keyName = "hideButton",
            name = "Hide Button",
            description = "Hides the button from your Runelite sidebar"
    )
    default boolean hideButton() { return false; }

    @ConfigItem(
            position = 4,
            keyName = "uninstallMessage",
            name = "Secret message!",
            description = "There's a secret message in this dropdown"
    )
    default ImplingFinderConfigMessage colorBlindMode()
    {
        return ImplingFinderConfigMessage.PLEASE;
    }

    @ConfigItem(
            position = 5,
            keyName = "splashSeen",
            name = "I've seen the splash page",
            description = "This plugin has been opened before"
    )
    default boolean beenOpened() { return false; }
}