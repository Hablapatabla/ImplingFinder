package com.hablapatabla.implingfinder;

import lombok.Getter;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

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
    default String shootingStarPostEndpointConfig()
    {
        return "https://puos0bfgxc2lno5-implingdb.adb.us-phoenix-1.oraclecloudapps.com/ords/impling/imp/implings";
    }

    @ConfigItem(
            keyName = GET_ENDPOINT_KEYNAME,
            position = 1,
            name = "GET endpoint",
            description = "Web endpoint to get star data from, only \"Any\" option will work"
    )
    default String shootingStarGetEndpointConfig()
    {
        return "https://puos0bfgxc2lno5-implingdb.adb.us-phoenix-1.oraclecloudapps.com/ords/impling/imp/implings";
    }

    @ConfigItem(
            position = 2,
            keyName = "showImpType",
            name = "Cool Kid",
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
            keyName = "leaveThisOneChecked",
            name = "Leave This One Checked",
            description = "Don't uncheck it"
    )
    default boolean leaveThisOneChecked() { return true; }

    @ConfigItem(
            position = 5,
            keyName = "splashSeen",
            name = "Have seen the splash page",
            description = "This plugin has been opened before"
    )
    default boolean beenOpened() { return false; }
}