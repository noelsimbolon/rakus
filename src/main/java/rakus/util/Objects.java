package rakus.util;

import Enums.*;
import Models.*;
import Services.*;
import rakus.Vars;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class Objects{
    public static List<GameObject> findAll(BotService botService, Predicate<GameObject> pred){
        return findAll(botService, pred, item -> distanceBetween(botService.getBot(), item));
    }

    public static <T extends Comparable<? super T>> List<GameObject> findAll(BotService botService, Predicate<GameObject> pred, Function<GameObject, ? extends T> comparator){
        var gameState = botService.getGameState();
        if(gameState.getGameObjects() == null)return null;

        return gameState.getGameObjects()
            .stream().filter(pred)
            .sorted(Comparator.comparing(comparator))
            .collect(Collectors.toList());
    }

    public static List<GameObject> findWithin(BotService botService, Predicate<GameObject> pred, double radius){
        return findAll(botService, item -> pred.test(item) && isWithin(botService.getBot(), item, radius));
    }

    public static GameObject findClosest(BotService botService, Predicate<GameObject> pred){
        return findClosest(botService, pred, item -> distanceBetween(botService.getBot(), item));
    }

    public static <T extends Comparable<? super T>> GameObject findClosest(BotService botService, Predicate<GameObject> pred, Function<GameObject, ? extends T> comparator){
        var gameState = botService.getGameState();
        if(gameState.getGameObjects() == null)return null;

        var list = findAll(botService, pred, comparator);
        return list == null || list.isEmpty() ? null : list.get(0);
    }

    public static List<GameObject> findPlayers(BotService botService){
        return findPlayers(botService, item -> true);
    }

    public static List<GameObject> findPlayers(BotService botService, Predicate<GameObject> pred){
        var gameState = botService.getGameState();
        if(gameState.getPlayerGameObjects() == null)return null;

        return gameState.getPlayerGameObjects()
            .stream().filter(pred)
            .sorted(Comparator.comparing(player -> distanceBetween(botService.getBot(), player)))
            .collect(Collectors.toList());
    }

    public static List<GameObject> findPlayersWithin(BotService botService, Predicate<GameObject> pred, double radius){
        return findPlayers(botService, player -> pred.test(player) && isWithin(botService.getBot(), player, radius));
    }

    public static double distanceBetween(GameObject object1, GameObject object2) {
        var triangleX = Math.abs(object1.getPosition().x - object2.getPosition().x);
        var triangleY = Math.abs(object1.getPosition().y - object2.getPosition().y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    public static double distanceFromOrigin(GameObject object){
        var triangleX = Math.abs(object.getPosition().x);
        var triangleY = Math.abs(object.getPosition().y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    public static int headingBetween(GameObject object1, GameObject object2) {
        var direction = toDegrees(Math.atan2(object2.getPosition().y - object1.getPosition().y,
            object2.getPosition().x - object1.getPosition().x));
        return (direction + 360) % 360;
    }

    public static boolean isFood(GameObject object){
        return object != null && (object.getGameObjectType() == ObjectTypes.FOOD || object.getGameObjectType() == ObjectTypes.SUPERFOOD);
    }

    public static boolean isPlayer(GameObject object){
        return object != null && object.getGameObjectType() == ObjectTypes.PLAYER;
    }

    public static boolean isEnemyPlayer(BotService botService, GameObject object){
        return isPlayer(object) && !equals(botService.getBot(), object);
    }

    public static boolean equals(GameObject a, GameObject b){
        return a != null && b != null && a.getId().equals(b.getId());
    }

    public static boolean isWithin(GameObject a, GameObject b, double radius){
        return a != null && b != null && distanceBetween(a, b) <= radius;
    }

    public static boolean safeFromEdge(BotService botService, GameObject object){
        return botService.getGameState().getWorld().getRadius() - distanceFromOrigin(object) < botService.getBot().getSize() + Vars.EDGE_AVOIDANCE;
    }

    private static int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }
}
