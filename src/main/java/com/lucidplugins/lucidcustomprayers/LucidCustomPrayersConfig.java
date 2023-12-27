package com.lucidplugins.lucidcustomprayers;

import net.runelite.api.Prayer;
import net.runelite.client.config.*;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

@ConfigGroup("lucid-custom-prayers")
public interface LucidCustomPrayersConfig extends Config
{
    @ConfigSection(
            name = "General",
            description = "Settings that don't belong to a specific preset",
            position = 0
    )
    String generalSection = "General";

    @ConfigSection(
            name = "Preset Loading/Saving",
            description = "Save/Load a custom preset",
            position = 1
    )
    String presetSection = "Preset Loading/Saving";

    @ConfigSection(
            name = "Custom Prayer 1",
            description = "Custom Prayer 1",
            position = 2
    )
    String prayer1Section = "Custom Prayer 1";

    @ConfigSection(
            name = "Custom Prayer 2",
            description = "Custom Prayer 2",
            position = 3
    )
    String prayer2Section = "Custom Prayer 2";

    @ConfigSection(
            name = "Custom Prayer 3",
            description = "Custom Prayer 3",
            position = 4
    )
    String prayer3Section = "Custom Prayer 3";

    @ConfigSection(
            name = "Custom Prayer 4",
            description = "Custom Prayer 4",
            position = 5
    )
    String prayer4Section = "Custom Prayer 4";

    @ConfigSection(
            name = "Custom Prayer 5",
            description = "Custom Prayer 5",
            position = 6
    )
    String prayer5Section = "Custom Prayer 5";

    @ConfigSection(
            name = "Custom Prayer 6",
            description = "Custom Prayer 6",
            position = 7
    )
    String prayer6Section = "Custom Prayer 6";

    // General Section

    @ConfigItem(
            name = "Toggle Debug Mode",
            description = "Toggle Debug Mode on to see events being fired in your chatbox",
            position = 0,
            keyName = "debugMode",
            section = generalSection
    )
    default boolean debugMode()
    {
        return false;
    }

    @ConfigItem(
            name = "Ignore non-target events",
            description = "Don't debug events if you're not the target",
            position = 1,
            keyName = "hideNonTargetEventsDebug",
            section = generalSection
    )
    default boolean hideNonTargetEventsDebug()
    {
        return false;
    }

    @ConfigItem(
            name = "Debug Animation Changed",
            description = "Toggle Debug Output for Animation Changed Event",
            position = 2,
            keyName = "debugAnimationChanged",
            section = generalSection
    )
    default boolean debugAnimationChanged()
    {
        return false;
    }

    @ConfigItem(
            name = "Debug NPC Spawned",
            description = "Toggle Debug Output for NPC Spawned Event",
            position = 3,
            keyName = "debugNpcSpawned",
            section = generalSection
    )
    default boolean debugNpcSpawned()
    {
        return false;
    }

    @ConfigItem(
            name = "Debug NPC Despawned",
            description = "Toggle Debug Output for NPC Despawned Event",
            position = 4,
            keyName = "debugNpcDespawned",
            section = generalSection
    )
    default boolean debugNpcDespawned()
    {
        return false;
    }

    @ConfigItem(
            name = "Debug NPC Changed",
            description = "Toggle Debug Output for NPC Changed Event",
            position = 5,
            keyName = "debugNpcChanged",
            section = generalSection
    )
    default boolean debugNpcChanged()
    {
        return false;
    }

    @ConfigItem(
            name = "Debug Projectile Spawned",
            description = "Toggle Debug Output for Projectile Spawned Event",
            position = 6,
            keyName = "debugProjectileSpawned",
            section = generalSection
    )
    default boolean debugProjectileSpawned()
    {
        return false;
    }

    @ConfigItem(
            name = "Debug Graphics Created",
            description = "Toggle Debug Output for Graphics Created Event",
            position = 7,
            keyName = "debugGraphicsCreated",
            section = generalSection
    )
    default boolean debugGraphicsCreated()
    {
        return false;
    }

