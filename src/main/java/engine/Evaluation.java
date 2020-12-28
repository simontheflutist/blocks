package engine;

import lombok.Value;
import model.player.Player;

import java.util.EnumMap;

@Value
public class Evaluation {
    /**
     * How good this position is for each player.
     */
    final EnumMap<Player, Double> scores;
}
