package Services;

import Enums.ObjectTypes;
import Models.GameObject;
import Models.GameState;
import Models.PlayerAction;
import Rakus.Struct.BotState;
import Rakus.Util.Objects;
import Rakus.Vars;

import java.util.Optional;
import java.util.UUID;

public class BotService {
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;

    // RAKUS
    private int lastTickUpdate;

    private BotState botState;
    private GameObject currentTarget;

    private int torpedoCooldown;

    private GameObject teleporter;
    private int teleporterSearchTime;
    private int teleporterCooldown;
    private boolean hasFiredTeleporter;

    private GameObject supernova;
    private int supernovaSearchTime;
    private boolean hasFiredSupernova;
    // END RAKUS

    public BotService() {
        this.playerAction = new PlayerAction();
        this.gameState = new GameState();

        // RAKUS
        this.lastTickUpdate = -1;

        this.botState = BotState.IDLE;
        this.currentTarget = null;

        this.torpedoCooldown = 0;

        this.teleporter = null;
        this.teleporterSearchTime = 0;
        this.teleporterCooldown = 0;
        this.hasFiredTeleporter = false;

        this.supernova = null;
        this.supernovaSearchTime = 0;
        this.hasFiredSupernova = false;
        Vars.botService = this;
        // END RAKUS
    }


    public GameObject getBot() {
        return this.bot;
    }

    public void setBot(GameObject bot) {
        this.bot = bot;
    }

    public PlayerAction getPlayerAction() {
        return this.playerAction;
    }

    public void setPlayerAction(PlayerAction playerAction) {
        this.playerAction = playerAction;
    }

    // RAKUS
    public BotState getBotState() {
        return this.botState;
    }

    public void setBotState(BotState botState) {
        this.botState = botState;
    }

    public GameObject getCurrentTarget() {
        return this.currentTarget;
    }

    public void setCurrentTarget(GameObject currentTarget) {
        this.currentTarget = currentTarget;
    }

    public GameObject getTeleporter() {
        return this.teleporter;
    }

    public void setTeleporter(GameObject teleporter) {
        this.teleporter = teleporter;
    }

    public void fireTeleporter() {
        this.hasFiredTeleporter = true;
        this.teleporterSearchTime = Vars.OBJECT_SEARCH_TIME;
    }

    public boolean hasFiredTeleporter() {
        return this.hasFiredTeleporter;
    }

    public GameObject getSupernova() {
        return this.supernova;
    }

    public void setSupernova(GameObject supernova) {
        this.supernova = supernova;
    }

    public boolean hasFiredSupernova() {
        return this.hasFiredSupernova;
    }

    public boolean consumeTorpedoCharge() {
        // Only return true if a charge is available and bot is sufficiently sized
        if (bot.getTorpedoCharge() <= 0 || bot.getSize() < Vars.TORPEDO_SAFE_SIZE || torpedoCooldown > 0) return false;

        // Set cooldown to prevent spamming
        torpedoCooldown = Vars.TORPEDO_COOLDOWN_TICK;
        return true;
    }

    public boolean consumeTeleporterCharge() {
        // Only return true if a charge is available and bot is sufficiently sized
        if (bot.getTeleporterCharge() <= 0 || bot.getSize() < Vars.TELEPORTER_SAFE_SIZE || teleporterCooldown > 0) return false;

        // Set cooldown to prevent spamming
        hasFiredTeleporter = true;
        teleporterSearchTime = Vars.OBJECT_SEARCH_TIME;
        teleporterCooldown = Vars.TELEPORTER_COOLDOWN_TICK;
        return true;
    }

    public boolean consumeSupernova() {
        // Return true if a charge is available and has not fired one already
        if (!bot.hasSupernova() || supernova != null) return false;

        hasFiredSupernova = true;
        return true;
    }

    private void update(int tick) {
        // Update cooldown timers
        if (torpedoCooldown > 0) --torpedoCooldown;
        if (teleporterCooldown > 0) --teleporterCooldown;

        // Keep track of teleporter object
        if (teleporter != null) {
            var pos = teleporter.getPosition();
            System.out.printf("[INFO] Tracking a teleporter object at (%d %d)%n", pos.getX(), pos.getY());
        }
        if (hasFiredTeleporter) {
            System.out.println("[INFO] Trying to find my teleporter...");
            teleporter = Objects.findClosest(bot, obj -> obj.getGameObjectType() == ObjectTypes.TELEPORTER);
            if (teleporter != null) {
                hasFiredTeleporter = false;
                System.out.printf("[INFO] Found a teleporter towards %d%n", teleporter.currentHeading);
                teleporterSearchTime = 0;
            } else {
                --teleporterSearchTime;
                if (teleporterSearchTime == 0) {
                    hasFiredTeleporter = false;
                    System.out.println("[INFO] Failed to find my teleporter, stopping search");
                }
            }
        }

        // Keep track of supernova object
        if (supernova != null) {
            var pos = supernova.getPosition();
            System.out.printf("[INFO] Tracking a supernova object at (%d %d)%n", pos.getX(), pos.getY());
        }
        if (hasFiredSupernova) {
            System.out.println("[INFO] Trying to find my supernova...");
            supernova = Objects.findClosest(bot, obj -> obj.getGameObjectType() == ObjectTypes.SUPERNOVA_BOMB);
            if (supernova != null) {
                hasFiredSupernova = false;
                System.out.printf("[INFO] Found a supernova towards %d%n", supernova.currentHeading);
                supernovaSearchTime = 0;
            } else {
                --supernovaSearchTime;
                if (supernovaSearchTime == 0) {
                    hasFiredSupernova = false;
                    System.out.println("[INFO] Failed to find my supernova, this sucks");
                }
            }
        }
    }
    // END RAKUS

    public void computeNextPlayerAction(PlayerAction playerAction) {
        // RAKUS
        Optional<Integer> optionalTick = Optional.ofNullable(gameState.getWorld().getCurrentTick());
        optionalTick.ifPresent(tick -> {
            if(tick == lastTickUpdate)return;
            System.out.printf("\n[TICK] Beginning of tick %d%n", tick);

            // Update attributes
            update(tick);

            // Update bot state, compute action
            botState = BotState.getNextState();
            this.playerAction = botState.func.get(playerAction);
            this.playerAction = BotState.ANY.func.get(playerAction);

            lastTickUpdate = tick;
        });
        // END RAKUS
    }

    public GameState getGameState() {
        return this.gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        updateSelfState();
    }

    private void updateSelfState() {
        var optionalBot = gameState.getPlayerGameObjects().stream().filter(obj -> obj.id.equals(bot.id)).findAny();
        optionalBot.ifPresent(bot -> this.bot = bot);
        currentTarget = Objects.findSelf(currentTarget);
        teleporter = Objects.findSelf(teleporter);
        supernova = Objects.findSelf(supernova);
    }
}
