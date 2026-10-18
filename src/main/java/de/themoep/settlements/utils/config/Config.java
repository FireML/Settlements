package de.themoep.settlements.utils.config;

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
    @ConfigComment("How many players should show up in settlement member lists")
    public static int PLAYERS_IN_INFO = 40;

    @ConfigComment("Max lengths of the rules")
    public static int MAX_RULES_LENGTH = 10000;

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
        public static String DATABASE = "settlements";
        @ConfigEntry(reload = false)
        public static String TABLE_PREFIX = "settlements_";
        @ConfigEntry(reload = false)
        public static String URL_PARAMETERS = "";
        @ConfigComment("MySQL connection timeout in ms")
        public static long CONNECTION_TIMEOUT = 30000;
        @ConfigComment("Should all data be saved again on shutdown? Normally modifications \n" +
                "are saved directly, this just makes sure it's all saved!")
        public static boolean SAVE_ON_SHUTDOWN = true;

        public static class Queue {
            @ConfigComment("Max size of the queue before saving happens")
            public static int SIZE = 100;

            @ConfigComment("Time to wait in seconds before saving queued entries")
            public static int WAIT = 2;

            @ConfigComment("Queue settings for mission data")
            public static class Missions {
                @ConfigComment("Max size of the queue before saving happens")
                public static int SIZE = 100;

                @ConfigComment("Time to wait in seconds before saving queued entries")
                public static int WAIT = 30;
            }
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
                public static int GRACE_DAYS = 1;

                @ConfigEntry(old = {"close-claims-unclaims-outposts"})
                @ConfigComment("Whether or not outposts are unclaimed")
                public static boolean OUTPOSTS = false;
            }

            public static class Building {
                @ConfigEntry(old = {"close-building-notification-distance", "settlement.close.building-notification.distance"}, reload = false)
                @ConfigComment("""
                        The distance in chunks to other settlements in which to allow building
                        Outside of this distance building will not be allowed.
                        If a notification message is set in the messages.yml at 'close-claims'then that will be sent to the player or members of the settlement.
                        Set to 0 to disable""")
                public static int DISTANCE = 4;

                @ConfigComment("Whether or not to block building outside the range.")
                public static boolean BLOCK_OUTSIDE = true;

                @ConfigComment("Whether or not to block building inside the range.")
                public static boolean BLOCK_INSIDE = false;

                @ConfigEntry(parser = "MaterialSet")
                @ConfigComment("Which blocks are allowed to get built anyways.")
                public static Set<Material> ALLOWED_BLOCKS;

                private static final Set<String> ALLOWED_BLOCKS_DEFAULT = ImmutableSet.of(
                        "*shulker_box"
                );

                @ConfigEntry(parser = "StringSet")
                @ConfigComment("Which worlds should this check be ignored in?")
                public static Set<String> ALLOWED_WORLDS = new HashSet<>();
            }
        }
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

        @ConfigComment("Maximum value that a settlement's auction tax can be set to. Set to -1 to disable maximum")
        public static double MAX_AUCTION_TAX = 30;

        @ConfigEntry(old = {"titles-per-rank"})
        @ConfigComment("How many different titles can be configured for a rank")
        public static int TITLES_PER_RANK = 10;

        @ConfigComment("The maximum length of the settlement names")
        public static int NAME_LENGTH = 20;

        @ConfigComment("What regex settlement names need to match")
        public static String NAME_VALIDATION = "[\\w-]+";

        public static class Leveling {
            @ConfigComment("The amount of levels until a settlement converts from a Village to a Kingdom")
            public static int VILLAGE_MAX_LEVEL = 5;

            @ConfigComment("The maximum amount of Kingdom levels")
            public static int KINGDOM_MAX_LEVEL = 15;
        }

        public static class Missions {
            @ConfigComment("Whether or not settlement missions should be given to residents")
            public static boolean ENABLED = true;

            @ConfigComment("Should newly joined resident be granted missions instantly or do they need to wait until the upkeep?")
            public static boolean DISTRIBUTE_ON_JOIN = true;

            @ConfigComment("Should additional missions be distributed when leveling up or do they need to wait until the upkeep?")
            public static boolean DISTRIBUTE_ON_LEVELUP = true;

            @ConfigComment("Allow distribution of duplicate missions?")
            public static boolean DUPLICATE_MISSIONS = false;

            @ConfigComment("Should missions that are not started yet be removed and replaced by new missions on reset?")
            public static boolean REMOVE_NOT_STARTED_MISSIONS = true;

            @ConfigComment("Should already started missions be removed and replaced by new missions on reset?")
            public static boolean REMOVE_STARTED_MISSIONS = false;

            @ConfigComment("Should finished missions be reset? If those are not reset then the resident needs to wait for the settlement to upgrade to get more!")
            public static boolean REMOVE_FINISHED_MISSIONS = true;

            @ConfigComment("Amount to pay from a settlement's bank to distribute new missions. (Set to -1 to disable this functionality)")
            public static long REDISTRIBUTE_PRICE = 1000;

            @ConfigComment("Time in minutes for which block placements should be cached so that break mission abuse isn't possible using (recent) player placed blocks.")
            public static long PLACE_CACHE_TIME = 60;
        }

        public static class Claiming {
            @ConfigEntry(old = {"disabled-claiming-worlds"}, parser = "StringSet")
            @ConfigComment("Worlds in which claiming of Settlement chunks are disabled")
            public static Set<String> DISABLED_WORLDS = new HashSet<>();
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
        public static class Unclaimable {}

        @ConfigComment("Override modes in specific worlds")
        public static class Worlds {}

        @ConfigComment("Modes in abandoned settlements")
        public static class Abandoned {
            @ConfigComment("Village-level Settlements")
            public static class Village {}

            @ConfigComment("Kingdom-level Settlements")
            public static class Kingdom {}
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

        @ConfigComment("""
                Whether portals in unclaimable worlds should just work like Vanilla nether portals
                If set to true but only-inside-claims is true too then players will not be able to manually link to portals in unclaimable worlds
                If set to false then players will not be able to create portals in unclaimable worlds if only-inside-claims is true.
                If set to false and only-inside-claims is false then portals will just work like normal nether portals in unclaimable worlds""")
        public static boolean IGNORE_UNCLAIMABLE_WORLDS = false;

    }

    public static class Teleport {
        @ConfigEntry(old = {"spawn-command"})
        @ConfigComment("""
                The command to run to teleport the player to spawn via the GUI
                or when a player is kicked from a settlement's claim
                If not set it will teleport to the world spawn of the default world""")
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

            public static int PLOTSQUARED = 3;
        }

        @ConfigComment("Seconds to wait after allowing a teleport again if user has the settlements.bypass.teleport-delay permission\n" +
                "Set to 0 to disable")
        public static class Cooldown {
            @ConfigEntry(old = {"teleport-cooldown.outpost"})
            public static int OUTPOST = 3;
            @ConfigEntry(old = {"teleport-cooldown.village"})
            public static int VILLAGE = 3;
            @ConfigEntry(old = {"teleport-cooldown.kingdom"})
            public static int KINGDOM = 3;
        }
    }

}
