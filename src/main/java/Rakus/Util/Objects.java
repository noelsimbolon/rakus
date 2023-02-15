package Rakus.Util;

import Enums.ObjectTypes;
import Models.GameObject;
import Rakus.Vars;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static Rakus.Vars.botService;

public class Objects {
    // Returns a game object with the given UUID, or null if such game object doesn't exist
    public static GameObject findWithUUID(UUID id) {
        var gameState = botService.getGameState();

        var res = gameState.getPlayerGameObjects().stream().filter(obj -> id.equals(obj.getId())).findAny();
        if (res.isEmpty())
            res = gameState.getGameObjects().stream().filter(obj -> id.equals(obj.getId())).findAny();

        if (res.isEmpty())
            return null;
        return res.get();
    }

    // Returns the instance of a stored object in the current game state (with the same UUID but updated state)
    public static GameObject findSelf(GameObject object) {
        if (object == null) return null;

        var optional = botService.getGameState().getGameObjects().stream().filter(obj -> obj.getId().equals(object.getId())).findAny();
        if (optional.isEmpty())
            return null;
        else
            return optional.get();
    }

    // Returns a list containing all game objects satisfying a given predicate, sorted ascending by distance to a game object
    public static List<GameObject> findAll(GameObject object, Predicate<GameObject> pred) {
        return findAll(item -> distanceBetween(object, item), pred);
    }

    // Returns a list containing all game objects satisfying a given predicate, sorted ascending by a comparison function
    public static <T extends Comparable<? super T>> List<GameObject> findAll(Function<GameObject, ? extends T> comparator, Predicate<GameObject> pred) {
        var gameState = botService.getGameState();
        if (gameState.getGameObjects() == null) return null;

        return gameState.getGameObjects()
                .stream().filter(pred)
                .sorted(Comparator.comparing(comparator))
                .collect(Collectors.toList());
    }

    // Returns a list containing all game objects satisfying a given predicate within a distance from this bot
    public static List<GameObject> findWithin(GameObject object, Predicate<GameObject> pred, double radius) {
        return findAll(object, item -> pred.test(item) && isWithin(botService.getBot(), item, radius));
    }

    // Returns the nearest (by distance) game object from a game object satisfying a given predicate
    public static GameObject findClosest(GameObject object, Predicate<GameObject> pred) {
        return findClosest(item -> distanceBetween(botService.getBot(), item), pred);
    }

    // Returns the smallest (by a comparison function) game object satisfying a given predicate
    public static <T extends Comparable<? super T>> GameObject findClosest(Function<GameObject, ? extends T> comparator, Predicate<GameObject> pred) {
        var gameState = botService.getGameState();
        if (gameState.getGameObjects() == null) return null;

        var list = findAll(comparator, pred);
        return list == null || list.isEmpty() ? null : list.get(0);
    }

    // Returns a list containing all players sorted ascending by distance to a game object
    public static List<GameObject> findPlayers(GameObject object) {
        return findPlayers(object, item -> true);
    }

    // Returns a list containing all players satisfying a given predicate, sorted ascending by distance to a game object
    public static List<GameObject> findPlayers(GameObject object, Predicate<GameObject> pred) {
        return findPlayers(player -> distanceBetween(object, player), pred);
    }

    // Returns a list containing all players satisfying a given predicate, sorted ascending by a comparison function
    public static <T extends Comparable<? super T>> List<GameObject> findPlayers(Function<GameObject, ? extends T> comparator, Predicate<GameObject> pred) {
        var gameState = botService.getGameState();
        if (gameState.getPlayerGameObjects() == null) return null;

        return gameState.getPlayerGameObjects()
            .stream().filter(pred)
            .sorted(Comparator.comparing(comparator))
            .collect(Collectors.toList());
    }

    // Returns a list containing all players satisfying a given predicate within a distance from a game object
    public static List<GameObject> findPlayersWithin(GameObject object, Predicate<GameObject> pred, double radius) {
        return findPlayers(object, player -> pred.test(player) && isWithin(object, player, radius));
    }

