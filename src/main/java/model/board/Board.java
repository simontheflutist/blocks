package model.board;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import model.piece.Piece;
import model.player.Player;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Models the state of the board: each square is either occupied by a player or empty. Immutable
 * unless I change my mind.
 */
@AllArgsConstructor
public class Board {
    final int nRows;
    final int nCols;

    final Player[] board;

    private static final Player get(Player[] board, int row, int col, int nRows) {
        return board[row * nRows + col];
    }

    private static final void put(Player[] board, Player player, int row, int col, int nRows) {
        board[row * nRows + col] = player;
    }

    private final Player get(int row, int col) {
        return get(this.board, row, col, this.nRows);
    }

    private final void put(Player player, int row, int col) {
        put(this.board, player, row, col, this.nRows);
    }

    final Map<Player, int[]> playerToStartingCorner;
    @Getter
    final Map<Player, Integer> nSquaresOccupied;

    public Board(int nRows, int nCols) {
        this.nRows = nRows;
        this.nCols = nCols;
        this.board = this.emptyBoard();
        this.playerToStartingCorner = this.startingCorners();
        this.nSquaresOccupied = this.initialCounts();
    }

    private Map<Player, Integer> initialCounts() {
        final Map<Player, Integer> counts = new EnumMap(Player.class);
        counts.put(Player.A, 0);
        counts.put(Player.B, 0);
        counts.put(Player.C, 0);
        counts.put(Player.D, 0);
        counts.put(Player.NO_PLAYER, this.nRows * this.nCols);
        return counts;
    }

    private Map<Player,int[]> startingCorners() {
        return ImmutableMap.<Player, int[]>builder()
                .put(Player.A, new int[] {0, 0})
                .put(Player.B, new int[] {this.nRows - 1, 0})
                .put(Player.C, new int[] {this.nRows - 1, this.nCols - 1})
                .put(Player.D, new int[] {0, this.nCols - 1})
                .build();
    }

    private Player[] emptyBoard() {
        final Player[] board = new Player[this.nRows * this.nCols];
        Arrays.fill(board, Player.NO_PLAYER);
        return board;
    }

    public String toArt() {
        return IntStream.range(0, this.nRows)
                .mapToObj(r ->
                        IntStream.range(0, this.nCols)
                                .mapToObj(c -> this.get(r, c))
                                .map(Player::getDisplayName)
                                .collect(Collectors.joining("")))
                .collect(Collectors.joining("\n"));
    }

