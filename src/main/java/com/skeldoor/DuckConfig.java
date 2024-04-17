package com.skeldoor;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("DuckDuckGoose")
public interface DuckConfig extends Config{
    @ConfigItem(
            keyName = "silenceDucks",
            name = "Silence Ducks",
            description = "Stops the ducks from quacking and giving encouragement"
    )
    default boolean silenceDucks()
    {
        return false;
    }
}
