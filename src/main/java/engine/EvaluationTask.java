package engine;

import lombok.Value;

import java.util.Objects;

@Value
public class EvaluationTask {
    GameState gameState;
    int depth;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EvaluationTask that = (EvaluationTask) o;
        return depth == that.depth && Objects.equals(gameState, that.gameState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameState, depth);
    }
}
