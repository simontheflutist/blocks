package engine;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import game.StandardPieces;
import lombok.AllArgsConstructor;
import lombok.Getter;
import model.board.Board;
import model.piece.Piece;
import model.player.Player;

import java.util.*;

@AllArgsConstructor
public class GameState {
    static final Map<Piece, List<Piece>> dihedralOrbit = generateDihedralOrbits();

    private static Map<Piece, List<Piece>> generateDihedralOrbits() {
        ImmutableMap.Builder<Piece, List<Piece>> builder = ImmutableMap.builder();
        for (Piece piece : StandardPieces.ALL_PIECES) {
            List<Piece> orbit = new ArrayList<>(piece.getDihedralOrbit());
            builder.put(piece, orbit);
        }
        return builder.build();
    }

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
        final Player nowPlaying = this.nowPlaying();
        final ImmutableList<Piece> pieces = this.unplayedPieces.get(nowPlaying);

        if (pieces == null) {
            return ImmutableList.of(this.pass());
        }

        // Find all sticky locations that a sticky location of a new piece can anchor to.
        for (final int[] boardSticky : this.board.getStickyLocationsForPlayer(nowPlaying)) {
            // Go through all pieces. We need this loop because k is the index used to remove a piece from this
            // player's hand.
            for (int k = 0; k < pieces.size(); k++) {
                final Piece piece = pieces.get(k);
                for (final Piece transformedPiece : dihedralOrbit.get(piece)) {
                    for (int l = 0; l < piece.nSquares; l++) {
                        final Optional<Board> moveAttempt =
                                this.moveToAlignSticky(boardSticky,
                                        new int[] {
                                                piece.rowLocations.get(l),
                                                piece.colLocations.get(l)},
                                        transformedPiece, nowPlaying);

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

    private Optional<Board> moveToAlignSticky(int[] boardSticky, int[] pieceSticky, Piece piece, Player nowPlaying) {
        // Translation equation is: pieceSticky - boardSticky = pieceAnchor - boardAnchor
        // For example, for (1,1) on the piece to land on (0,0) on the board, the displacement is (-1,-1)
        int row = boardSticky[0] - pieceSticky[0];
        int col = boardSticky[1] - pieceSticky[1];

        return this.board.move(row, col, piece, nowPlaying);
    }

    private GameState pass() {
        return new GameState(board,
                nextPlayers.isEmpty() ? nextPlayers : nextPlayers.subList(1, nextPlayers.size()),
                unplayedPieces,
                turnNumber + 1);
    }

    public boolean isOver() {
        return this.nextPlayers.isEmpty();
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

    /**
     * Used for T-table, so turn number not taken int account.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameState gameState = (GameState) o;
        return Objects.equals(board, gameState.board)
                && Objects.equals(nextPlayers, gameState.nextPlayers)
                && Objects.equals(unplayedPieces, gameState.unplayedPieces);
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, nextPlayers, unplayedPieces);
    }
}