    // Returns the Euclidean distance between two game objects
    public static double distanceBetween(GameObject object1, GameObject object2) {
        var triangleX = Math.abs(object1.getPosition().x - object2.getPosition().x);
        var triangleY = Math.abs(object1.getPosition().y - object2.getPosition().y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    // Returns the Euclidean distance between a game object and world origin (0,0)
    public static double distanceFromOrigin(GameObject object) {
        var orig = botService.getGameState().getWorld().getCenterPoint();
        var triangleX = Math.abs(object.getPosition().x - orig.x);
        var triangleY = Math.abs(object.getPosition().y - orig.y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    // Returns the angle of an object from the world origin
    public static int headingFromOrigin(GameObject object) {
        var direction = toDegrees(Math.atan2(object.getPosition().y, object.getPosition().x));
        return (direction + 360) % 360;
    }

    // Returns an angle (in degrees) specifying direction from object1 to object2
    public static int headingBetween(GameObject object1, GameObject object2) {
        var direction = toDegrees(Math.atan2(object2.getPosition().y - object1.getPosition().y,
                object2.getPosition().x - object1.getPosition().x));
        return (direction + 360) % 360;
    }

    // Returns a heading that is the reverse of a given heading
    public static int headingReverse(int heading) {
        return (heading + 180) % 360;
    }

    // Returns angle difference between two headings
    public static int headingDiff(int heading1, int heading2) {
        int d = Math.abs(heading1 - heading2);
        return d < 180 ? d : (heading1 < heading2 ? Math.abs(heading1 - heading2 + 360) : Math.abs(heading1 - heading2 - 360));
    }

    // Returns the median between two headings
    public static int headingMedian(int heading1, int heading2) {
        // Force heading1 <= heading2
        if (heading1 > heading2) return headingMedian(heading2, heading1);
        // There are two angles that is equidistant to either heading, choose one with the least distance
        int m = (heading1 + heading2) / 2;
        if (headingDiff(heading1, m) > 90)
            m += 180;
        return m % 360;
    }

    // Returns priority penalty (if negative, bonus) against a game object from the perspective of a bot
    public static int priorityPenalty(GameObject object, GameObject bot) {
        int total = 0;
        if (!findWithin(object, obj -> obj.getGameObjectType() == ObjectTypes.GAS_CLOUD, bot.getSize() * Vars.GAS_CLOUD_AVOIDANCE).isEmpty())
            total += Vars.GAS_CLOUD_PENALTY;
        if (!findWithin(object, obj -> obj.getGameObjectType() == ObjectTypes.ASTEROID_FIELD, bot.getSize() * Vars.ASTEROID_FIELD_AVOIDANCE).isEmpty())
            total += Vars.ASTEROID_FIELD_PENALTY;
        if (!safeFromEdge(object))
            total += Vars.EDGE_PENALTY;

        return total;
    }

    // Returns whether an object is classified as food (includes superfood)
    public static boolean isFood(GameObject object) {
        return object != null && (object.getGameObjectType() == ObjectTypes.FOOD || object.getGameObjectType() == ObjectTypes.SUPERFOOD);
    }

    // Returns whether an object is a player
    public static boolean isPlayer(GameObject object) {
        return object != null && object.getGameObjectType() == ObjectTypes.PLAYER;
    }

    // Returns whether an object is an enemy player to the current bot
    public static boolean isEnemyPlayer(GameObject object) {
        return isPlayer(object) && !equals(botService.getBot(), object);
    }

    // Returns whether two objects are equal (by UUID)
    public static boolean equals(GameObject a, GameObject b) {
        return a != null && b != null && a.getId().equals(b.getId());
    }

    // Returns whether two objects are within a distance from each other
    public static boolean isWithin(GameObject a, GameObject b, double radius) {
        return a != null && b != null && distanceBetween(a, b) <= radius;
    }

    // Returns whether an object can be safely reached without touching the world edge
    public static boolean safeFromEdge(GameObject object) {
        return botService.getGameState().getWorld().getRadius() - distanceFromOrigin(object) > Vars.EDGE_AVOIDANCE * botService.getBot().getSize();
    }

    // Takes an angle in radians as argument and returns an equivalent angle in degrees
    private static int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }
}