    @ConfigItem(
            name = "Debug GameObject Spawned",
            description = "Toggle Debug Output for GameObject Spawned Event",
            position = 8,
            keyName = "debugGameObjectSpawned",
            section = generalSection
    )
    default boolean debugGameObjectSpawned()
    {
        return false;
    }

    @ConfigItem(
            name = "Debug Other Interact You",
            description = "Toggle Debug Output for Other Interact You Event",
            position = 9,
            keyName = "debugOtherInteractYou",
            section = generalSection
    )
    default boolean debugOtherInteractYou()
    {
        return false;
    }

    @ConfigItem(
            name = "Debug You Interact Other",
            description = "Toggle Debug Output for You Interact Other Event",
            position = 10,
            keyName = "debugYouInteractOther",
            section = generalSection
    )
    default boolean debugYouInteractOther()
    {
        return false;
    }

    @ConfigItem(
            name = "Debug Item Equipped",
            description = "Toggle Debug Output for Item Equipped Event",
            position = 11,
            keyName = "debugItemEquipped",
            section = generalSection
    )
    default boolean debugItemEquipped()
    {
        return false;
    }

    // Preset Loading/Saving

    @ConfigItem(
            name = "Preset Name",
            description = "Name of the preset (replaces all non-alphanumerical characters with a space)",
            position = 0,
            keyName = "presetName",
            section = presetSection
    )
    default String presetName()
    {
        return "";
    }

    @ConfigItem(
            name = "Load Preset Hotkey",
            description =  "Loads the preset with the saved preset in your runelite/lucid-custom-prayers/ folder",
            position = 1,
            keyName = "loadPresetHotkey",
            section = presetSection
    )
    default Keybind loadPresetHotkey()
    {
        return new Keybind(KeyEvent.VK_F11, InputEvent.CTRL_DOWN_MASK);
    }

    @ConfigItem(
            name = "Save Preset Hotkey",
            description =  "Saves the preset as a JSON file to your runelite/lucid-custom-prayers/ folder",
            position = 2,
            keyName = "savePresetHotkey",
            section = presetSection
    )
    default Keybind savePresetHotkey()
    {
        return new Keybind(KeyEvent.VK_F12, InputEvent.CTRL_DOWN_MASK);
    }

    // Custom Prayer 1
    @ConfigItem(
            name = "Enable This Preset?",
            description = "Toggle this off if you want to disable this custom prayer from doing anything",
            position = 0,
            keyName = "activated1",
            section = prayer1Section
    )
    default boolean activated1()
    {
        return true;
    }
    @ConfigItem(
            name = "Event IDs",
            description = "Enter the IDs for the event that will trigger this prayer, separated by commas",
            position = 1,
            keyName = "pray1Ids",
            section = prayer1Section
    )
    default String pray1Ids()
    {
        return "";
    }
    @ConfigItem(
            name = "Tick delays",
            description = "How many ticks after the event to activate the prayer, separate multiple values by comma."
            + "One delay per ID required or leaving this completely blank will default to instant activation.",
            position = 2,
            keyName = "pray1delays",
            section = prayer1Section
    )
    default String pray1delays()
    {
        return "";
    }
    @ConfigItem(
            name = "Prayer",
            description = "Which prayer will be activated?",
            position = 3,
            keyName = "pray1choice",
            section = prayer1Section
    )
    default Prayer pray1choice()
    {
        return Prayer.PIETY;
    }
    @ConfigItem(
            name = "Event",
            description = "Type of event these IDs correlate to",
            position = 4,
            keyName = "eventType1",
            section = prayer1Section
    )
    default EventType eventType1()
    {
        return EventType.ANIMATION_CHANGED;
    }
    @ConfigItem(
            name = "Toggle Prayer?",
            description = "Having this on will toggle the prayer on/off depending on it's current state instead of only activating it.",
            position = 5,
            keyName = "toggle1",
            section = prayer1Section
    )
    default boolean toggle1()
    {
        return false;
    }

