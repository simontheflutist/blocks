package engine;

import lombok.Value;

@Value
public class EvaluatedGameState {
    final GameState bestMove;
    final Evaluation evaluation;
}