    /**
     * Check whether there is space on the board to place this piece here. Specifically, it cannot overlap
     * any other piece and must not exceed the boundaries of the board.
     * @param i row
     * @param j column
     * @param piece
     * @return true if piece does not overlap any other piece at this location and it fits on the board,
     *  false otherwise
     */
    boolean fitsWithoutOverlap(int i, int j, Piece piece) {
        // Use the piece's coordinates as displacements and verify that the spot is empty.
        for (int k = 0; k < piece.nSquares; k++) {
            final int r = i + piece.rowLocations.get(k);
            final int c = j + piece.colLocations.get(k);

            if (r >= this.nRows || c >= this.nCols) {
                return false;
            }

            if (this.get(r, c) != Player.NO_PLAYER) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param i row
     * @param j column
     * @param piece
     * @param player
     * @return true if laying this piece here doesn't lie next to the player's own piece
     */
    boolean doesNotTouchSideOfOwnPiece(int i, int j, Piece piece, Player player) {
        // Idea: generate all the cardinal (north, south, east, west) neighbor squares of this piece.
        // Deduplicate them and then query them to make sure that none of them are occupied by this player.

        // Add all cardinal neighbors
        for (int k = 0; k < piece.nSquares; k++) {
            // Row and column by displacement
            final int r = i + piece.rowLocations.get(k);
            final int c = j + piece.colLocations.get(k);

            if (r + 1 < this.nRows && occupiedBy(r + 1, c, player)) {
                return false;
            }

            if (r - 1 >= 0 && occupiedBy(r - 1, c, player)) {
                return false;
            }
            if (c + 1 < this.nCols && occupiedBy(r, c + 1, player)) {
                return false;
            }
            if (c - 1 >= 0 && occupiedBy(r, c - 1, player)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check whether this piece would have corner-to-corner contact with one of the player's own pieces
     * @param i
     * @param j
     * @param piece
     * @param player
     * @return
     */
    boolean touchesCornerOfOwnPiece(int i, int j, Piece piece, Player player) {
        // Same idea as doesNotTouchSideOfOwnPiece, except this time we are generating "sticky" squares
        // that would have to have one of the player's own squares.

        // Add all sticky squares (+1, +1), (+1, -1), etc.
        for (int k = 0; k < piece.nSquares; k++) {
            // Row and column by displacement
            final int r = i + piece.rowLocations.get(k);
            final int c = j + piece.colLocations.get(k);

            if (r + 1 < this.nRows) {
                if (c + 1 < this.nCols && occupiedBy(r + 1, c + 1, player)) {
                    return true;
                }
                if (c - 1 >= 0 && occupiedBy(r + 1, c - 1, player)) {
                    return true;
                }
            }

            if (r - 1 >= 0) {
                if (c + 1 < this.nCols && occupiedBy(r - 1, c + 1, player)) {
                    return true;
                }
                if (c - 1 >= 0 && occupiedBy(r - 1, c - 1, player)) {
                    return true;
                }
            }
        }

        return false;
    }

    // JVM pls inline :)
    private final boolean occupiedBy(int r, int c, Player player) {
        return this.get(r, c) == player;
    }

    /**
     * Comparator that prefers to scan top down, left to right
     */
    private static int lexicographicComparing(int[] loc1, int[] loc2) {
        int msbDiff = loc1[0] - loc2[0];
        if (msbDiff != 0) {
            return msbDiff;
        }
        return loc1[1] - loc2[1];
    }

    /**
     * Check whether playing PIECE at row I, col J is legal, and return a board with the new state if it is.
     * @param i row
     * @param j column
     * @param piece piece to place
     * @param player who is trying to move
     * @return empty if illegal, otherwise a Callable that yields the new Board.
     */
    public Optional<Board> move(int i, int j, Piece piece, Player player) {
        if (this.isFirstMove(player)) {
            if (!this.startsInCorner(i, j, piece, player)) {
                return Optional.empty();
            }
            if (!this.fitsWithoutOverlap(i, j, piece)) {
                return Optional.empty();
            }
        } else {
            if (!this.fitsWithoutOverlap(i, j, piece)) {
                return Optional.empty();
            }
            if (!this.doesNotTouchSideOfOwnPiece(i, j, piece, player)) {
                return Optional.empty();
            }
            if (!this.touchesCornerOfOwnPiece(i, j, piece, player)) {
                return Optional.empty();
            }
        }

        return Optional.of(this.boardAfterMove(i, j, piece, player));
    }

    Board boardAfterMove(int i, int j, Piece piece, Player player) {
        // New array
        final Player[] newBoard = Arrays.copyOf(this.board, this.board.length);

        for (int k = 0; k < piece.nSquares; k++) {
            final int r = i + piece.rowLocations.get(k);
            final int c = j + piece.colLocations.get(k);
            put(newBoard, player, r, c, this.nRows);
        }

        // New counts
        final int nSquares = piece.nSquares;
        final Map<Player, Integer> nSquaresOccupied = new EnumMap<>(Player.class);
        nSquaresOccupied.putAll(this.nSquaresOccupied);
        nSquaresOccupied.put(player, this.nSquaresOccupied.get(player) + nSquares);
        nSquaresOccupied.put(Player.NO_PLAYER, this.nSquaresOccupied.get(Player.NO_PLAYER) - nSquares);

        return new Board(nRows, nCols, newBoard, playerToStartingCorner, nSquaresOccupied);
    }

    boolean startsInCorner(int i, int j, Piece piece, Player player) {
        final int [] startingCorner = this.playerToStartingCorner.get(player);

        for (int k = 0; k < piece.nSquares; k++) {
            final int r = i + piece.rowLocations.get(k);
            final int c = j + piece.colLocations.get(k);

            if (r == startingCorner[0] && c == startingCorner[1]) {
                return true;
            }
        }

        return false;
    }

    boolean isFirstMove(Player player) {
        return this.nSquaresOccupied.get(player) == 0;
    }

    /**
     * Equals() and HashCode are important for transposition tables!
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Board board1 = (Board) o;
        return nRows == board1.nRows
                && nCols == board1.nCols
                && Arrays.deepEquals(board, board1.board)
                && Objects.equals(playerToStartingCorner, board1.playerToStartingCorner)
                && Objects.equals(nSquaresOccupied, board1.nSquaresOccupied);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }
}
