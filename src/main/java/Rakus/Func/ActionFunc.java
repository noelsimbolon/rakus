package Rakus.Func;

import Models.PlayerAction;

public interface ActionFunc {
    // Functional interface - ActionFunc takes in bot service information as well as a PlayerAction object, calculates action needed to achieve
    // a determined goal, and returns the PlayerAction object with its fields set to the needed values
    PlayerAction get(PlayerAction action);
}
