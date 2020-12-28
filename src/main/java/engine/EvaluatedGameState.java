package engine;

import lombok.Value;

@Value
public class EvaluatedGameState {
    final GameState state;
    final Evaluation evaluation;
}
