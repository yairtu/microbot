package net.runelite.client.plugins.microbot.herbrun;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("example")
public interface HerbrunConfig extends Config {

    @ConfigSection(
            name = "Guide",
            description = "Guide",
            position = 1
    )
    String guideSection = "Guide";

    @ConfigItem(
            keyName = "guide",
            name = "How to use",
            description = "How to use this plugin",
            position = 1,
            section = guideSection
    )
    default String GUIDE() {
        return "Start next to a bank\n" +
                "Have the following in your bank:\n" +
                "- Ardougne cloak\n" +
                "- Ultracompost or Bottomless compost bucket (filled)\n" +
                "- Magic Secatuers\n" +
                "- Seed dibber\n" +
                "- Spade\n" +
                "- Rake\n" +
                "- Ectophial \n" +
                "- Any Quetzal whistle (WITH CHARGES\n" +
                "- Stony Basalt/Trollheim tab/runes\n" +
                "- Icy Basalt\n" +
                "- Skills necklace \n" +
                "- Explorer's Ring \n" +
                "- Herb seeds \n" +
                "- Camelot teleport tab/runes\n" +
                "- Xeric's talisman";

    }

    @ConfigItem(
            keyName = "bottomless",
            name = "Use bottomless bucket?",
            description = "Should  bottomless bucket be withdrawn from the bank?",
            position = 4,
            section = settingsSection
    )
    default boolean COMPOST() {
        return true;
    }

    @ConfigItem(
            keyName = "graceful",
            name = "Equip graceful?",
            description = "Should graceful be equipped from bank?",
            position = 4,
            section = settingsSection
    )
    default boolean GRACEFUL() {
        return true;
    }

    @ConfigItem(
            keyName = "seedTypes",
            name = "Seeds to use",
            description = "Which seeds to use for the herb run?",
            position = 3,
            section = settingsSection
    )
    default HerbrunInfo.seedType SEED() {
        return HerbrunInfo.seedType.KWUARM_SEED;
    }

    @ConfigItem(
            keyName = "cloakType",
            name = "Cloak to use",
            description = "Which cloak to use for the herb run?",
            position = 3,
            section = settingsSection
    )
    default HerbrunInfo.cloak CLOAK() {
        return HerbrunInfo.cloak.ARDOUGNE_CLOAK_2;
    }

    @ConfigItem(
            keyName = "ringType",
            name = "Explorers ring to use",
            description = "Which explorers ring to use for the herb run?",
            position = 3,
            section = settingsSection
    )
    default HerbrunInfo.ring RING() {
        return HerbrunInfo.ring.EXPLORERS_RING_2;
    }

    @ConfigItem(
            keyName = "trollHeim teleport Type",
            name = "Trollheim teleport to use?",
            description = "Which trollheim teleport to use??",
            position = 3,
            section = settingsSection
    )
    default HerbrunInfo.trollheimTeleport TROLLHEIMTELEPORT() { return HerbrunInfo.trollheimTeleport.STONY_BASALT;
    }


    @ConfigSection(
            name = "Settings",
            description = "Settings",
            position = 2
    )
    String settingsSection = "Settings";
/*    @ConfigItem(
            keyName = "Ore",
            name = "Ore",
            description = "Choose the ore",
            position = 0
    )
    default List<String> ORE()
    {
        return Rocks.TIN;
    }*/
}