    @ConfigItem(
            name = "Ignore non-target NPC events?",
            description = "Having this on will ignore certain incoming events if youre not being targeted by the npc performing the action.<br>"
            + "Having it disabled will not ignore any events at all.",
            position = 6,
            keyName = "ignoreNonTargetEvents1",
            section = prayer1Section
    )
    default boolean ignoreNonTargetEvents1()
    {
        return false;
    }

    // Prayer 2
    @ConfigItem(
            name = "Enable This Preset?",
            description = "Toggle this off if you want to disable this custom prayer from doing anything",
            position = 0,
            keyName = "activated2",
            section = prayer2Section
    )
    default boolean activated2()
    {
        return true;
    }

    @ConfigItem(
            name = "Event IDs",
            description = "Enter the IDs for the event that will trigger this prayer, separated by commas",
            position = 1,
            keyName = "pray2Ids",
            section = prayer2Section
    )
    default String pray2Ids()
    {
        return "";
    }
    @ConfigItem(
            name = "Tick delays",
            description = "How many ticks after the event to activate the prayer, separate multiple values by comma."
                    + "One delay per ID required or leaving this completely blank will default to instant activation.",
            position = 2,
            keyName = "pray2delays",
            section = prayer2Section
    )
    default String pray2delays()
    {
        return "";
    }
    @ConfigItem(
            name = "Prayer",
            description = "Which prayer will be activated?",
            position = 3,
            keyName = "pray2choice",
            section = prayer2Section
    )
    default Prayer pray2choice()
    {
        return Prayer.PIETY;
    }
    @ConfigItem(
            name = "Event",
            description = "Type of event these IDs correlate to",
            position = 4,
            keyName = "eventType2",
            section = prayer2Section
    )
    default EventType eventType2()
    {
        return EventType.ANIMATION_CHANGED;
    }
    @ConfigItem(
            name = "Toggle Prayer?",
            description = "Having this on will toggle the prayer on/off depending on it's current state instead of only activating it.",
            position = 5,
            keyName = "toggle2",
            section = prayer2Section
    )
    default boolean toggle2()
    {
        return false;
    }

    @ConfigItem(
            name = "Ignore non-target NPC events?",
            description = "Having this on will ignore certain incoming events if youre not being targeted by the npc performing the action.<br>"
                    + "Having it disabled will not ignore any events at all.",
            position = 6,
            keyName = "ignoreNonTargetEvents2",
            section = prayer2Section
    )
    default boolean ignoreNonTargetEvents2()
    {
        return false;
    }

    // Prayer 3
    @ConfigItem(
            name = "Enable This Preset?",
            description = "Toggle this off if you want to disable this custom prayer from doing anything",
            position = 0,
            keyName = "activated3",
            section = prayer3Section
    )
    default boolean activated3()
    {
        return true;
    }
    @ConfigItem(
            name = "Event IDs",
            description = "Enter the IDs for the event that will trigger this prayer, separated by commas",
            position = 1,
            keyName = "pray3Ids",
            section = prayer3Section
    )
    default String pray3Ids()
    {
        return "";
    }
    @ConfigItem(
            name = "Tick delays",
            description = "How many ticks after the event to activate the prayer, separate multiple values by comma."
                    + "One delay per ID required or leaving this completely blank will default to instant activation.",
            position = 2,
            keyName = "pray3delays",
            section = prayer3Section
    )
    default String pray3delays()
    {
        return "";
    }
    @ConfigItem(
            name = "Prayer",
            description = "Which prayer will be activated?",
            position = 3,
            keyName = "pray3choice",
            section = prayer3Section
    )
    default Prayer pray3choice()
    {
        return Prayer.PIETY;
    }
    @ConfigItem(
            name = "Event",
            description = "Type of event these IDs correlate to",
            position = 4,
            keyName = "eventType3",
            section = prayer3Section
    )
    default EventType eventType3()
    {
        return EventType.ANIMATION_CHANGED;
    }
    @ConfigItem(
            name = "Toggle Prayer?",
            description = "Having this on will toggle the prayer on/off depending on it's current state instead of only activating it.",
            position = 5,
            keyName = "toggle3",
            section = prayer3Section
    )
    default boolean toggle3()
    {
        return false;
    }

