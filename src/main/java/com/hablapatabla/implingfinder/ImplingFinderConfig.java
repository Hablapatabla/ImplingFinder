package com.hablapatabla.implingfinder;

import com.hablapatabla.implingfinder.model.ImplingFinderConfigMessage;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(ImplingFinderPlugin.CONFIG_GROUP)
public interface ImplingFinderConfig extends Config
{
    String HIDE_BUTTON = "hideButton";
    String POST_ENDPOINT_KEYNAME = "post endpoint";
    String GET_ENDPOINT_KEYNAME = "get endpoint";
    String SPLASH_SEEN = "splashSeen";

    @ConfigItem(
            keyName = POST_ENDPOINT_KEYNAME,
            position = 0,
            name = "POST endpoint",
            description = "Web endpoint to post star data to"
    )
    default String implingFinderPostEndpointConfig()
    {
        return "https://puos0bfgxc2lno5-implingdb.adb.us-phoenix-1.oraclecloudapps.com/ords/impling/implingdev/dev";
    }

    @ConfigItem(
            keyName = GET_ENDPOINT_KEYNAME,
            position = 1,
            name = "GET endpoint",
            description = "Web endpoint to get star data from, only \"Any\" option will work"
    )
    default String implingFinderGetEndpointConfig()
    {
        return "https://puos0bfgxc2lno5-implingdb.adb.us-phoenix-1.oraclecloudapps.com/ords/impling/implingdev/dev";
    }

    @ConfigItem(
            position = 2,
            keyName = "showImpType",
            name = "I'm a cool kid",
            description = "Only cool kids leave this checked!"
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