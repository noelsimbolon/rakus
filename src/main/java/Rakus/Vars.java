package Rakus;

import Services.BotService;

public class Vars {
    /* ------------------------------------------------------------------------------------------------------- */
    // MODIFIER CONSTANTS
    // Final score multiplier for state evaluation
    public static final float
        FEED_SCOREMULT = 0.875f,
        FLEE_LOW_SCOREMULT = 1.2f,
        CHASE_LOW_SCOREMULT = 1.1f,
        CHASE_HIGH_SCOREMULT = 1f,
        PICK_SUPERNOVA_SCOREMULT = 2f;

    // Modifiers for FLEE_LOW
    public static final double
        FLEE_LOW_SEARCH_RADIUS = 600,
        FLEE_LOW_TORPEDO_RANGE = 500;

    // Modifiers for CHASE_LOW
    public static final double
        CHASE_LOW_SEARCH_RADIUS = 1000,
        CHASE_LOW_TORPEDO_RANGE = 600;

    // Modifiers for CHASE_HIGH
    public static final int
        CHASE_HIGH_SIZE_DIFF = 30,
        CHASE_HIGH_TELEPORT_BONUS = 200;
    public static final double
        CHASE_HIGH_SEARCH_RADIUS = 1000,
        CHASE_HIGH_TORPEDO_RANGE = 600,
        CHASE_HIGH_TELEPORT_RANGE = 800;

    // Modifiers for PICK_SUPERNOVA
    public static final double
        // PICK_SUPERNOVA_MIN_SIZE = 30,
        PICK_SUPERNOVA_WEIGHT_BIAS = 16,
        PICK_SUPERNOVA_TELEPORT_RADIUS = 20,
        PICK_SUPERNOVA_TELEPORT_SAFETY_RADIUS = 30,
        PICK_SUPERNOVA_TOXIC_RADIUS = 200;

    // Modifiers for ANY
    public static final double
        ANY_GASCLOUD_SEARCH_RADIUS = 120,
        ANY_EDGE_AVOID_RADIUS = 60,
        ANY_SHIELD_SEARCH_RADIUS = 90,
        ANY_INTERCEPT_SEARCH_RADIUS = 60;
    public static final int
        ANY_SHIELD_HEADING_DIFF = 135,
        ANY_INTERCEPT_HEADING_DIFF = 135;

    // Other modifiers
    public static final double
        CHASE_TELEPORT_CLEARANCE = 1.5;

    // Penalty values; game object scores are offset by this if near the corresponding game objects
    public static final int
        GAS_CLOUD_PENALTY = 100,
        ASTEROID_FIELD_PENALTY = 30;
    // Object avoidance scale; objects within bot size times this value to the corresponding objects will be prioritized less
    public static final double
        GAS_CLOUD_AVOIDANCE = 0.5,
        ASTEROID_FIELD_AVOIDANCE = 0.33;
    // World edge avoidance scale; objects within bot size times this value to the world edge will be mostly ignored
    public static final double EDGE_AVOIDANCE = 1.5;
    public static final int EDGE_PENALTY = 1000;

    // Ability cooldown values
    public static final int
        TORPEDO_COOLDOWN_TICK = 2,
        TELEPORTER_COOLDOWN_TICK = 10;

    // Object minimum size for actions
    public static final int
        TORPEDO_SAFE_SIZE = 15,
        TELEPORTER_SAFE_SIZE = 40,
        SHIELD_SAFE_SIZE = 40;
    // Bot constants
    public static final int
        OBJECT_SEARCH_TIME = 30;

    /* ------------------------------------------------------------------------------------------------------- */
    // GLOBAL OBJECTS
    // Current bot service instance
    public static BotService botService;

    // Player object attribute count
    public static final int PLAYER_ATTRIB_COUNT = 11;
}