    @ConfigItem(
            name = "Ignore non-target NPC events?",
            description = "Having this on will ignore certain incoming events if youre not being targeted by the npc performing the action.<br>"
                    + "Having it disabled will not ignore any events at all.",
            position = 6,
            keyName = "ignoreNonTargetEvents3",
            section = prayer3Section
    )
    default boolean ignoreNonTargetEvents3()
    {
        return false;
    }

    // Prayer 4
    @ConfigItem(
            name = "Enable This Preset?",
            description = "Toggle this off if you want to disable this custom prayer from doing anything",
            position = 0,
            keyName = "activated4",
            section = prayer4Section
    )
    default boolean activated4()
    {
        return true;
    }
    @ConfigItem(
            name = "Event IDs",
            description = "Enter the IDs for the event that will trigger this prayer, separated by commas",
            position = 1,
            keyName = "pray4Ids",
            section = prayer4Section
    )
    default String pray4Ids()
    {
        return "";
    }
    @ConfigItem(
            name = "Tick delays",
            description = "How many ticks after the event to activate the prayer, separate multiple values by comma."
                    + "One delay per ID required or leaving this completely blank will default to instant activation.",
            position = 2,
            keyName = "pray4delays",
            section = prayer4Section
    )
    default String pray4delays()
    {
        return "";
    }
    @ConfigItem(
            name = "Prayer",
            description = "Which prayer will be activated?",
            position = 3,
            keyName = "pray4choice",
            section = prayer4Section
    )
    default Prayer pray4choice()
    {
        return Prayer.PIETY;
    }
    @ConfigItem(
            name = "Event",
            description = "Type of event these IDs correlate to",
            position = 4,
            keyName = "eventType4",
            section = prayer4Section
    )
    default EventType eventType4()
    {
        return EventType.ANIMATION_CHANGED;
    }
    @ConfigItem(
            name = "Toggle Prayer?",
            description = "Having this on will toggle the prayer on/off depending on it's current state instead of only activating it.",
            position = 5,
            keyName = "toggle4",
            section = prayer4Section
    )
    default boolean toggle4()
    {
        return false;
    }

    @ConfigItem(
            name = "Ignore non-target NPC events?",
            description = "Having this on will ignore certain incoming events if youre not being targeted by the npc performing the action.<br>"
                    + "Having it disabled will not ignore any events at all.",
            position = 6,
            keyName = "ignoreNonTargetEvents4",
            section = prayer4Section
    )
    default boolean ignoreNonTargetEvents4()
    {
        return false;
    }

    // Prayer 5
    @ConfigItem(
            name = "Enable This Preset?",
            description = "Toggle this off if you want to disable this custom prayer from doing anything",
            position = 0,
            keyName = "activated5",
            section = prayer5Section
    )
    default boolean activated5()
    {
        return true;
    }
    @ConfigItem(
            name = "Event IDs",
            description = "Enter the IDs for the event that will trigger this prayer, separated by commas",
            position = 1,
            keyName = "pray5Ids",
            section = prayer5Section
    )
    default String pray5Ids()
    {
        return "";
    }
    @ConfigItem(
            name = "Tick delays",
            description = "How many ticks after the event to activate the prayer, separate multiple values by comma."
                    + "One delay per ID required or leaving this completely blank will default to instant activation.",
            position = 2,
            keyName = "pray5delays",
            section = prayer5Section
    )
    default String pray5delays()
    {
        return "";
    }
    @ConfigItem(
            name = "Prayer",
            description = "Which prayer will be activated?",
            position = 3,
            keyName = "pray5choice",
            section = prayer5Section
    )
    default Prayer pray5choice()
    {
        return Prayer.PIETY;
    }
    @ConfigItem(
            name = "Event",
            description = "Type of event these IDs correlate to",
            position = 4,
            keyName = "eventType5",
            section = prayer5Section
    )
    default EventType eventType5()
    {
        return EventType.ANIMATION_CHANGED;
    }
    @ConfigItem(
            name = "Toggle Prayer?",
            description = "Having this on will toggle the prayer on/off depending on it's current state instead of only activating it.",
            position = 5,
            keyName = "toggle5",
            section = prayer5Section
    )
    default boolean toggle5()
    {
        return false;
    }

