package engine.evaluators;

import engine.BoardEvaluator;
import engine.Evaluation;
import engine.GameState;
import lombok.AllArgsConstructor;
import model.player.Player;

import java.util.EnumMap;
import java.util.Random;

@AllArgsConstructor
public class MaterialEvaluator implements BoardEvaluator {
    Random random;

    @Override
    public Evaluation evaluate(GameState state) {
        EnumMap<Player, Double> evals = new EnumMap<>(Player.class);
        state.getBoard().getNSquaresOccupied().entrySet().stream()
                .forEach(entry -> evals.put(
                        entry.getKey(),
                        random.nextDouble()/4 + entry.getValue().doubleValue()));
        return new Evaluation(evals);
    }
}
