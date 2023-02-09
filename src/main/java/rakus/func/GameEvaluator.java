package rakus.func;

import Services.*;

public interface GameEvaluator{
    // Functional interface - GameEvaluator takes bot service information and returns an integer specifying an evaluation score of the current
    // game state; used to determine priority of a bot state
    int get();
}
