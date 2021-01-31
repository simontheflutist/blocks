package game;

import com.google.common.collect.ImmutableList;
import model.piece.Piece;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class StandardPieces {
    private static final Piece PIECE_Q = new Piece(1, 1,
            ImmutableList.of(0),
            ImmutableList.of(0));
    private static final Piece PIECE_W = new Piece(1, 2,
            ImmutableList.of(0, 0),
            ImmutableList.of(0, 1));
    private static final Piece PIECE_E = new Piece(2, 2,
            ImmutableList.of(0, 0, 1),
            ImmutableList.of(0, 1, 1));
    private static final Piece PIECE_R = new Piece(1, 3,
            ImmutableList.of(0, 0, 0),
            ImmutableList.of(0, 1, 2));
    private static final Piece PIECE_T = new Piece(2, 2,
            ImmutableList.of(0, 0, 1, 1),
            ImmutableList.of(0, 1, 0, 1));
    private static final Piece PIECE_Y = new Piece(2, 3,
            ImmutableList.of(0, 1, 1, 1),
            ImmutableList.of(1, 0, 1, 2));
    private static final Piece PIECE_U = new Piece(1, 4,
            ImmutableList.of(0, 0, 0, 0),
            ImmutableList.of(0, 1, 2, 3));
    private static final Piece PIECE_I = new Piece(2, 3,
            ImmutableList.of(0, 1, 1, 1),
            ImmutableList.of(2, 0, 1, 2));
    private static final Piece PIECE_O = new Piece(2, 3,
            ImmutableList.of(0, 0, 1, 1),
            ImmutableList.of(1, 2, 0, 1));
    private static final Piece PIECE_P = new Piece(2, 4,
            ImmutableList.of(0, 1, 1, 1, 1),
            ImmutableList.of(0, 0, 1, 2, 3));
    private static final Piece PIECE_S = new Piece(3, 3,
            ImmutableList.of(0, 1, 2, 2, 2),
            ImmutableList.of(1, 1, 0, 1, 2));
    private static final Piece PIECE_F = new Piece(3, 3,
            ImmutableList.of(0, 1, 2, 2, 2),
            ImmutableList.of(0, 0, 0, 1, 2));
    private static final Piece PIECE_G = new Piece(2, 4,
            ImmutableList.of(0, 0, 0, 1, 1),
            ImmutableList.of(1, 2, 3, 0, 1));
    private static final Piece PIECE_H = new Piece(3, 3,
            ImmutableList.of(0, 1, 1, 1, 2),
            ImmutableList.of(2, 0, 1, 2, 0));
    private static final Piece PIECE_J = new Piece(5,1,
            ImmutableList.of(0, 1, 2, 3, 4),
            ImmutableList.of(0, 0, 0, 0, 0));
    private static final Piece PIECE_K = new Piece(3, 2,
            ImmutableList.of(0, 1, 1, 2, 2),
            ImmutableList.of(0, 0, 1, 0, 1));
    private static final Piece PIECE_L = new Piece(3, 3,
            ImmutableList.of(0, 0, 1, 1, 2),
            ImmutableList.of(1, 2, 0, 1, 0));
    private static final Piece PIECE_Z = new Piece(3, 2,
            ImmutableList.of(0, 0, 1, 2, 2),
            ImmutableList.of(0, 1, 0, 0, 1));
    private static final Piece PIECE_X = new Piece(3, 3,
            ImmutableList.of(0, 0, 1, 1, 2),
            ImmutableList.of(1, 2, 0, 1, 1));
    private static final Piece PIECE_V = new Piece(3, 3,
            ImmutableList.of(0, 1, 1, 1, 2),
            ImmutableList.of(1, 0, 1, 2, 1));
    private static final Piece PIECE_N = new Piece(2, 4,
            ImmutableList.of(0, 1, 1, 1, 1),
            ImmutableList.of(1, 0, 1, 2, 3));

    static class SingletonPiece extends Piece {
        static AtomicInteger sequenceNumber = new AtomicInteger();
        int hashCode = sequenceNumber.getAndIncrement();

        public SingletonPiece(Piece piece) {
            super(piece.nRows, piece.nCols, piece.rowLocations, piece.colLocations);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            SingletonPiece that = (SingletonPiece) o;
            return hashCode == that.hashCode;
        }

        @Override
        public int hashCode() {
            return this.hashCode;
        }
    }

    public static final ImmutableList<Piece> ALL_PIECES = Stream.of(
                PIECE_Q, PIECE_W, PIECE_E, PIECE_R, PIECE_T, PIECE_Y, PIECE_U, PIECE_I, PIECE_O, PIECE_P,
                PIECE_S, PIECE_F, PIECE_G, PIECE_H, PIECE_J, PIECE_K, PIECE_L,
                PIECE_Z, PIECE_X, PIECE_V, PIECE_N)
            .map(SingletonPiece::new)
            .collect(ImmutableList.toImmutableList());
}
