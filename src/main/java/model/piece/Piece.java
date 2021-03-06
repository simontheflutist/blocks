package model.piece;

import com.google.common.collect.ImmutableList;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A tileable piece, abstracted away from its place on the board.
 * The tile is represented as a matrix with height and width.
 * The filled-in parts are specified by giving their row, column coordinates in parallel arrays.
 */
public class Piece {
    public final int nRows;
    public final int nCols;
    public final int nSquares;
    public final ImmutableList<Integer> rowLocations;
    public final ImmutableList<Integer> colLocations;

    public Piece(int nRows, int nCols, ImmutableList<Integer> rowLocations, ImmutableList<Integer> colLocations) {
        if (nRows < 0 || nCols < 0) {
            throw new IllegalArgumentException();
        }
        if (rowLocations.size() != colLocations.size()) {
            throw new IllegalArgumentException();
        }

        this.nRows = nRows;
        this.nCols = nCols;
        this.rowLocations = rowLocations;
        this.colLocations = colLocations;
        this.nSquares = rowLocations.size();
    }

    public String toArt() {
        return Arrays.stream(this.toArray())
                .map(this::rowToArt)
                .collect(Collectors.joining("\n"));
    }

    private StringBuilder rowToArt(boolean[] row) {
        final StringBuilder sb = new StringBuilder(this.nCols);
        for (int j = 0; j < this.nCols; j++) {
            if (row[j]) {
                sb.append("X");
            } else {
                sb.append(" ");
            }
        }
        return sb;
    }

    public boolean[][] toArray() {
        final boolean[][] array = new boolean[nRows][nCols];
        for (int k = 0; k < this.nSquares; k++) {
            array[rowLocations.get(k)][colLocations.get(k)] = true;
        }
        return array;
    }

    /**
     * Reflect across horizontal and/or vertical axes.
     * @return the same piece but flipped
     */
    public Piece flip(boolean horizontalAxis, boolean verticalAxis) {
        final ImmutableList.Builder<Integer> rowLocationsBuilder = ImmutableList.builder();
        final ImmutableList.Builder<Integer> colLocationsBuilder = ImmutableList.builder();

        for (int k = 0; k < this.nSquares; k++) {
            int rowLoc = this.rowLocations.get(k);
            int colLoc = this.colLocations.get(k);

            if (horizontalAxis) {
                rowLoc = this.nRows - rowLoc - 1;
            }
            if (verticalAxis) {
                colLoc = this.nCols - colLoc - 1;
            }

            rowLocationsBuilder.add(rowLoc);
            colLocationsBuilder.add(colLoc);
        }

        return new Piece(this.nRows, this.nCols, rowLocationsBuilder.build(), colLocationsBuilder.build());
    }

    /**
     * Rotate this piece 90 degrees counterclockwise.
     * @return the piece after a 90 degree counterclockwise rotation.
     */
    public Piece rotate() {
        return this.transpose().flip(true, false);
    }

    private Piece transpose() {
        return new Piece(this.nCols, this.nRows, this.colLocations, this.rowLocations);
    }

    public Set<Piece> getDihedralOrbit() {
        final Set<Piece> pieces = new HashSet<>();
        Piece piece = this;
        for (int i = 0; i < 2; i++) {
            piece = piece.flip(true, false);
            for (int j = 0; j < 4; j++) {
                piece = piece.rotate();
                pieces.add(piece);
            }
        }

        return pieces;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return Arrays.deepEquals(this.toArray(), ((Piece) o).toArray());
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(this.toArray());
    }
}