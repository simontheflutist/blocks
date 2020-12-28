package engine.evaluators;

import engine.BoardEvaluator;
import engine.Evaluation;
import engine.GameState;
import model.player.Player;

import java.util.EnumMap;

public class MaterialEvaluator implements BoardEvaluator {
    @Override
    public Evaluation evaluate(GameState state) {
        EnumMap<Player, Double> evals = new EnumMap<>(Player.class);
        state.getBoard().getNSquaresOccupied().entrySet().stream()
                .forEach(entry -> evals.put(entry.getKey(), entry.getValue().doubleValue()));
        return new Evaluation(evals);
    }
}
