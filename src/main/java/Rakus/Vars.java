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
    // Radius for opponent detection for CHASE_LOW
    public static final double CHASE_LOW_SEARCH_RADIUS = 1000;
    // World edge avoidance radius; nonessential objects within this range from world edge should be ignored
    public static final int EDGE_AVOIDANCE = 100;
    /* ------------------------------------------------------------------------------------------------------- */
    // GLOBAL OBJECTS
    // Current bot service instance
    public static BotService botService;
}
