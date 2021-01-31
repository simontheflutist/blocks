package engine;

import lombok.AllArgsConstructor;
import model.player.Player;

import java.util.*;

@AllArgsConstructor
public class Engine {
    /**
     * Heuristic to evaluate a single position
     */
    final BoardEvaluator evaluator;
    /**
     * Only explore at most this many variations
     */
    final int topNVariations;

    /**
     * Sort by highest eval first.
     */
    private static Comparator<EvaluatedGameState> bestIsLeast(Player nowPlaying) {
        return Comparator.comparingDouble(evaluatedGameState ->
                -1 * evaluatedGameState.getEvaluation().getScores().get(nowPlaying));
    }

    /**
     * Evaluate this position by playing each player's best moves for DEPTH many turns
     * afterwards. Cache on return.
     * @param state the state to evaluate
     * @param depth plies to search (0 means compute heuristic on this position)
     * @return the evaluation of this state, and the best move that achieves that evaluation
     */
    public EvaluatedGameState evaluate(GameState state, int depth, boolean parallel)  {
        final Player nowPlaying = state.nowPlaying();

        if (depth == 0) {
            return new EvaluatedGameState(state, this.evaluator.evaluate(state));
        }

        // Get candidate moves.
        final List<EvaluatedGameState> candidates = getCandidateMoves(state, nowPlaying);
        // Deeply evaluate them and return the best. This is hard to unit-test, so I test this by checking for
        // greediness on a low depth.
        return (parallel ? candidates.parallelStream() : candidates.stream())
                .map(candidate -> {
                    // Recursion to go one level deeper
                    EvaluatedGameState deeper =
                            this.evaluate(candidate.getBestMove(), depth - 1, false);
                    // Pair candidate move with deep evaluation
                    return new EvaluatedGameState(candidate.getBestMove(), deeper.getEvaluation());
                })
                .max(Comparator.comparingDouble(
                        evaluatedGameState -> evaluatedGameState.getEvaluation().getScores().get(nowPlaying)))
                .get();
    }

    private List<EvaluatedGameState> getCandidateMoves(GameState state, Player nowPlaying) {
        // Enumerate the possible moves of the next player.
        final List<GameState> possibleMoves = state.possibleMoves();
        // Shallow-evaluate them and store them in a heap.
        final Queue<EvaluatedGameState> shallowEvaluatedNextMoves =
                new PriorityQueue<>(bestIsLeast(nowPlaying));
        for (GameState child : possibleMoves) {
            shallowEvaluatedNextMoves.offer(new EvaluatedGameState(child, this.evaluator.evaluate(child)));
        }

        // Only return the best
        final List<EvaluatedGameState> topMoves = new ArrayList<>();
        int variationsLeft = this.topNVariations;
        while (!shallowEvaluatedNextMoves.isEmpty() && variationsLeft-- > 0) {
            topMoves.add(shallowEvaluatedNextMoves.poll());
        }
        return topMoves;
    }
}
