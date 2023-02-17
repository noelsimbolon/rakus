import Enums.ObjectTypes;
import Models.GameObject;
import Models.GameState;
import Models.GameStateDto;
import Models.Position;
import Services.BotService;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Main {

    public static void main(String[] args) throws Exception {
        Logger logger = LoggerFactory.getLogger(Main.class);
        BotService botService = new BotService();
        String token = System.getenv("Token");
        token = (token != null) ? token : UUID.randomUUID().toString();

        String environmentIp = System.getenv("RUNNER_IPV4");

        String ip = (environmentIp != null && !environmentIp.isBlank()) ? environmentIp : "localhost";
        ip = ip.startsWith("http://") ? ip : "http://" + ip;

        String url = ip + ":" + "5000" + "/runnerhub";

        HubConnection hubConnection = HubConnectionBuilder.create(url)
                .build();

        hubConnection.on("Disconnect", (id) -> {
            System.out.println("[INFO] Disconnected:");

            hubConnection.stop();
        }, UUID.class);

        hubConnection.on("Registered", (id) -> {
            System.out.println("[INFO] Registered with the runner " + id);

            Position position = new Position();
            GameObject bot = new GameObject(id, 10, 20, 0, position, ObjectTypes.PLAYER, 0, 0, false, 0, 0);
            botService.setBot(bot);
        }, UUID.class);

        hubConnection.on("ReceiveGameState", (gameStateDto) -> {
            GameState gameState = new GameState();
            gameState.world = gameStateDto.getWorld();

            for (Map.Entry<String, List<Integer>> objectEntry : gameStateDto.getGameObjects().entrySet()) {
                gameState.getGameObjects().add(GameObject.FromStateList(UUID.fromString(objectEntry.getKey()), objectEntry.getValue()));
            }

            for (Map.Entry<String, List<Integer>> objectEntry : gameStateDto.getPlayerObjects().entrySet()) {
                gameState.getPlayerGameObjects().add(GameObject.FromStateList(UUID.fromString(objectEntry.getKey()), objectEntry.getValue()));
            }

            botService.setGameState(gameState);
        }, GameStateDto.class);

        hubConnection.on("ReceiveGameComplete", (string) -> {
            System.out.println("[INFO] Game over; did I win?");
            System.out.println("[INFO] Game completed with message: " + string);
        }, String.class);

        hubConnection.on("ReceivePlayerConsumed", () -> {
            System.out.println("[INFO] Oh no! I've been consumed!");
        });

        hubConnection.start().blockingAwait();

        Thread.sleep(1000);
        System.out.println("[INFO] Registering with the runner...");
        hubConnection.send("Register", token, "Rakus");

        // This is a blocking call
        hubConnection.start().subscribe(() -> {
            while (hubConnection.getConnectionState() == HubConnectionState.CONNECTED) {
                Thread.sleep(20);

                GameObject bot = botService.getBot();
                if (bot == null) {
                    continue;
                }

                botService.getPlayerAction().setPlayerId(bot.getId());
                botService.computeNextPlayerAction(botService.getPlayerAction());
                if (hubConnection.getConnectionState() == HubConnectionState.CONNECTED) {
                    var action = botService.getPlayerAction();
                    hubConnection.send("SendPlayerAction", action);
                }
            }
        });

        hubConnection.stop();
    }
}
