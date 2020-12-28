package engine.evaluators;

import engine.BoardEvaluator;
import engine.Evaluation;
import engine.GameState;
import lombok.AllArgsConstructor;
import model.player.Player;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

@AllArgsConstructor
public class MaterialMinusOthersEvaluator implements BoardEvaluator {
    Random random;

    @Override
    public Evaluation evaluate(GameState state) {
        EnumMap<Player, Double> evals = new EnumMap<>(Player.class);
        Map<Player, Integer> squaresOccupied = state.getBoard().getNSquaresOccupied();
        for (Player player : squaresOccupied.keySet()) {
            double eval = random.nextInt(10) / 10;
            for (Player otherPlayer : squaresOccupied.keySet()) {
                if (otherPlayer.equals(Player.NO_PLAYER)) {
                    continue;
                }
                if (otherPlayer.equals(player)) {
                    eval += squaresOccupied.get(otherPlayer);
                } else {
                    eval -= squaresOccupied.get(otherPlayer);
                }
            }
            evals.put(player, eval);
        }
        return new Evaluation(evals);
    }
}
