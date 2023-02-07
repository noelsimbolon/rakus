package rakus;

public class Vars{
    // Final score multiplier for state evaluation
    public static final float
        FEED_SCOREMULT = 1f,
        FLEE_LOW_SCOREMULT = 1.2f;

    // Radius for hostile player detection for FLEE_LOW
    public static final double FLEE_LOW_RADIUS = 600;
    // World edge avoidance radius; nonessential objects within this range from world edge should be ignored
    public static final int EDGE_AVOIDANCE = 100;
}
