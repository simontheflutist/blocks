package engine;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.AllArgsConstructor;
import model.player.Player;

import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
public class Engine {
    public static final int CONCURRENCY_LEVEL = 8;
    /**
     * Heuristic to evaluate a single position
     */
    final BoardEvaluator evaluator;
    /**
     * Only explore at most this many variations
     */
    final int topNVariations;
    /**
     * Cache position evaluations.
     */
    final LoadingCache<EvaluationTask, EvaluatedGameState> transpositionTable;

    public Engine(BoardEvaluator evaluator, int topNVariations, int cacheSize) {
        this.evaluator = evaluator;
        this.topNVariations = topNVariations;
        this.transpositionTable = this.initializeCache(cacheSize);
    }

    private LoadingCache<EvaluationTask, EvaluatedGameState> initializeCache(int cacheSize) {
        return CacheBuilder.newBuilder().concurrencyLevel(CONCURRENCY_LEVEL).maximumSize(cacheSize)
                .build(new CacheLoader<>() {
            @Override
            public EvaluatedGameState load(EvaluationTask key) throws Exception {
                return Engine.this.evaluate(key.getGameState(), key.getDepth());
            }
        });
    }

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
    public EvaluatedGameState evaluate(GameState state, int depth) throws Exception {
        final Player nowPlaying = state.nowPlaying();
        if (depth == 0) {
            return new EvaluatedGameState(state, this.evaluator.evaluate(state));
        }

        // Enumerate the possible moves of the next player.
        final List<GameState> possibleMoves = state.possibleMoves();
        // Shallow-evaluate them and store them in a heap.
        final Queue<EvaluatedGameState> shallowEvaluatedNextMoves =
                new PriorityQueue<>(bestIsLeast(nowPlaying));
        for (GameState child : possibleMoves) {
            shallowEvaluatedNextMoves.offer(new EvaluatedGameState(child, this.evaluator.evaluate(child)));
        }

        // Mutual recursion with trans table to go deeper
        int variationsLeft = this.topNVariations;
        GameState bestNextMove = shallowEvaluatedNextMoves.poll().getBestMove();
        Evaluation evaluationOfBestMove = shallowEvaluatedNextMoves.poll().getEvaluation();
        while (!shallowEvaluatedNextMoves.isEmpty() && variationsLeft-- > 0) {
            final GameState possibleMove = shallowEvaluatedNextMoves.poll().getBestMove();
            final EvaluatedGameState childEval = this.transpositionTable.get(
                    new EvaluationTask(possibleMove, depth - 1));

            if (childEval.getEvaluation().getScores().get(nowPlaying)
                    >= evaluationOfBestMove.getScores().get(nowPlaying)) {
                bestNextMove = possibleMove;
                evaluationOfBestMove = childEval.getEvaluation();
            }
        }

        return new EvaluatedGameState(bestNextMove, evaluationOfBestMove);
    }
}