    @ConfigItem(
            name = "Ignore non-target NPC events?",
            description = "Having this on will ignore certain incoming events if youre not being targeted by the npc performing the action.<br>"
                    + "Having it disabled will not ignore any events at all.",
            position = 6,
            keyName = "ignoreNonTargetEvents5",
            section = prayer5Section
    )
    default boolean ignoreNonTargetEvents5()
    {
        return false;
    }

    // Prayer 6
    @ConfigItem(
            name = "Enable This Preset?",
            description = "Toggle this off if you want to disable this custom prayer from doing anything",
            position = 0,
            keyName = "activated6",
            section = prayer6Section
    )
    default boolean activated6()
    {
        return true;
    }
    @ConfigItem(
            name = "Event IDs",
            description = "Enter the IDs for the event that will trigger this prayer, separated by commas",
            position = 1,
            keyName = "pray6Ids",
            section = prayer6Section
    )
    default String pray6Ids()
    {
        return "";
    }
    @ConfigItem(
            name = "Tick delays",
            description = "How many ticks after the event to activate the prayer, separate multiple values by comma."
                    + "One delay per ID required or leaving this completely blank will default to instant activation.",
            position = 2,
            keyName = "pray6delays",
            section = prayer6Section
    )
    default String pray6delays()
    {
        return "";
    }
    @ConfigItem(
            name = "Prayer",
            description = "Which prayer will be activated?",
            position = 3,
            keyName = "pray6choice",
            section = prayer6Section
    )
    default Prayer pray6choice()
    {
        return Prayer.PIETY;
    }
    @ConfigItem(
            name = "Event",
            description = "Type of event these IDs correlate to",
            position = 4,
            keyName = "eventType6",
            section = prayer6Section
    )
    default EventType eventType6()
    {
        return EventType.ANIMATION_CHANGED;
    }
    @ConfigItem(
            name = "Toggle Prayer?",
            description = "Having this on will toggle the prayer on/off depending on it's current state instead of only activating it.",
            position = 5,
            keyName = "toggle6",
            section = prayer6Section
    )
    default boolean toggle6()
    {
        return false;
    }

    @ConfigItem(
            name = "Ignore non-target NPC events?",
            description = "Having this on will ignore certain incoming events if youre not being targeted by the npc performing the action.<br>"
                    + "Having it disabled will not ignore any events at all.",
            position = 6,
            keyName = "ignoreNonTargetEvents6",
            section = prayer6Section
    )
    default boolean ignoreNonTargetEvents6()
    {
        return false;
    }

    // Prayer 7

    @ConfigSection(
            name = "Custom Prayer 7",
            description = "Custom Prayer 7",
            position = 7
    )
    String prayer7Section = "Custom Prayer 7";

    @ConfigItem(
            name = "Enable This Preset?",
            description = "Toggle this off if you want to disable this custom prayer from doing anything",
            position = 0,
            keyName = "activated7",
            section = prayer7Section
    )
    default boolean activated7()
    {
        return true;
    }
    @ConfigItem(
            name = "Event IDs",
            description = "Enter the IDs for the event that will trigger this prayer, separated by commas",
            position = 1,
            keyName = "pray7Ids",
            section = prayer7Section
    )
    default String pray7Ids()
    {
        return "";
    }
    @ConfigItem(
            name = "Tick delays",
            description = "How many ticks after the event to activate the prayer, separate multiple values by comma."
                    + "One delay per ID required or leaving this completely blank will default to instant activation.",
            position = 2,
            keyName = "pray7delays",
            section = prayer7Section
    )
    default String pray7delays()
    {
        return "";
    }
    @ConfigItem(
            name = "Prayer",
            description = "Which prayer will be activated?",
            position = 3,
            keyName = "pray7choice",
            section = prayer7Section
    )
    default Prayer pray7choice()
    {
        return Prayer.PIETY;
    }
    @ConfigItem(
            name = "Event",
            description = "Type of event these IDs correlate to",
            position = 4,
            keyName = "eventType7",
            section = prayer7Section
    )
    default EventType eventType7()
    {
        return EventType.ANIMATION_CHANGED;
    }
    @ConfigItem(
            name = "Toggle Prayer?",
            description = "Having this on will toggle the prayer on/off depending on it's current state instead of only activating it.",
            position = 5,
            keyName = "toggle7",
            section = prayer7Section
    )
    default boolean toggle7()
    {
        return false;
    }

