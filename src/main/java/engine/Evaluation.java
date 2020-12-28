package engine;

import lombok.ToString;
import lombok.Value;
import model.player.Player;

import java.util.EnumMap;

@Value
public class Evaluation {
    /**
     * How good this position is for each player.
     */
    final EnumMap<Player, Double> scores;

    public Evaluation rounded() {
        EnumMap<Player, Double> scores = new EnumMap<>(Player.class);
        for (Player player : this.getScores().keySet()) {
            scores.put(player, (double) (this.scores.get(player).intValue()));
        }
        return new Evaluation(scores);
    }
}
