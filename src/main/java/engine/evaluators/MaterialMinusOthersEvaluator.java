package engine.evaluators;

import engine.BoardEvaluator;
import engine.Evaluation;
import engine.GameState;
import model.player.Player;

import java.util.EnumMap;
import java.util.Map;

public class MaterialMinusOthersEvaluator implements BoardEvaluator {
    @Override
    public Evaluation evaluate(GameState state) {
        EnumMap<Player, Double> evals = new EnumMap<>(Player.class);
        Map<Player, Integer> squaresOccupied = state.getBoard().getNSquaresOccupied();
        for (Player player : squaresOccupied.keySet()) {
            double eval = 0;
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