    @ConfigItem(
            name = "Ignore non-target NPC events?",
            description = "Having this on will ignore certain incoming events if youre not being targeted by the npc performing the action.<br>"
                    + "Having it disabled will not ignore any events at all.",
            position = 6,
            keyName = "ignoreNonTargetEvents7",
            section = prayer7Section
    )
    default boolean ignoreNonTargetEvents7()
    {
        return false;
    }

    // Prayer 8

    @ConfigSection(
            name = "Custom Prayer 8",
            description = "Custom Prayer 8",
            position = 8
    )
    String prayer8Section = "Custom Prayer 8";
    @ConfigItem(
            name = "Enable This Preset?",
            description = "Toggle this off if you want to disable this custom prayer from doing anything",
            position = 0,
            keyName = "activated8",
            section = prayer8Section
    )
    default boolean activated8()
    {
        return true;
    }
    @ConfigItem(
            name = "Event IDs",
            description = "Enter the IDs for the event that will trigger this prayer, separated by commas",
            position = 1,
            keyName = "pray8Ids",
            section = prayer8Section
    )
    default String pray8Ids()
    {
        return "";
    }
    @ConfigItem(
            name = "Tick delays",
            description = "How many ticks after the event to activate the prayer, separate multiple values by comma."
                    + "One delay per ID required or leaving this completely blank will default to instant activation.",
            position = 2,
            keyName = "pray8delays",
            section = prayer8Section
    )
    default String pray8delays()
    {
        return "";
    }
    @ConfigItem(
            name = "Prayer",
            description = "Which prayer will be activated?",
            position = 3,
            keyName = "pray8choice",
            section = prayer8Section
    )
    default Prayer pray8choice()
    {
        return Prayer.PIETY;
    }
    @ConfigItem(
            name = "Event",
            description = "Type of event these IDs correlate to",
            position = 4,
            keyName = "eventType8",
            section = prayer8Section
    )
    default EventType eventType8()
    {
        return EventType.ANIMATION_CHANGED;
    }
    @ConfigItem(
            name = "Toggle Prayer?",
            description = "Having this on will toggle the prayer on/off depending on it's current state instead of only activating it.",
            position = 5,
            keyName = "toggle8",
            section = prayer8Section
    )
    default boolean toggle8()
    {
        return false;
    }

    @ConfigItem(
            name = "Ignore non-target NPC events?",
            description = "Having this on will ignore certain incoming events if youre not being targeted by the npc performing the action.<br>"
                    + "Having it disabled will not ignore any events at all.",
            position = 6,
            keyName = "ignoreNonTargetEvents8",
            section = prayer8Section
    )
    default boolean ignoreNonTargetEvents8()
    {
        return false;
    }

    // Prayer 9

    @ConfigSection(
            name = "Custom Prayer 9",
            description = "Custom Prayer 9",
            position = 9
    )
    String prayer9Section = "Custom Prayer 9";
    @ConfigItem(
            name = "Enable This Preset?",
            description = "Toggle this off if you want to disable this custom prayer from doing anything",
            position = 0,
            keyName = "activated9",
            section = prayer9Section
    )
    default boolean activated9()
    {
        return true;
    }

