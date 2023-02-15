package Services;

import Models.GameObject;
import Models.GameState;
import Models.PlayerAction;
import Rakus.Struct.BotState;
import Rakus.Vars;

import java.util.Optional;

public class BotService {
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;

    // RAKUS
    private int lastTickUpdate;

    private BotState botState;
    private GameObject currentTarget;

    private boolean isFiringTorpedo;
    // private int torpedoWarmup;
    private int torpedoCooldown;
    // END RAKUS

    public BotService() {
        this.playerAction = new PlayerAction();
        this.gameState = new GameState();

        // RAKUS
        this.lastTickUpdate = -1;

        this.botState = BotState.IDLE;
        this.currentTarget = null;

        this.isFiringTorpedo = false;
        // this.torpedoWarmup = 0;
        this.torpedoCooldown = 0;

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

    public boolean consumeTorpedoCharge() {
        // Return true if already firing a torpedo
        if (isFiringTorpedo) return true;

        // Only return true if a charge is available and bot is sufficiently sized
        if(bot.getTorpedoCharge() <= 0 || bot.getSize() < 10 || torpedoCooldown > 0) return false;

        // Decrement charge and set cooldown to prevent spamming
        isFiringTorpedo = true;
        torpedoCooldown = Vars.TORPEDO_COOLDOWN_TICK;
        return true;
    }

    private void update(int tick) {
        // Update torpedo
        if (torpedoCooldown > 0) torpedoCooldown--;
        /*if (isFiringTorpedo) torpedoWarmup++;
        if (torpedoWarmup > Vars.TORPEDO_WARMUP_TICK) {
            isFiringTorpedo = false;
            torpedoWarmup = 0;
        }*/
    }
    // END RAKUS

    public void computeNextPlayerAction(PlayerAction playerAction) {
        // RAKUS
        Optional<Integer> optionalTick = Optional.ofNullable(gameState.getWorld().getCurrentTick());
        optionalTick.ifPresent(tick -> {
            if(tick == lastTickUpdate)return;

            // Update attributes
            update(tick);

            // Update bot state, compute action
            botState = BotState.getNextState();
            this.playerAction = botState.func.get(playerAction);

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
        Optional<GameObject> optionalBot = gameState.getPlayerGameObjects().stream().filter(gameObject -> gameObject.id.equals(bot.id)).findAny();
        optionalBot.ifPresent(bot -> this.bot = bot);
    }
}
