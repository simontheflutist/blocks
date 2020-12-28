package engine;

import model.player.Player;

@FunctionalInterface
public interface BoardEvaluator {
    /**
     * Return a heuristic for how good this position is for the player who is currently playing.
     */
    Evaluation evaluate(GameState state);
}
