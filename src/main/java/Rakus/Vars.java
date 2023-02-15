package Rakus;

import Services.BotService;

public class Vars {
    /* ------------------------------------------------------------------------------------------------------- */
    // MODIFIER CONSTANTS
    // Final score multiplier for state evaluation
    public static final float
            FEED_SCOREMULT = 1f,
            FLEE_LOW_SCOREMULT = 1.2f,
            CHASE_LOW_SCOREMULT = 1.1f;

    // Radius for opponent detection for FLEE_LOW
    public static final double FLEE_LOW_SEARCH_RADIUS = 600;
    // Radius for torpedo clearance for FLEE_LOW
    public static final double FLEE_LOW_TORPEDO_RANGE = 400;

    // Radius for opponent detection for CHASE_LOW
    public static final double CHASE_LOW_SEARCH_RADIUS = 1000;
    // Radius for torpedo clearance for CHASE_LOW
    public static final double CHASE_LOW_TORPEDO_RANGE = 500;

    // Penalty values; game object scores are offset by this if near the corresponding game objects
    public static final int
        GAS_CLOUD_PENALTY = 100,
        ASTEROID_FIELD_PENALTY = 30;
    // World edge avoidance scale; objects within bot size times this value to the world edge will be ignored
    public static final double EDGE_AVOIDANCE = 1.5;

    // DEPRECATED: Torpedo buffer tick duration; torpedo fire commands will be sent over this amount of ticks
    // public static final int TORPEDO_WARMUP_TICK = 0;
    // Torpedo cooldown duration; the bot will wait this amount of ticks between torpedo fires
    public static final int TORPEDO_COOLDOWN_TICK = 0;

    /* ------------------------------------------------------------------------------------------------------- */
    // GLOBAL OBJECTS AND CONSTANTS
    // Current bot service instance
    public static BotService botService;

    // Player object attribute count
    public static final int PLAYER_ATTRIB_COUNT = 11;
}