    @ConfigItem(
            name = "Event IDs",
            description = "Enter the IDs for the event that will trigger this prayer, separated by commas",
            position = 1,
            keyName = "pray9Ids",
            section = prayer9Section
    )
    default String pray9Ids()
    {
        return "";
    }
    @ConfigItem(
            name = "Tick delays",
            description = "How many ticks after the event to activate the prayer, separate multiple values by comma."
                    + "One delay per ID required or leaving this completely blank will default to instant activation.",
            position = 2,
            keyName = "pray9delays",
            section = prayer9Section
    )
    default String pray9delays()
    {
        return "";
    }
    @ConfigItem(
            name = "Prayer",
            description = "Which prayer will be activated?",
            position = 3,
            keyName = "pray9choice",
            section = prayer9Section
    )
    default Prayer pray9choice()
    {
        return Prayer.PIETY;
    }
    @ConfigItem(
            name = "Event",
            description = "Type of event these IDs correlate to",
            position = 4,
            keyName = "eventType9",
            section = prayer9Section
    )
    default EventType eventType9()
    {
        return EventType.ANIMATION_CHANGED;
    }
    @ConfigItem(
            name = "Toggle Prayer?",
            description = "Having this on will toggle the prayer on/off depending on it's current state instead of only activating it.",
            position = 5,
            keyName = "toggle9",
            section = prayer9Section
    )
    default boolean toggle9()
    {
        return false;
    }

    @ConfigItem(
            name = "Ignore non-target NPC events?",
            description = "Having this on will ignore certain incoming events if youre not being targeted by the npc performing the action.<br>"
                    + "Having it disabled will not ignore any events at all.",
            position = 6,
            keyName = "ignoreNonTargetEvents9",
            section = prayer9Section
    )
    default boolean ignoreNonTargetEvents9()
    {
        return false;
    }

    // Prayer 10

    @ConfigSection(
            name = "Custom Prayer 10",
            description = "Custom Prayer 10",
            position = 10
    )
    String prayer10Section = "Custom Prayer 10";
    @ConfigItem(
            name = "Enable This Preset?",
            description = "Toggle this off if you want to disable this custom prayer from doing anything",
            position = 0,
            keyName = "activated10",
            section = prayer10Section
    )
    default boolean activated10()
    {
        return true;
    }
    @ConfigItem(
            name = "Event IDs",
            description = "Enter the IDs for the event that will trigger this prayer, separated by commas",
            position = 1,
            keyName = "pray10Ids",
            section = prayer10Section
    )
    default String pray10Ids()
    {
        return "";
    }
    @ConfigItem(
            name = "Tick delays",
            description = "How many ticks after the event to activate the prayer, separate multiple values by comma."
                    + "One delay per ID required or leaving this completely blank will default to instant activation.",
            position = 2,
            keyName = "pray10delays",
            section = prayer10Section
    )
    default String pray10delays()
    {
        return "";
    }
    @ConfigItem(
            name = "Prayer",
            description = "Which prayer will be activated?",
            position = 3,
            keyName = "pray10choice",
            section = prayer10Section
    )
    default Prayer pray10choice()
    {
        return Prayer.PIETY;
    }
    @ConfigItem(
            name = "Event",
            description = "Type of event these IDs correlate to",
            position = 4,
            keyName = "eventType10",
            section = prayer10Section
    )
    default EventType eventType10()
    {
        return EventType.ANIMATION_CHANGED;
    }
    @ConfigItem(
            name = "Toggle Prayer?",
            description = "Having this on will toggle the prayer on/off depending on it's current state instead of only activating it.",
            position = 5,
            keyName = "toggle10",
            section = prayer10Section
    )
    default boolean toggle10()
    {
        return false;
    }

    @ConfigItem(
            name = "Ignore non-target NPC events?",
            description = "Having this on will ignore certain incoming events if youre not being targeted by the npc performing the action.<br>"
                    + "Having it disabled will not ignore any events at all.",
            position = 6,
            keyName = "ignoreNonTargetEvents10",
            section = prayer10Section
    )
    default boolean ignoreNonTargetEvents10()
    {
        return false;
    }
}
