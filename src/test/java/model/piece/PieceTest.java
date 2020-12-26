package model.piece;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PieceTest {

    /**
     * Looks like an L:
     * X
     * XX
     */
    final Piece piece = new Piece(2, 2, ImmutableList.of(0, 1, 1), ImmutableList.of(0, 0, 1));

    @Test
    void negativeRows() {
        assertThrows(RuntimeException.class,
                () -> new Piece(-1, 0, ImmutableList.of(), ImmutableList.of()));
    }

    @Test
    void negativeCols() {
        assertThrows(RuntimeException.class,
                () -> new Piece(0, -1, ImmutableList.of(), ImmutableList.of()));
    }

    @Test
    void mismatchedParallelArrays() {
        assertThrows(RuntimeException.class,
                () -> new Piece(1, 1, ImmutableList.of(0), ImmutableList.of(0, 0)));
    }

    @Test
    void toArt() {
        assertEquals("X \nXX", piece.toArt());
    }

    @Test
    void flipOnceVertical() {
        assertEquals(" X\nXX", piece.flip(false, true).toArt());
    }

    @Test
    void flipOnceHorizontal() {
        assertEquals("XX\nX ", piece.flip(true, false).toArt());
    }

    @Test
    void flipTwiceVertical() {
        assertEquals(piece,
                piece.flip(false, true).flip(false, true));
    }

    @Test
    void rotateOnce() {
        assertEquals(" X\nXX", piece.rotate().toArt());
    }

    @Test
    void rotateFourTimes() {
        assertEquals(piece, piece.rotate().rotate().rotate().rotate());
    }

    @Test
    void hashCodeEquivalence() {
        assertEquals(piece.hashCode(), piece.rotate().rotate().rotate().rotate().hashCode());
    }
}