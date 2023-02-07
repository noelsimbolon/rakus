package rakus.func;

import Models.*;
import Services.*;

public interface ActionFunc{
    PlayerAction get(BotService botService, PlayerAction action);
}
