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
    public static final int TRANSPOSITION_CACHE_SIZE = 5;
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
    final LoadingCache<EvaluationTask, EvaluatedGameState> transpositionTable = this.initializeCache();

    private LoadingCache<EvaluationTask, EvaluatedGameState> initializeCache() {
        return CacheBuilder.newBuilder().concurrencyLevel(CONCURRENCY_LEVEL).maximumSize(TRANSPOSITION_CACHE_SIZE)
                .build(new CacheLoader<>() {
            @Override
            public EvaluatedGameState load(EvaluationTask key) {
                return Engine.this.evaluate(key.getGameState(), key.getDepth());
            }
        });
    }

    /**
     * Sort by highest eval first.
     */
    private static Comparator<EvaluatedGameState> bestForPlayer(Player nowPlaying) {
        return Comparator.comparingDouble(evaluatedGameState ->
                -1 * evaluatedGameState.getEvaluation().getScores().get(nowPlaying));
    }

    /**
     * Evaluate this position by playing each player's best moves for DEPTH many turns
     * afterwards. Cache on return.
     * @param state the state to evaluate
     * @param depth plies to search (0 means compute heuristic on this position)
     * @return same state that was passed in, but decorated with the evaluation.
     */
    public EvaluatedGameState evaluate(GameState state, int depth) {
        final Player nowPlaying = state.nowPlaying();
        if (depth == 0) {
            return new EvaluatedGameState(state, this.evaluator.evaluate(state));
        }

        // Enumerate the possible moves of the next player.
        final List<GameState> possibleMoves = state.possibleMoves();
        // Shallow-evaluate them and store them in a heap.
        final Queue<EvaluatedGameState> shallowEvaluatedNextMoves =
                new PriorityQueue<>(bestForPlayer(nowPlaying));
        for (GameState child : possibleMoves) {
            shallowEvaluatedNextMoves.offer(new EvaluatedGameState(child, this.evaluator.evaluate(child)));
        }

        // Deeply evaluate the top variations.
        int variationsLeft = this.topNVariations;
        Evaluation evaluationOfBestMove = shallowEvaluatedNextMoves.poll().getEvaluation();
        while (!shallowEvaluatedNextMoves.isEmpty() && variationsLeft-- > 0) {
            final GameState possibleMove = shallowEvaluatedNextMoves.poll().getState();
            final EvaluatedGameState opponentsBestMove = this.bestMove(possibleMove, depth - 1);
            if (opponentsBestMove.getEvaluation().getScores().get(nowPlaying)
                    >= evaluationOfBestMove.getScores().get(nowPlaying)) {
                evaluationOfBestMove = opponentsBestMove.getEvaluation();
            }
        }

        return new EvaluatedGameState(state, evaluationOfBestMove);
    }

    /**
     * Choose the best next move.
     * @param state state the choose the best move for
     * @param depth how many plies to look (0 means just look at immediate children)
     * @return best next move, with evaluation.
     */
    public EvaluatedGameState bestMove(GameState state, final int depth) {
        if (state.isOver()) {
            return new EvaluatedGameState(state, this.evaluator.evaluate(state));
        }

        try {
            final Player nowPlaying = state.nowPlaying();
            final Comparator<EvaluatedGameState> comp = bestForPlayer(nowPlaying);
            final List<EvaluationTask> possibleMoves =
                    state.possibleMoves().stream().map(g -> new EvaluationTask(g, depth)).collect(Collectors.toList());
            final Map<EvaluationTask, EvaluatedGameState> evaluatedStates =
                    this.transpositionTable.getAll(possibleMoves);
            return evaluatedStates.entrySet().stream()
                    .max((entry1, entry2) -> comp.compare(entry1.getValue(), entry2.getValue()))
                    .get()
                    .getValue();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
