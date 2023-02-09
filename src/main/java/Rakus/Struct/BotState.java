package Rakus.Struct;

import Enums.PlayerActions;
import Models.GameObject;
import Rakus.Func.ActionFunc;
import Rakus.Func.GameEvaluator;
import Rakus.Util.Objects;
import Rakus.Vars;

import static Rakus.Vars.botService;

public enum BotState {
    /* ------------------------------------------------------------------------------------------------------- */
    // IDLE - Acts as a fallback option when other states are inapplicable
    IDLE(() -> {
        // PRIORITY: Always return MIN_VALUE + 1
        return Integer.MIN_VALUE + 1;

    }, action -> {
        // ACTION: Freeze in place and wait until a new state is chosen
        action.action = PlayerActions.STOP;
        return action;
    }),

    /* ------------------------------------------------------------------------------------------------------- */
    // FEED - Relatively low priority feeding action in relative safety
    FEED(() -> {
        // PRIORITY: Prioritize feeding if bot is small and there are food objects nearby
        var bot = botService.getBot();
        var gameState = botService.getGameState();
        var world = gameState.getWorld();

        if (!gameState.getGameObjects().isEmpty()) {
            GameObject target = botService.getCurrentTarget();
            // Search new food target if transitioning in from another state or current food is already eaten
            GameObject finalTarget = target;
            if (!Objects.isFood(target) || gameState.getGameObjects().stream().noneMatch(item -> Objects.equals(item, finalTarget))) {
                target = Objects.findClosest(bot, item -> Objects.isFood(item) && Objects.safeFromEdge(item));
            }

            // Score is proportional to minus distance to the target and current bot size
            if (target != null) {
                int distance = (int) Objects.distanceBetween(bot, target);
                return (int) (Vars.FEED_SCOREMULT * (2 * world.radius - distance - bot.getSize()));
            }
        }
        return Integer.MIN_VALUE;

    }, action -> {
        // ACTION: Move towards the currently targeted food object, or find the nearest one if target is not present
        var bot = botService.getBot();
        var gameState = botService.getGameState();
        var world = gameState.getWorld();

        if (!gameState.getGameObjects().isEmpty()) {
            GameObject target = botService.getCurrentTarget();
            // Search new food target if transitioning in from another state or current food is already eaten
            GameObject finalTarget = target;
            if (!Objects.isFood(target) || gameState.getGameObjects().stream().noneMatch(item -> Objects.equals(item, finalTarget))) {
                target = Objects.findClosest(bot, Objects::isFood);
            }

            // Move towards current target
            if (target != null) {
                botService.setCurrentTarget(target);
                action.action = PlayerActions.FORWARD;
                action.heading = Objects.headingBetween(bot, target);
            }
        }
        return action;
    }),

    /* ------------------------------------------------------------------------------------------------------- */
    // FLEE_LOW - Flee from the nearest opponent, grabbing food along the way
    FLEE_LOW(() -> {
        // PRIORITY: Proportional to minus distance to the nearest larger opponent within FLEE_LOW_SEARCH_RADIUS, or MIN_VALUE if no enemies are found
        var bot = botService.getBot();
        var gameState = botService.getGameState();
        var world = gameState.getWorld();

        if (!gameState.getPlayerGameObjects().isEmpty()) {
            // Score is distance to nearest other player within a radius times a constant
            var players = Objects.findPlayersWithin(bot, player -> Objects.isEnemyPlayer(player) && bot.getSize() <= player.getSize(), Vars.FLEE_LOW_SEARCH_RADIUS);
            if (players != null && !players.isEmpty()) {
                int distance = (int) Objects.distanceBetween(bot, players.get(0));
                return (int) (Vars.FLEE_LOW_SCOREMULT * (2 * world.radius - distance));
            }
        }
        return Integer.MIN_VALUE;

    }, action -> {
        // ACTION: Choose a direction heading away from the opponent, preferably towards a food object
        var bot = botService.getBot();
        var gameState = botService.getGameState();
        var world = gameState.getWorld();

        // Reset target
        botService.setCurrentTarget(null);

        if (!gameState.getPlayerGameObjects().isEmpty()) {
            // Choose a direction with food away from nearest player as flee direction
            var players = Objects.findPlayers(bot, player -> Objects.isEnemyPlayer(player) && bot.getSize() <= player.getSize());
            if (players != null && !players.isEmpty()) {
                int directHeading = Objects.headingBetween(players.get(0), bot);
                var food = Objects.findClosest(
                        item -> Math.abs(Objects.headingBetween(bot, item) - directHeading),
                        item -> Objects.isFood(item) && Objects.safeFromEdge(item)
                );

                // Move towards food, or away from player if no food is found
                action.action = PlayerActions.FORWARD;
                action.heading = food == null ? directHeading : Objects.headingBetween(bot, food);
            }
        }
        return action;
    }),

    /* ------------------------------------------------------------------------------------------------------- */
    // CHASE_LOW - Chase nearest opponent with low aggression
    CHASE_LOW(() -> {
        // PRIORITY: Proportional to minus distance to the nearest smaller opponent within CHASE_LOW_SEARCH_RADIUS, or MIN_VALUE if no enemies are found
        var bot = botService.getBot();
        var gameState = botService.getGameState();
        var world = gameState.getWorld();

        if (!gameState.getPlayerGameObjects().isEmpty()) {
            // Score is distance to nearest other player within a radius times a constant
            var players = Objects.findPlayersWithin(bot, player -> Objects.isEnemyPlayer(player) && bot.getSize() > player.getSize(), Vars.CHASE_LOW_SEARCH_RADIUS);
            if (players != null && !players.isEmpty()) {
                int distance = (int) Objects.distanceBetween(bot, players.get(0));
                return (int) (Vars.CHASE_LOW_SCOREMULT * (2 * world.radius - distance));
            }
        }
        return Integer.MIN_VALUE;
    }, action -> {
        // ACTION: Move towards the nearest smaller opponent
        var bot = botService.getBot();
        var gameState = botService.getGameState();
        var world = gameState.getWorld();

        if (!gameState.getPlayerGameObjects().isEmpty()) {
            // Find nearest player with smaller size
            var players = Objects.findPlayers(bot, player -> Objects.isEnemyPlayer(player) && bot.getSize() > player.getSize());
            if (players != null && !players.isEmpty()) {
                // Move towards the nearest smaller player
                action.action = PlayerActions.FORWARD;
                action.heading = Objects.headingBetween(bot, players.get(0));
            }
        }
        return action;
    });

    /* ------------------------------------------------------------------------------------------------------- */
    public final GameEvaluator eval;
    public final ActionFunc func;

    private BotState(GameEvaluator evaluator, ActionFunc actionFunc) {
        eval = evaluator;
        func = actionFunc;
    }

    // Return state with maximum evaluation score
    public static BotState getNextState() {
        var str = new StringBuilder();
        var bot = botService.getBot();
        str.append(String.format("Tick %d, pos (%d %d), size %d, evaluation score:\n", botService.getGameState().getWorld().getCurrentTick(), bot.getPosition().getX(), bot.getPosition().getY(), bot.getSize()));

        var values = BotState.values();
        BotState current = values[0];
        int currentEval = current.eval.get();
        str.append(String.format("  %s: %d", current.name(), currentEval));

        for (int i = 1; i < values.length; i++) {
            int newEval = values[i].eval.get();
            if (newEval > currentEval) {
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
