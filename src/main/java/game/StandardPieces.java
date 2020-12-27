package game;

import com.google.common.collect.ImmutableList;
import model.piece.Piece;

import java.util.List;

public class StandardPieces {
    public static final Piece PIECE_Q = new Piece(1, 1,
            ImmutableList.of(0),
            ImmutableList.of(0));
    public static final Piece PIECE_W = new Piece(1, 2,
            ImmutableList.of(0, 0),
            ImmutableList.of(0, 1));
    public static final Piece PIECE_E = new Piece(2, 2,
            ImmutableList.of(0, 0, 1),
            ImmutableList.of(0, 1, 1));
    public static final Piece PIECE_R = new Piece(1, 3,
            ImmutableList.of(0, 0, 0),
            ImmutableList.of(0, 1, 2));
    public static final Piece PIECE_T = new Piece(2, 2,
            ImmutableList.of(0, 0, 1, 1),
            ImmutableList.of(0, 1, 0, 1));
    public static final Piece PIECE_Y = new Piece(2, 3,
            ImmutableList.of(0, 1, 1, 1),
            ImmutableList.of(1, 0, 1, 2));
    public static final Piece PIECE_U = new Piece(1, 4,
            ImmutableList.of(0, 0, 0, 0),
            ImmutableList.of(0, 1, 2, 3));
    public static final Piece PIECE_I = new Piece(2, 3,
            ImmutableList.of(0, 1, 1, 1),
            ImmutableList.of(2, 0, 1, 2));
    public static final Piece PIECE_O = new Piece(2, 3,
            ImmutableList.of(0, 0, 1, 1),
            ImmutableList.of(1, 2, 0, 1));
    public static final Piece PIECE_P = new Piece(2, 4,
            ImmutableList.of(0, 1, 1, 1, 1),
            ImmutableList.of(0, 0, 1, 2, 3));
    public static final Piece PIECE_S = new Piece(3, 3,
            ImmutableList.of(0, 1, 2, 2, 2),
            ImmutableList.of(1, 1, 0, 1, 2));
    public static final Piece PIECE_F = new Piece(3, 3,
            ImmutableList.of(0, 1, 2, 2, 2),
            ImmutableList.of(0, 0, 0, 1, 2));
    public static final Piece PIECE_G = new Piece(2, 4,
            ImmutableList.of(0, 0, 0, 1, 1),
            ImmutableList.of(1, 2, 3, 0, 1));
    public static final Piece PIECE_H = new Piece(3, 3,
            ImmutableList.of(0, 1, 1, 1, 2),
            ImmutableList.of(2, 0, 1, 2, 0));
    public static final Piece PIECE_J = new Piece(5,1,
            ImmutableList.of(0, 1, 2, 3, 4),
            ImmutableList.of(0, 0, 0, 0, 0));
    public static final Piece PIECE_K = new Piece(3, 2,
            ImmutableList.of(0, 1, 1, 2, 2),
            ImmutableList.of(0, 0, 1, 0, 1));
    public static final Piece PIECE_L = new Piece(3, 3,
            ImmutableList.of(0, 0, 1, 1, 2),
            ImmutableList.of(1, 2, 0, 1, 0));
    public static final Piece PIECE_Z = new Piece(3, 2,
            ImmutableList.of(0, 0, 1, 2, 2),
            ImmutableList.of(0, 1, 0, 0, 1));
    public static final Piece PIECE_X = new Piece(3, 3,
            ImmutableList.of(0, 0, 1, 1, 2),
            ImmutableList.of(1, 2, 0, 1, 1));
    public static final Piece PIECE_V = new Piece(3, 3,
            ImmutableList.of(0, 1, 1, 1, 2),
            ImmutableList.of(1, 0, 1, 2, 1));
    public static final Piece PIECE_N = new Piece(2, 4,
            ImmutableList.of(0, 1, 1, 1, 1),
            ImmutableList.of(1, 0, 1, 2, 3));

    public static final ImmutableList<Piece> ALL_PIECES = ImmutableList.<Piece>builder()
            .add(PIECE_Q, PIECE_W, PIECE_E, PIECE_R, PIECE_T, PIECE_Y, PIECE_U, PIECE_I, PIECE_O, PIECE_P)
            .add(PIECE_S, PIECE_F, PIECE_G, PIECE_H, PIECE_J, PIECE_K, PIECE_L)
            .add(PIECE_Z, PIECE_X, PIECE_V, PIECE_N).build();
}
