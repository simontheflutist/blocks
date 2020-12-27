package engine;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import game.StandardPieces;
import lombok.AllArgsConstructor;
import lombok.Getter;
import model.board.Board;
import model.piece.Piece;
import model.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
public class GameState {
    static final ImmutableList<Player> STARTING_ORDER =
            ImmutableList.of(Player.A, Player.B, Player.C, Player.D);
    static final ImmutableMap<Player, ImmutableList<Piece>> ALL_PIECES_UNPLAYED =
            ImmutableMap.<Player, ImmutableList<Piece>>builder()
                    .put(Player.A, StandardPieces.ALL_PIECES)
                    .put(Player.B, StandardPieces.ALL_PIECES)
                    .put(Player.C, StandardPieces.ALL_PIECES)
                    .put(Player.D, StandardPieces.ALL_PIECES)
                    .build();
    public static final int N_ROWS = 20;
    public static final int N_COLS = 20;

    /**
     * State of the board at this step.
     */
    @Getter
    final Board board;

    /**
     * Players to move---this list cycles forward with each step.
     * Player can be removed if they have no more moves.
     */
    final ImmutableList<Player> nextPlayers;

    /**
     * Pieces that each player has not yet played.
     */
    final ImmutableMap<Player, ImmutableList<Piece>> unplayedPieces;

    /**
     * Number of turns that have passed.
     */
    final int turnNumber;

    public static GameState newGame() {
        return new GameState(new Board(N_ROWS, N_COLS), STARTING_ORDER, ALL_PIECES_UNPLAYED, 0);
    }

    /**
     * Get the moves that can be made at this step.
     * @return list of moves that the current player can take.
     */
    public List<GameState> possibleMoves() {
        final List<GameState> moves = new ArrayList<>();
        // Every location. TODO: cache locations that should not be searched
        for (int i = 0; i < N_ROWS; i++) {
            for (int j = 0; j < N_COLS; j++) {
                final Player nowPlaying = this.nowPlaying();
                final ImmutableList<Piece> pieces = this.unplayedPieces.get(nowPlaying);

                for (int k = 0, getSize = pieces.size(); k < getSize; k++) {
                    Piece piece = pieces.get(k);

                    for (Piece transformedPiece : piece.getDihedralOrbit()) {
                        final Optional<Board> moveAttempt = this.board.move(i, j, transformedPiece, nowPlaying);
                        if (moveAttempt.isEmpty()) {
                            continue;
                        }

                        moves.add(this.createChildState(k, moveAttempt.get()));
                    }
                }
            }
        }

        if (moves.isEmpty()) {
            return ImmutableList.of(this.pass());
        } else {
            return moves;
        }
    }

    private GameState pass() {
        return new GameState(board, nextPlayers.subList(1, nextPlayers.size()), unplayedPieces, turnNumber + 1);
    }

    public boolean isOver() {
        return this.nextPlayers.isEmpty();
    }

    public GameState randomNextMove(Random random) {
        final List<GameState> possibleMoves = this.possibleMoves();
        return possibleMoves.get(random.nextInt(possibleMoves.size()));
    }

    public GameState bestMove(BiFunction<GameState, GameState, Double> heuristic, int depth, double pruneFactor) {
        if (depth == 0) {
            return this;
        }

        final List<GameState> possibleMoves = this.possibleMoves();
        final List<Double> prelimEvals = possibleMoves.stream()
                .map(state -> heuristic.apply(this, state))
                .sorted()
                .collect(Collectors.toList());
        final double minEval = prelimEvals.get((int) (prelimEvals.size() * pruneFactor));

//        if (depth == 2) {
//            System.out.println("hey");
//        }
        final List<Double> evaluations = possibleMoves.parallelStream()
//                .filter(state -> heuristic.apply(this, state) >= minEval)
                .map(state -> heuristic.apply(this, state.bestMove(heuristic, depth - 1, pruneFactor)))
                .collect(Collectors.toList());

        // do this using BoundedExecutor instead
//        for (GameState state : possibleMoves) {
//            final GameState nextBestMove = state.bestMove(heuristic, depth - 1, pruneFactor);
//            evaluations.add(heuristic.apply(nextBestMove));
//        }

        // Find highest evaluation
        int ind = 0;
        double best = Double.MIN_VALUE;
        for (int i = 0; i < evaluations.size(); i++) {
            if (evaluations.get(i) > best) {
                best = evaluations.get(i);
                ind = i;
            }
        }

        return possibleMoves.get(ind);
    }

    private GameState createChildState(int pieceMoved, Board resultingBoard) {
        // New Board
        final Board board = resultingBoard;

        // New NextPlayers
        final ImmutableList.Builder<Player> nextPlayersBuilder = ImmutableList.builder();
        for (int i = 1; i < this.nextPlayers.size(); i++) {
            nextPlayersBuilder.add(this.nextPlayers.get(i));
        }
        final ImmutableList<Player> nextPlayers = nextPlayersBuilder.add(this.nowPlaying()).build();

        // New UnplayedPieces
        final ImmutableMap.Builder<Player, ImmutableList<Piece>> unplayedPiecesBuilder = ImmutableMap.builder();
        for (Player p : STARTING_ORDER) {
            if (p != nowPlaying()) {
                unplayedPiecesBuilder.put(p, this.unplayedPieces.get(p));
            }
        }
        // This list will "lose" a piece.
        final ImmutableList.Builder<Piece> nowPlayingUnplayed = ImmutableList.builder();
        final List<Piece> currentUnplayedPieces = this.unplayedPieces.get(this.nowPlaying());
        for (int i = 0; i < currentUnplayedPieces.size(); i++) {
            if (i != pieceMoved) {
                nowPlayingUnplayed.add(currentUnplayedPieces.get(i));
            }
        }
        final ImmutableMap<Player, ImmutableList<Piece>> unplayedPieces =
                unplayedPiecesBuilder.put(this.nowPlaying(), nowPlayingUnplayed.build()).build();

        // New turnNumber
        final int turnNumber = this.turnNumber + 1;

        return new GameState(board, nextPlayers, unplayedPieces, turnNumber);
    }

    public Player nowPlaying() {
        if (this.nextPlayers.isEmpty()) {
            return Player.NO_PLAYER;
        }
        return this.nextPlayers.get(0);
    }

    @Override
    public String toString() {
        return "Turn " + this.turnNumber + ", \"" + this.nowPlaying().getDisplayName()
                + "\" to play:\n" + this.board.toArt() +
                "\nSquares occupied: " + this.board.getNSquaresOccupied();
    }
}
