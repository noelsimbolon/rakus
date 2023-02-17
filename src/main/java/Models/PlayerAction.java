package Models;

import Enums.PlayerActions;

import java.util.UUID;

public class PlayerAction {

    public UUID playerId;
    public PlayerActions action;
    public int heading;

    public UUID getPlayerId() {
        return playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    public PlayerActions getAction() {
        return action;
    }

    public void setAction(PlayerActions action) {
        this.action = action;
    }

    public int getHeading() {
        return heading;
    }

    public void setHeading(int heading) {
        this.heading = heading;
    }
}
