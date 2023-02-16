package Rakus.Struct;

import Enums.ObjectTypes;
import Enums.PlayerActions;
import Models.GameObject;
import Models.Position;
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
        // ACTION: Move towards the center of the map
        action.action = PlayerActions.FORWARD;
        action.heading = Objects.headingReverse(Objects.headingFromOrigin(botService.getBot()));
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
                target = Objects.findClosest(item -> Objects.distanceBetween(bot, item) /*+ Objects.priorityPenalty(item, bot)*/, obj -> Objects.isFood(obj) && Objects.priorityPenalty(obj, bot) <= 50);
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
                target = Objects.findClosest(item -> Objects.distanceBetween(bot, item) /*+ Objects.priorityPenalty(item, bot)*/, obj -> Objects.isFood(obj) && Objects.priorityPenalty(obj, bot) <= 50);
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
                        item -> Objects.headingDiff(Objects.headingBetween(bot, item), directHeading),
                        item -> Objects.isFood(item) && Objects.safeFromEdge(item)
                );

                if (Objects.isWithin(bot, players.get(0), bot.getSize() + Vars.FLEE_LOW_TORPEDO_RANGE) && botService.consumeTorpedoCharge()) {
                    // Fire a torpedo salvo towards the nearest opponent
                    action.action = PlayerActions.FIRETORPEDOES;
                    action.heading = Objects.headingReverse(directHeading);
                } else {
                    // Move towards food, or away from opponent if no food is found
                    action.action = PlayerActions.FORWARD;
                    action.heading = food == null ? directHeading : Objects.headingBetween(bot, food);
                }
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
                var teleporter = botService.getTeleporter();
                if (teleporter == null && Objects.isWithin(bot, players.get(0), bot.getSize() + Vars.CHASE_LOW_TORPEDO_RANGE) && botService.consumeTorpedoCharge()) {
                    // Fire a torpedo salvo towards the nearest opponent
                    action.action = PlayerActions.FIRETORPEDOES;
                } else {
                    // Move towards the nearest smaller opponent
                    action.action = PlayerActions.FORWARD;
                }
                action.heading = Objects.headingBetween(bot, players.get(0));
            }
        }
        return action;
    }),

    /* ------------------------------------------------------------------------------------------------------- */
    // CHASE_HIGH - Gives aggressive pursuit to a vulnerable opponent
    CHASE_HIGH(() -> {
        // PRIORITY: Proportional to minus distance to the nearest smaller opponent within CHASE_HIGH_SEARCH_RADIUS, or MIN_VALUE if no enemies are found
        var bot = botService.getBot();
        var gameState = botService.getGameState();
        var world = gameState.getWorld();

        if (!gameState.getPlayerGameObjects().isEmpty()) {
            // Score is distance to nearest other player within a radius times a constant
            var players = Objects.findPlayersWithin(bot, player -> Objects.isEnemyPlayer(player) && bot.getSize() > (botService.getTeleporter() == null ? Vars.CHASE_HIGH_SIZE_DIFF : 0) + player.getSize(), Vars.CHASE_HIGH_SEARCH_RADIUS);
            if (players != null && !players.isEmpty()) {
                int distance = (int) Objects.distanceBetween(bot, players.get(0));
                return (int) (Vars.CHASE_HIGH_SCOREMULT * (2 * world.radius - distance + ((botService.getTeleporter() != null ? 1 : 0) + bot.getTeleporterCharge()) * Vars.CHASE_HIGH_TELEPORT_BONUS));
            }
        }

        return Integer.MIN_VALUE;

    }, action -> {
        // ACTION: Move towards the targeted opponent, utilizing teleporters if available
        var bot = botService.getBot();
        var gameState = botService.getGameState();
        var world = gameState.getWorld();

        if (!gameState.getPlayerGameObjects().isEmpty()) {
            // Find nearest player with smaller size
            var players = Objects.findPlayers(bot, player -> Objects.isEnemyPlayer(player) && bot.getSize() > (botService.getTeleporter() == null ? Vars.CHASE_HIGH_SIZE_DIFF : 0) + Vars.CHASE_HIGH_SIZE_DIFF + player.getSize());
            if (players != null && !players.isEmpty()) {
                var teleporter = botService.getTeleporter();
                if (teleporter == null && Objects.isWithin(bot, players.get(0), bot.getSize() + Vars.CHASE_HIGH_TELEPORT_RANGE) && botService.consumeTeleporterCharge()){
                    // Fire a teleporter towards the targeted opponent
                    action.action = PlayerActions.FIRETELEPORT;
                } else{
                    if(Objects.isWithin(bot, players.get(0), bot.getSize() + Vars.CHASE_HIGH_TORPEDO_RANGE) && botService.consumeTorpedoCharge()){
                        // Fire a torpedo salvo towards the nearest opponent
                        action.action = PlayerActions.FIRETORPEDOES;
                    }else{
                        // Move towards the nearest smaller opponent
                        action.action = PlayerActions.FORWARD;
                    }
                }
                action.heading = Objects.headingBetween(bot, players.get(0));
            }
        }
        return action;
    }),

    /* ------------------------------------------------------------------------------------------------------- */
    // PICK_SUPERNOVA - Move towards the supernova pickup
    PICK_SUPERNOVA(() -> {
        // PRIORITY: A b s o l u t e
        var bot = botService.getBot();
        var gameState = botService.getGameState();
        var world = gameState.getWorld();

        if (!gameState.getGameObjects().isEmpty() && (bot.teleporterCharge > 0 || botService.getTeleporter() != null)) {
            if (Objects.findClosest(bot, obj -> obj.getGameObjectType() == ObjectTypes.SUPERNOVA_PICKUP) != null)
                return (int)(Vars.PICK_SUPERNOVA_SCOREMULT * (Vars.PICK_SUPERNOVA_WEIGHT_BIAS + bot.teleporterCharge) * bot.getSize());
        }

        return Integer.MIN_VALUE;

    }, action -> {
        // ACTION: is for me????
        var bot = botService.getBot();
        var gameState = botService.getGameState();
        var world = gameState.getWorld();

        if (!gameState.getGameObjects().isEmpty()) {
            var pickup = Objects.findClosest(bot, obj -> obj.getGameObjectType() == ObjectTypes.SUPERNOVA_PICKUP);

            if (pickup != null) {
                FLEE_LOW.func.get(action);
                System.out.printf("[WARN] Supernova pickup is present at (%d %d)%n", pickup.getPosition().getX(), pickup.getPosition().getY());

                var teleporter = botService.getTeleporter();
                if (teleporter == null && botService.consumeTeleporterCharge()) {
                    // Fire a teleporter towards the pickup
                    action.action = PlayerActions.FIRETELEPORT;
                    action.heading = Objects.headingBetween(bot, pickup);
                } else if (Objects.isWithin(bot, pickup, Vars.PICK_SUPERNOVA_TOXIC_RADIUS)) {
                    var opponents = Objects.findPlayersWithin(pickup, Objects::isEnemyPlayer, bot.getSize() + Vars.PICK_SUPERNOVA_TOXIC_RADIUS);
                    if (opponents != null && !opponents.isEmpty() && botService.consumeTorpedoCharge()) {
                        // Lodge torpedoes in those undeserving of the Holy Weapon
                        action.action = PlayerActions.FIRETORPEDOES;
                        action.heading = Objects.headingBetween(bot, opponents.get(0));
                    } else {
                        // Move one step closer into the Weapon's embrace
                        action.action = PlayerActions.FORWARD;
                        action.heading = Objects.headingBetween(bot, pickup);
                    }
                }
            }
        }

        return action;
    }),

    /* ------------------------------------------------------------------------------------------------------- */
    // ANY - A special state that is always run on every tick AFTER the current bot state update
    ANY(() -> Integer.MIN_VALUE, action -> {
        // ACTION: Do high-priority or emergency tasks
        var bot = botService.getBot();
        var gameState = botService.getGameState();
        var world = gameState.getWorld();

        var opponents = Objects.findPlayers(GameObject::getSize, Objects::isEnemyPlayer);
        String act = null;

        // Move away from gas clouds
        var gasClouds = Objects.findWithin(bot, obj -> obj.getGameObjectType() == ObjectTypes.GAS_CLOUD, bot.getSize() + Vars.ANY_GASCLOUD_SEARCH_RADIUS);
        if (action.action == PlayerActions.FORWARD && gasClouds != null && !gasClouds.isEmpty()) {
            var nearest = gasClouds.get(0);
            int avoidance = Objects.headingMedian(bot.currentHeading, Objects.headingBetween(nearest, bot));
            // Smoothing step to reduce jittery turning movement
            action.heading = Objects.headingMedian(action.heading, avoidance);
            act = "[INFO] Attempting to avoid a gas cloud";
        }

        // Move away from world edge
        if (action.action == PlayerActions.FORWARD && Objects.distanceFromOrigin(bot) + bot.getSize() + Vars.ANY_EDGE_AVOID_RADIUS > world.getRadius()) {
            int avoidance = Objects.headingMedian(bot.currentHeading, Objects.headingReverse(Objects.headingFromOrigin(bot)));
            // Smoothing step to reduce jittery turning movement
            action.heading = Objects.headingMedian(action.heading, avoidance);
            act = "[INFO] Attempting to move away from world edge";
        }

        if (opponents != null && !opponents.isEmpty()){
            // Fire a supernova bomb
            if(botService.consumeSupernova()){
                action.action = PlayerActions.FIRESUPERNOVA;
                action.heading = Objects.headingBetween(bot, opponents.get(opponents.size() - 1));
                act = "[INFO] Firing a supernova!";
            }

            // Detonate a supernova bomb
            if(botService.getSupernova() != null){
                if(!Objects.isWithin(botService.getSupernova(), bot, 0.25 * world.getRadius())
                    && !Objects.findPlayersWithin(botService.getSupernova(), Objects::isEnemyPlayer, 0.25 * world.getRadius()).isEmpty()) {
                    action.action = PlayerActions.DETONATESUPERNOVA;
                    act = "[INFO] Detonating a supernova!";
                }
            }
        }

        var teleporter = botService.getTeleporter();

        // Trigger chase teleporters
        if (teleporter != null /*&& Objects.findPlayersWithin(teleporter, player -> Objects.isEnemyPlayer(player) && bot.getSize() < player.getSize(), Vars.CHASE_HIGH_TELEPORT_CLEARANCE * bot.getSize()).isEmpty()*/
            && !Objects.findPlayersWithin(teleporter, player -> Objects.isEnemyPlayer(player) && bot.getSize() > player.getSize(), Vars.CHASE_TELEPORT_CLEARANCE * bot.getSize()).isEmpty()){
            // If teleporter is close to a target and it is safe to do so (no larger players in range), trigger
            act = "[INFO] Attempting to teleport to opponent!";
            action.action = PlayerActions.TELEPORT;
        }

        // Trigger supernova pickup teleporters
        var pickup = Objects.findClosest(bot, obj -> obj.getGameObjectType() == ObjectTypes.SUPERNOVA_PICKUP);
        if (Objects.isWithin(teleporter, pickup, bot.getSize() + Vars.PICK_SUPERNOVA_TELEPORT_RADIUS)
            && Objects.findPlayersWithin(bot, Objects::isEnemyPlayer, bot.getSize() + Vars.PICK_SUPERNOVA_TELEPORT_SAFETY_RADIUS).isEmpty()) {
            // Teleport to the pickup if the surrounding is relatively safe
            act = "[INFO] Attempting to teleport to supernova pickup!";
            action.action = PlayerActions.TELEPORT;
        }

        // Block incoming torpedoes with shield
        if (bot.getShieldCharge() > 0 && !Objects.findWithin(bot,
            obj ->
                obj.getGameObjectType() == ObjectTypes.TORPEDO_SALVO
                && Objects.headingDiff(Objects.headingBetween(bot, obj), obj.currentHeading) >= Vars.ANY_SHIELD_HEADING_DIFF,
                bot.getSize() + Vars.ANY_SHIELD_SEARCH_RADIUS).isEmpty() && bot.getSize() > Vars.SHIELD_SAFE_SIZE) {
            action.action = PlayerActions.ACTIVATESHIELD;
            act = "[INFO] Activating shield";
        }

        // Intercept incoming torpedoes
        var torpedoes = Objects.findWithin(bot,
            obj ->
                obj.getGameObjectType() == ObjectTypes.TORPEDO_SALVO
                && Objects.headingDiff(Objects.headingBetween(bot, obj), obj.currentHeading) >= Vars.ANY_INTERCEPT_HEADING_DIFF,
                bot.getSize() + Vars.ANY_INTERCEPT_SEARCH_RADIUS);
        if (!torpedoes.isEmpty() && botService.consumeTorpedoCharge()) {
            action.action = PlayerActions.FIRETORPEDOES;
            action.heading = Objects.headingBetween(bot, torpedoes.get(0));
            act = "[INFO] Attempting to intercepting incoming torpedoes";
        }

        if (act != null) System.out.println(act);
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
        str.append(String.format("[INFO] Tick %d, current bot info:\n", botService.getGameState().getWorld().getCurrentTick()));
        str.append(bot.toString());

        str.append("evaluation:\n");
        var values = BotState.values();
        BotState current = values[0];
        int currentEval = current.eval.get();
        str.append(String.format("  %s: %d", current.name(), currentEval));

        for (int i = 1; i < values.length - 1; i++) {
            int newEval = values[i].eval.get();
            if (newEval > currentEval) {
                current = values[i];
                currentEval = newEval;
            }
            str.append(String.format("\n  %s: %d", values[i].name(), newEval));
        }
        str.append(String.format("\n  -> State selected: %s", current.name()));

        System.out.println(str.toString());
        return current;
    }
}
