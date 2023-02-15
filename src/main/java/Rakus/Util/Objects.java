package Rakus.Util;

import Enums.ObjectTypes;
import Models.GameObject;
import Rakus.Vars;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static Rakus.Vars.botService;

public class Objects {
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
        var gameState = botService.getGameState();
        if (gameState.getPlayerGameObjects() == null) return null;

        return gameState.getPlayerGameObjects()
                .stream().filter(pred)
                .sorted(Comparator.comparing(player -> distanceBetween(object, player)))
                .collect(Collectors.toList());
    }

    // Returns a list containing all players satisfying a given predicate within a distance from a game object
    public static List<GameObject> findPlayersWithin(GameObject object, Predicate<GameObject> pred, double radius) {
        return findPlayers(object, player -> pred.test(player) && isWithin(botService.getBot(), player, radius));
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
