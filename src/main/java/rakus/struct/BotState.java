package rakus.struct;

import Models.GameObject;
import rakus.func.*;
import rakus.util.*;
import rakus.*;

import Enums.*;
import Services.*;

public enum BotState{
    IDLE(botService -> {
        return 0;
    }, (botService, action) -> {
        action.action = PlayerActions.STOP;
        return action;
    }),

    FEED(botService -> {
        var bot = botService.getBot();
        var gameState = botService.getGameState();
        var world = gameState.getWorld();

        if(!gameState.getGameObjects().isEmpty()){
            GameObject target = botService.getCurrentTarget();
            // Search new food target if transitioning in from another state or current food is already eaten
            GameObject finalTarget = target;
            if(!Objects.isFood(target) || gameState.getGameObjects().stream().noneMatch(item -> Objects.equals(item, finalTarget))){
                target = Objects.findClosest(botService, item -> Objects.isFood(item) && Objects.safeFromEdge(botService, item));
            }

            // Score is distance to target from the bot times a constant
            if(target != null){
                int distance = (int)Objects.distanceBetween(bot, target);
                return (int)(Vars.FEED_SCOREMULT * (world.radius - distance));
            }else{
                return Integer.MIN_VALUE;
            }
        }
        return Integer.MIN_VALUE;
    }, (botService, action) -> {
        var bot = botService.getBot();
        var gameState = botService.getGameState();
        var world = gameState.getWorld();

        if(!gameState.getGameObjects().isEmpty()){
            GameObject target = botService.getCurrentTarget();
            // Search new food target if transitioning in from another state or current food is already eaten
            GameObject finalTarget = target;
            if(!Objects.isFood(target) || gameState.getGameObjects().stream().noneMatch(item -> Objects.equals(item, finalTarget))){
                target = Objects.findClosest(botService, Objects::isFood);
            }

            // Move towards current target
            if(target != null){
                botService.setCurrentTarget(target);
                action.action = PlayerActions.FORWARD;
                action.heading = Objects.headingBetween(bot, target);
            }
        }
        return action;
    }),

    FLEE_LOW(botService -> {
        var bot = botService.getBot();
        var gameState = botService.getGameState();
        var world = gameState.getWorld();

        if(!gameState.getGameObjects().isEmpty()){
            // Score is distance to nearest other player within a radius times a constant
            var players = Objects.findPlayersWithin(botService, player -> Objects.isEnemyPlayer(botService, player) && bot.getSize() <= player.getSize(), Vars.FLEE_LOW_RADIUS);
            if(players != null && !players.isEmpty()){
                int distance = (int)Objects.distanceBetween(bot, players.get(0));
                return (int)(Vars.FLEE_LOW_SCOREMULT * (world.radius - distance));
            }
        }
        return Integer.MIN_VALUE;
    }, (botService, action) -> {
        var bot = botService.getBot();
        var gameState = botService.getGameState();
        var world = gameState.getWorld();

        // Reset target
        botService.setCurrentTarget(null);

        if(!gameState.getGameObjects().isEmpty()){
            // Choose a direction with food away from nearest player as flee direction
            var players = Objects.findPlayers(botService, player -> Objects.isEnemyPlayer(botService, player) && bot.getSize() <= player.getSize());
            if(players != null && !players.isEmpty()){
                int directHeading = Objects.headingBetween(players.get(0), bot);
                var food = Objects.findClosest(botService,
                    item -> Objects.isFood(item) && Objects.safeFromEdge(botService, item),
                    item -> Math.abs(Objects.headingBetween(bot, item) - directHeading));

                // Move towards food, or away from player if no food is found
                action.action = PlayerActions.FORWARD;
                action.heading = food == null ? directHeading : Objects.headingBetween(bot, food);
            }
        }
        return action;
    });

    public final GameEvaluator eval;
    public final ActionFunc func;

    private BotState(GameEvaluator evaluator, ActionFunc actionFunc){
        eval = evaluator;
        func = actionFunc;
    }

    // Return state with maximum evaluation score
    public static BotState getNextState(BotService botService){
        var str = new StringBuilder();
        str.append(String.format("Tick %d, evaluation score:\n", botService.getGameState().getWorld().getCurrentTick()));

        var values = BotState.values();
        BotState current = values[0];
        int currentEval = current.eval.get(botService);
        str.append(String.format("  %s: %d", current.name(), currentEval));

        for(int i = 1; i < values.length; i++){
            int newEval = values[i].eval.get(botService);
            if(newEval > currentEval){
                current = values[i];
                currentEval = newEval;
            }
            str.append(String.format("\n  %s: %d", values[i].name(), newEval));
        }
        str.append(String.format("\n  Selected: %s", current.name()));

        System.out.println(str.toString());
        return current;
    }
}
