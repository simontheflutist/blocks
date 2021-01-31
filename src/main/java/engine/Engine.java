package engine;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.AllArgsConstructor;
import model.player.Player;

import java.util.*;
import java.util.concurrent.ExecutionException;

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
        return CacheBuilder.newBuilder()
                .concurrencyLevel(CONCURRENCY_LEVEL)
                .initialCapacity(cacheSize)
                .maximumSize(cacheSize)
                .build(new CacheLoader<>() {
            @Override
            public EvaluatedGameState load(EvaluationTask key) throws Exception {
                return Engine.this.evaluate(key.getGameState(), key.getDepth(), false);
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
    public EvaluatedGameState evaluate(GameState state, int depth, boolean parallel) throws Exception {
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
                    try {
                        // Recursion to go one level deeper
                        EvaluatedGameState deeper =
                                transpositionTable.get(new EvaluationTask(candidate.getBestMove(), depth - 1));
                        // Pair candidate move with deep evaluation
                        return new EvaluatedGameState(candidate.getBestMove(), deeper.getEvaluation());
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    }
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
