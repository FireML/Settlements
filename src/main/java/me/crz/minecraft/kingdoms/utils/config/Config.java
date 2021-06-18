package me.crz.minecraft.kingdoms.utils.config;

import com.google.common.collect.ImmutableSet;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World.Environment;
import org.bukkit.WorldType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Config {

    @ConfigEntry(reload = false)
    @ConfigComment("For internal usage only. Do NOT touch!")
    public static int CONFIG_VERSION = 0;

    @ConfigComment("Log some debug messages about the inner working of the plugin")
    public static boolean DEBUG = true;

    @ConfigComment("Amount of entries that should be shown per page in chat e.g. for command help messages")
    public static int ENTRIES_PER_PAGE = 10;

    @ConfigEntry(old = {"players-in-show"})
    @ConfigComment("How many players should show up in settlement/empire member lists")
    public static int PLAYERS_IN_INFO = 40;

    @ConfigComment("How long in seconds a player has time to confirm certain commands")
    public static int COMMAND_CONFIRM_TIME = 30;

    public static class Storage {
        @ConfigEntry(reload = false)
        public static String TYPE = "mysql";
        @ConfigEntry(reload = false)
        public static String HOST = "localhost";
        @ConfigEntry(reload = false)
        public static int PORT = 3306;
        @ConfigEntry(reload = false)
        public static String USER = "root";
        @ConfigEntry(reload = false)
        public static String PASSWORD = "password";
        @ConfigEntry(reload = false)
        public static String DATABASE = "kingdoms";
        @ConfigEntry(reload = false)
        public static String TABLE_PREFIX = "kingdoms_";
        @ConfigEntry(reload = false)
        public static String URL_PARAMETERS = "";

        public static class Queue {
            @ConfigComment("Max size of the queue before saving happens")
            public static int SIZE = 100;

            @ConfigComment("Time to wait in seconds before saving queued entries")
            public static int WAIT = 2;
        }
    }

    @ConfigComment("Upkeep and mission reset scheduler settings")
    public static class Upkeep {
        public static String TIMEZONE = "UTC";
        public static int HOUR = 0;
    }

    public static class Cache {
        @ConfigEntry(old = {"resident-cache-duration"})
        @ConfigComment("Time in seconds which a resident's data stays in the cache\n" +
                " even if it is not needed elsewhere")
        public static long DURATION_RESIDENT = 120;
    }

    public static class Settlement {
        @ConfigEntry(old = {"village-creation-cost"})
        @ConfigComment("How much a player must pay to create a settlement")
        public static long CREATION_COST = 0;

        @ConfigEntry(old = {"village-start-balance"})
        @ConfigComment("How much money a settlement starts with")
        public static long START_BALANCE = 0;

        @ConfigEntry(old = {"max-village-size"})
        @ConfigComment("Maximum size a settlement can have (0 disables the size check)")
        public static int MAX_SIZE = 0;

        @ConfigEntry(old = {"village-max-tax"})
        @ConfigComment("Maximum value that a settlement's tax can be set to. Set to -1 to disable maximum")
        public static long MAX_TAX = -1;

        @ConfigEntry(old = {"titles-per-rank"})
        @ConfigComment("How many different titles can be configured for a rank")
        public static int TITLES_PER_RANK = 10;

        public static class Leveling {
            @ConfigComment("The amount of levels until a settlement converts from a Village to a Kingdom")
            public static int VILLAGE_MAX_LEVEL = 5;

            @ConfigComment("The maximum amount of Kingdom levels")
            public static int KINGDOM_MAX_LEVEL = 10;
        }

        public static class Claiming {
            @ConfigEntry(old = {"disabled-claiming-worlds"}, parser = "StringSet")
            @ConfigComment("Worlds in which claiming of Settlement chunks are disabled")
            public static Set<String> DISABLED_WORLDS = new HashSet<>();

            @ConfigEntry(old = {"claim-enemy-distance"})
            @ConfigComment("Distance in chunks that have to be between claims of non-allied settlements")
            public static int ENEMY_DISTANCE = 4;

            @ConfigEntry(old = {"claim-max-radius"})
            @ConfigComment("Maximum radius in chunks which can be (un)claimed with the radius (un)claiming commands")
            public static int MAX_RADIUS = 5;

            @ConfigEntry(old = {"mob-flag-check-period"})
            @ConfigComment("The seconds between each check for mobs to be removed in claimed land that has mobspawning turned off\n" +
                    "Set to 0 to disable")
            public static int MOB_FLAG_CHECK_PERIOD = 30;

            public static class Close {
                public static class Unclaim {
                    @ConfigEntry(old = {"close-claims-grace-days"})
                    @ConfigComment("How many days to wait before automatically unclaiming claims that are too close when unallying\n" +
                            "Set to -1 to disable the automatic unclaiming of too close lands")
                    public static int GRACE_DAYS = 0;

                    @ConfigEntry(old = {"close-claims-unclaims-outposts"})
                    @ConfigComment("Whether or not outposts are unclaimed")
                    public static boolean OUTPOSTS = false;
                }

                public static class Building {
                    @ConfigEntry(old = {"close-building-notification-distance", "settlement.close.building-notification.distance"}, reload = false)
                    @ConfigComment("The distance in chunks to other settlements in which to allow building\n" +
                            "Outside of this distance building will not be allowed.\n" +
                            "If a notification message is set in the messages.yml at 'close-claims'" +
                            "then that will be sent to the player or members of the settlement.\n" +
                            "Set to 0 to disable")
                    public static int DISTANCE = 4;

                    @ConfigComment("Whether or not to block building outside the range.")
                    public static boolean BLOCK_OUTSIDE = true;

                    @ConfigComment("Whether or not to block building inside the range.")
                    public static boolean BLOCK_INSIDE = false;

                    @ConfigEntry(parser = "MaterialSet")
                    @ConfigComment("Which blocks are allowed to get built anyways.")
                    public static Set<Material> ALLOWED_BLOCKS;

                    private static Set<String> ALLOWED_BLOCKS_DEFAULT = ImmutableSet.of(
                            "*shulker_box"
                    );

                    @ConfigEntry(old = {"close-building-notification-toggle-time", "settlement.close.building-notification.toggle-time"})
                    @ConfigComment("The time in minutes a disabled close building notification stays around")
                    public static int TOGGLE_TIME = 30;
                }
            }
        }

        public static class Outpost {
            @ConfigEntry(old = {"outpost-creation-cost"})
            @ConfigComment("How much a settlement must pay to create a new outpost")
            public static long CREATION_COST = 0;
        }

        public static class Upkeep {
            @ConfigEntry(old = {"abandon-time.village"})
            @ConfigComment("The amount of minutes that a settlement will stay abandoned after not being able to pay their upkeep\n" +
                    "Abandoned settlements can be raided and fully accessed but nobody can claim them")
            public static long ABANDON_TIME = 30;

            @ConfigEntry(old = {"village-inactive-time"})
            @ConfigComment("The amount of days that a settlement's chief can be offline until it gets deleted\n" +
                    "Set to -1 if you don't want to delete them at all")
            public static int INACTIVE_TIME = 30;

        }
    }

    public static class Empire {
        @ConfigEntry(old = {"claimtransfer-to-empire"})
        @ConfigComment("Whether or not a player can transfer their bought claims to an empire")
        public static boolean CLAIM_TRANSFER = true;

        @ConfigEntry(old = {"empire-creation-cost"})
        @ConfigComment("How much a settlement must pay to create an empire")
        public static long CREATION_COST = 0;

        @ConfigEntry(old = {"empire-start-balance"})
        @ConfigComment("How much money an empire starts with")
        public static long START_BALANCE = 0;

        @ConfigEntry(old = {"empire-max-tax"})
        @ConfigComment("Maximum value that an empire's tax can be set to. Set to -1 to disable maximum")
        public static long MAX_TAX = -1;

        @ConfigEntry(old = {"max-empire-size"})
        @ConfigComment("Maximum size an empire can have (0 disables the size check)")
        public static int MAX_SIZE = 0;

        @ConfigEntry(old = {"empires-world.cooldown"})
        @ConfigComment("The cooldown in seconds after renaming an empire")
        public static int RENAME_COOLDOWN = 30;

        @ConfigEntry(old = {"titles-per-rank"})
        @ConfigComment("How many different titles can be configured for a rank")
        public static int TITLES_PER_RANK = 10;

        public static class Leveling {
            @ConfigComment("The amount of levels an Empire can reach")
            public static int MAX_LEVEL = 5;
        }

        public static class Missions {
            @ConfigComment("Whether or not empire missions should be given to empire member settlements")
            public static boolean ENABLED = true;

            @ConfigComment("Should newly joined settlements be granted missions instantly or do they need to wait until the upkeep?")
            public static boolean DISTRIBUTE_ON_JOIN = false;

            @ConfigComment("Should missions that are not started yet be removed and replaced by new missions on reset?")
            public static boolean RREMOVE_NOT_STARTED_MISSIONS = true;

            @ConfigComment("Should already started missions be removed and replaced by new missions on reset?")
            public static boolean REMOVE_STARTED_MISSIONS = false;

            @ConfigComment("Should finished missions be reset? If those are not reset then the settlement needs to wait for the empire to upgrade to get more!")
            public static boolean REMOVE_FINISHED_MISSIONS = true;

            @ConfigComment("Time in minutes for which block placements should be cached so that break mission abuse isn't possible using (recent) player placed blocks.")
            public static long PLACE_CACHE_TIME = 60;
        }

        public static class World {
            @ConfigEntry(old = {"empires-world.enabled"})
            @ConfigComment("Whether each empire should get their own world automatically generated")
            public static boolean ENABLED = true;

            @ConfigEntry(old = {"empires-world.prefix"}, reload = false)
            @ConfigComment("The prefix of all empire worlds. WARNING: Changing this with existing worlds will break them!")
            public static String PREFIX = "";

            @ConfigEntry(old = {"empires-world.suffix"}, reload = false)
            @ConfigComment("The suffix of all empire worlds. WARNING: Changing this with existing worlds will break them!")
            public static String SUFFIX = "_Domain";

            @ConfigEntry(reload = false)
            @ConfigComment("Whether or not worlds should be renamed on empire rename. Might cause issues with some plugins.")
            public static boolean RENAME = true;

            @ConfigEntry(old = {"empires-world.environment"})
            @ConfigComment("The environment of the world. Can be normal, nether or the_end")
            public static Environment ENVIRONMENT = Environment.NORMAL;

            @ConfigEntry(old = {"empires-world.type"})
            @ConfigComment("The type of the world. Can be normal, flat, large_biomes or amplified")
            public static WorldType TYPE = WorldType.NORMAL;

            @ConfigEntry(old = {"empires-world.generator"})
            @ConfigComment("The name of the generator to use for the empire worlds. Set to empty to not use one")
            public static String GENERATOR = "";

            @ConfigEntry(old = {"empires-world.generator-settings"})
            @ConfigComment("The settings for the defined generator")
            public static String GENERATOR_SETTINGS = "";

            @ConfigEntry(old = {"empires-world.seed"})
            @ConfigComment("The seed to be used for the generator. Set to empty to use a random one for each world.")
            public static String SEED = "";

            @ConfigEntry(old = {"empires-world.structures"})
            @ConfigComment("Whether or not to generate structures")
            public static boolean STRUCTURES = false;

            @ConfigEntry(old = {"empires-world.regen-duration"})
            @ConfigComment("Amount of seconds an empire can regenerate their world after empire generation to find one they like\n" +
                    "Set to -1 to allow infinite regeneration")
            public static int REGEN_DURATION = 3600;
        }

        public static class Upkeep {
            @ConfigEntry(old = {"abandon-time.empire"})
            @ConfigComment("The amount of minutes that an empire will stay abandoned after not being able to pay their upkeep\n" +
                    "Abandoned empires can be raided and fully accessed")
            public static long ABANDON_TIME = 1440;
        }
    }

    public static class Plot {
        @ConfigEntry(old = {"plot-max-tax"})
        @ConfigComment("Maximum value that a plot's tax can be set to. Set to -1 to disable maximum")
        public static long MAX_TAX = -1;

        @ConfigEntry(old = {"plot.name-length"})
        @ConfigComment("Maximum length that plot names can be (without formatting). Cannot be longer than 255.")
        public static int NAME_LENGTH = 25;

        @ConfigEntry(old = {"plot.description-length"})
        @ConfigComment("Maximum length that plot descriptions can be (without formatting).")
        public static int DESCRIPTION_LENGTH = 255;
    }

    @ConfigComment("Default modes of certain types or full worlds")
    public static class Modes {
        @ConfigComment("Modes in unclaimed land")
        public static class Unclaimed {}

        @ConfigComment("Modes in unclaimable worlds")
        public static class Unclaimeable {}

        @ConfigComment("Override modes in specific worlds")
        public static class Worlds {}

        @ConfigComment("Modes in abandoned settlements/empires")
        public static class Abandoned {
            @ConfigComment("Village-level Settlements")
            public static class Village {}

            @ConfigComment("Kingdom-level Settlements")
            public static class Kingdom {}

            @ConfigComment("Abandoned Empire worlds")
            public static class Empire {}
        }
    }

    public static class Lock {
        @ConfigEntry(reload = false)
        @ConfigComment("Enable or disable the inbuilt locking")
        public static boolean ENABLED = true;

        @ConfigEntry(old = "only-lock-in-settlement")
        @ConfigComment("Whether or not locks are only allowed in settlement claims and empire worlds")
        public static boolean ONLY_IN_CLAIM = true;

        @ConfigEntry(old = {"alternative-hopper-protection"})
        @ConfigComment(
                "This checks if a block is protected by checking on hopper place\n" +
                " rather than using the InventoryMoveItemEvent stealing of items"
        )
        public static boolean ALTERNATIVE_HOPPER_PROTECTION = true;

        @ConfigEntry(old = {"lock-load-queue-ticks"})
        @ConfigComment("Amount in ticks after which the chunk queue will be processed to load the locks in it")
        public static int LOAD_QUEUE_TICKS = 20;

        @ConfigEntry(old = {"lockable"}, parser = "MaterialSet")
        @ConfigComment("Blocks that can be locked")
        public static Set<Material> BLOCKS;

        private static Set<String> BLOCKS_DEFAULT = ImmutableSet.of(
                "chest",
                "trapped_chest",
                "dispenser",
                "dropper",
                "furnace",
                "*shulker_box",
                "*_door",
                "*_trapdoor",
                "*_fence_gate",
                "*_button",
                "lever",
                "note_block",
                "jukebox"
        );

        public static class Unclaim {
            @ConfigEntry(old = {"remove-locks-on-unclaim"})
            @ConfigComment("Remove all locks in a chunk when it gets unclaimed")
            public static boolean REMOVE = true;

            @ConfigEntry(old = {"remove-locked-blocks"})
            @ConfigComment("Also remove the blocks when the locks get removed on unclaim")
            public static boolean REMOVE_BLOCK = false;
        }
    }

    public static class Portal {
        @ConfigEntry(reload = false)
        @ConfigComment("Enable or disable the inbuilt portal handling which allows direct linking of a settlement's portals with each other")
        public static boolean ENABLED = true;

        @ConfigComment("Whether unlinked portals should teleport to the nether like normal")
        public static boolean ALLOW_NETHER_TELEPORT = false;

        @ConfigComment("Only allow portals to be created and used inside of claims.\n" +
                "Linking of portals will still be restricted to claimed land only!")
        public static boolean ONLY_INSIDE_CLAIMS = true;

        @ConfigComment("Whether portals in unclaimable worlds should just work like Vanilla nether portals\n" +
                "If set to true but only-inside-claims is true too then players will not be able to manually link to portals in unclaimable worlds\n" +
                "If set to false then players will not be able to create portals in unclaimable worlds if only-inside-claims is true.\n" +
                "If set to false and only-inside-claims is false then portals will just work like normal nether portals in unclaimable worlds")
        public static boolean IGNORE_UNCLAIMABLE_WORLDS = false;

    }

    public static class Teleport {
        @ConfigEntry(old = {"spawn-command"})
        @ConfigComment("The command to run to teleport the player to spawn via the GUI,\n" +
                "when a player is kicked from a settlement's claim or when an Empire world is made private/removed\n" +
                "If not set it will teleport to the world spawn of the default world")
        public static String SPAWN_COMMAND = "";

        @ConfigComment("The amount of time in seconds which a player needs to stand still before being teleported to an outpost/home\n" +
                "Set to 0 to instant teleport")
        public static class Delay {
            @ConfigEntry(old = {"teleport-delay.outpost"})
            public static int OUTPOST = 3;
            @ConfigEntry(old = {"teleport-delay.village"})
            public static int VILLAGE = 3;
            @ConfigEntry(old = {"teleport-delay.kingdom"})
            public static int KINGDOM = 3;
            @ConfigEntry(old = {"teleport-delay.empire"})
            public static int EMPIRE = 3;

            public static int PLOTSQUARED = 3;
        }

        @ConfigComment("Seconds to wait after allowing a teleport again if user has the kingdoms.bypass.teleport-delay permission\n" +
                "Set to 0 to disable")
        public static class Cooldown {
            @ConfigEntry(old = {"teleport-cooldown.outpost"})
            public static int OUTPOST = 3;
            @ConfigEntry(old = {"teleport-cooldown.village"})
            public static int VILLAGE = 3;
            @ConfigEntry(old = {"teleport-cooldown.kingdom"})
            public static int KINGDOM = 3;
            @ConfigEntry(old = {"teleport-cooldown.empire"})
            public static int EMPIRE = 3;
        }
    }

    public static class Chat {
        @ConfigEntry(reload = false)
        @ConfigComment("Enable or disable the inbuilt chat")
        public static boolean ENABLED = true;

        @ConfigEntry(reload = false)
        @ConfigComment("Should this plugin only handle chat and not provide any other function?\n" +
                "Useful for cross-server chat (requires ConnectorPlugin)")
        public static boolean CHAT_ONLY = false;

        @ConfigEntry(old = {"reset-channel-on-login"})
        @ConfigComment("Should all channels be set to the default config on player login or be the same as on logout")
        public static boolean RESET_CHANNELS_ON_LOGIN = true;

        @ConfigComment("The default chat channel colors")
        public static class ChannelColors {
            public static String GENERAL = "white";
            public static String EVENT = "green";
            public static String LOCAL = "yellow";
            public static String ADMIN = "dark_purple";
            public static String STAFF = "gold";
            public static String HELP = "white";
            public static String SETTLEMENT = "yellow";
            public static String TRADE = "light_purple";
            public static String ALLY = "aqua";
            public static String EMPIREALLY = "aqua";
            public static String EMPIRE = "aqua";
            public static String PRIVATE_SENT = "aqua";
            public static String PRIVATE_RECEIVED = "aqua";
        }


        @ConfigComment("The default channel prefix colors")
        public static class ChannelPrefixColors {
            public static String GENERAL = "black";
            public static String EVENT = "green";
            public static String LOCAL = "yellow";
            public static String ADMIN = "red";
            public static String STAFF = "blue";
            public static String HELP = "red";
            public static String SETTLEMENT = "aqua";
            public static String TRADE = "light_purple";
            public static String ALLY = "gold";
            public static String EMPIREALLY = "gold";
            public static String EMPIRE = "aqua";
            public static String PRIVATE_SENT = "dark_gray";
            public static String PRIVATE_RECEIVED = "dark_gray";
        }

        @ConfigComment("The colors that players can use for chat channels")
        public static List<String> SELECTABLE_COLORS = Arrays.asList(
                "blue",
                "dark_blue",
                "dark_green",
                "dark_aqua",
                "dark_red",
                "dark_purple",
                "gold",
                "gray",
                "dark_gray",
                "green",
                "aqua",
                "red",
                "light_purple",
                "yellow",
                "white"
        );

        public static class Notify {
            @ConfigEntry(old = {"chat.notify.sound"})
            @ConfigComment("The sound that is played when a player's name is mentioned in chat")
            public static Sound SOUND = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;

            @ConfigEntry(old = {"chat.notify.cooldown"})
            @ConfigComment("Seconds between notification sounds")
            public static int COOLDOWN = 15;
        }

        @ConfigComment("Toggle PlaceholderAPI placeholders in certain places")
        public static class PlaceholderApi {
            @ConfigEntry(old = {"placeholder-api.chat.hover"})
            @ConfigComment("Replace placeholders in the player name hovers")
            public static boolean HOVER = true;

            public static class Format {
                @ConfigEntry(old = {"placeholder-api.chat.format.channel"})
                @ConfigComment("Replace placeholders in the channel format")
                public static boolean CHANNEL = true;

                @ConfigEntry(old = {"placeholder-api.chat.format.private"})
                @ConfigComment("Replace placeholders in the private message format")
                public static boolean PRIVATE = true;
            }
        }
    }

    public static class Sidebar {
        @ConfigEntry(reload = false)
        @ConfigComment("Enable or disable the inbuilt sidebar")
        public static boolean ENABLED = true;

        @ConfigEntry(old = {"scoreboard.update"}, reload = false)
        @ConfigComment("Seconds between scoreboard updates")
        public static int UPDATE = 1;

        @ConfigEntry(old = {"scoreboard.animation-speed"}, reload = false)
        @ConfigComment("Ticks for the scoreboard header animation (set to 0 to disable it)")
        public static int HEADER_SPEED = 2;

        @ConfigEntry(old = {"placeholder-api.sidebar"})
        @ConfigComment("Whether PlaceholderAPI placeholders should be replaced in the sidebar")
        public static boolean PLACEHOLDER_API = true;
    }

    public static class Chestshop {
        @ConfigComment("Whether or not it is allowed for a settlement's/empire's shop to be sold to if the owner goes below their next upkeep")
        public static boolean ALLOW_SELLING_BELOW_UPKEEP = false;

        @ConfigEntry(old = {"chestshop.notification-cooldown"})
        @ConfigComment("The seconds between shop transaction notifications")
        public static int NOTIFICATION_COOLDOWN = 5;
    }

    public static class Brewery {
        @ConfigComment("Players with a drunkeness above that value can no longer write in the channel")
        public static class BlockChannels {
            public static int LOCAL = 9001;
            public static int TRADE = 1;
            public static int EVENT = 1;
            public static int GENERAL = 1;
            public static int SETTLEMENT = 9001;
            public static int EMPIRE = 1;
            public static int ALLY = 1;
            public static int EMPIREALLY = 1;
            public static int ADMIN = 9001;
            public static int STAFF = 9001;
        }
    }

    public static class PlotSquared {
        @ConfigComment("The world which the area for purchased plots is in.\n" +
                "Leave empty to use the first available area globally. (E.g. if you only have one)")
        public static String PLOT_WORLD = "";

        @ConfigComment("The id of the plot area which purchased plots are added to.\n" +
                "Leave empty to use the first available one in the world/globally")
        public static String PLOT_AREA_ID = "";
    }
}
