package Services;

import Models.GameObject;
import Models.GameState;
import Models.PlayerAction;
import Rakus.Struct.BotState;
import Rakus.Vars;

import java.util.Optional;
// END RAKUS

public class BotService {
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;

    // RAKUS
    private BotState botState;
    private GameObject currentTarget;
    // END RAKUS

    public BotService() {
        this.playerAction = new PlayerAction();
        this.gameState = new GameState();

        // RAKUS
        this.botState = BotState.IDLE;
        this.currentTarget = null;

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
    // END RAKUS

    public void computeNextPlayerAction(PlayerAction playerAction) {
        // RAKUS
        botState = BotState.getNextState();
        this.playerAction = botState.func.get(playerAction);
        // END RAKUS

        /*playerAction.action = PlayerActions.FORWARD;
        playerAction.heading = new Random().nextInt(360);

        if (!gameState.getGameObjects().isEmpty()) {
            var foodList = gameState.getGameObjects()
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.FOOD)
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());

            playerAction.heading = getHeadingBetween(foodList.get(0));
        }

        this.playerAction = playerAction;*/
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
