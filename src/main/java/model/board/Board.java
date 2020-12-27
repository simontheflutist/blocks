package model.board;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import model.piece.Piece;
import model.player.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Models the state of the board: each square is either occupied by a player or empty. Immutable
 * unless I change my mind.
 */
@AllArgsConstructor
public class Board {
    final int nRows;
    final int nCols;
    final Player[][] board;
    final Map<Player, int[]> playerToStartingCorner;
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

    private Player[][] emptyBoard() {
        final Player[][] board = new Player[this.nRows][this.nCols];
        for (int i = 0; i < this.nRows; i++) {
            for (int j = 0; j < this.nCols; j++) {
                board[i][j] = Player.NO_PLAYER;
            }
        }
        return board;
    }

    public String toArt() {
        return Arrays.stream(board)
                .map(row ->
                        Arrays.stream(row)
                                .map(Player::getDisplayName)
                                .collect(Collectors.joining()))
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

            if (board[r][c] != Player.NO_PLAYER) {
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

        // Use a PQ with lexicographic sort to be nice to memory :)
        final Queue<int[]> locations = new PriorityQueue<>(Board::lexicographicComparing);
        // Add all cardinal neighbors
        for (int k = 0; k < piece.nSquares; k++) {
            // Row and column by displacement
            final int r = i + piece.rowLocations.get(k);
            final int c = j + piece.colLocations.get(k);

            if (r + 1 < this.nRows) {
                addUnique(locations, new int[]{r + 1, c});
            }
            if (r - 1 >= 0) {
                addUnique(locations, new int[]{r - 1, c});
            }
            if (c + 1 < this.nCols) {
                addUnique(locations, new int[]{r, c + 1});
            }
            if (c - 1 >= 0) {
                addUnique(locations, new int[]{r, c - 1});
            }
        }

        // Drain the queue to check no illegal contact
        for (int[] location = locations.poll(); location != null; location = locations.poll()) {
            final int r = location[0];
            final int c = location[1];

            if (this.board[r][c] == player) {
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

        final Queue<int[]> locations = new PriorityQueue<>(Board::lexicographicComparing);
        // Add all sticky squares (+1, +1), (+1, -1), etc.
        for (int k = 0; k < piece.nSquares; k++) {
            // Row and column by displacement
            final int r = i + piece.rowLocations.get(k);
            final int c = j + piece.colLocations.get(k);

            if (r + 1 < this.nRows) {
                if (c + 1 < this.nCols) {
                    addUnique(locations, new int[]{r + 1, c + 1});
                }
                if (c - 1 >= 0) {
                    addUnique(locations, new int[]{r + 1, c - 1});
                }
            }

            if (r - 1 >= 0) {
                if (c + 1 < this.nCols) {
                    addUnique(locations, new int[]{r - 1, c + 1});
                }
                if (c - 1 >= 0) {
                    addUnique(locations, new int[]{r - 1, c - 1});
                }
            }
        }

        // Check if a sticky square is already occupied by the player.
        for (int[] location = locations.poll(); location != null; location = locations.poll()) {
            final int r = location[0];
            final int c = location[1];

            if (this.board[r][c] == player) {
                return true;
            }
        }
        return false;
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
        final Player[][] newBoard = new Player[this.nRows][this.nCols];
        for (int r = 0; r < this.nRows; r++) {
            newBoard[r] = Arrays.copyOf(this.board[r], this.nCols);
        }
        for (int k = 0; k < piece.nSquares; k++) {
            final int r = i + piece.rowLocations.get(k);
            final int c = j + piece.colLocations.get(k);
            newBoard[r][c] = player;
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

    static <T> void addUnique(Queue<T> queue, T object) {
        queue.remove(object);
        queue.offer(object);
    }
}
