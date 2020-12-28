package engine;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.AllArgsConstructor;
import model.player.Player;

import java.util.*;
import java.util.concurrent.ExecutionException;
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
        if (nowPlaying == Player.C && depth == 5) {
            System.out.printf("");
        }

        if (depth == 0) {
            return new EvaluatedGameState(state, this.evaluator.evaluate(state));
        }

        final List<EvaluatedGameState> candidates = getBestShallowEvaluatedNextMoves(state, nowPlaying);
        final List<Evaluation> deepEvaluations = (parallel ? candidates.parallelStream() : candidates.stream())
                .map(egs -> {
                    try {
                        return transpositionTable.get(new EvaluationTask(egs.getBestMove(), depth - 1));
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(EvaluatedGameState::getEvaluation)
                .collect(Collectors.toList());

        GameState bestMove = candidates.get(0).getBestMove();
        Evaluation evalOfBestMove = deepEvaluations.get(0);
        double bestEval = evalOfBestMove.getScores().get(nowPlaying);
        for (int i = 1; i < candidates.size(); i++) {
            EvaluatedGameState candidate = candidates.get(i);
            double eval = deepEvaluations.get(i).getScores().get(nowPlaying);
            if (eval > bestEval) {
                bestEval = eval;
                evalOfBestMove = deepEvaluations.get(i);
                bestMove = candidate.getBestMove();
            }
        }

        return new EvaluatedGameState(bestMove, evalOfBestMove);
    }

    private List<EvaluatedGameState> getBestShallowEvaluatedNextMoves(GameState state, Player nowPlaying) {
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
