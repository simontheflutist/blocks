package game;

import model.piece.Piece;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StandardPiecesTest {
    @Test
    void number_is_21() {
        assertEquals(21, StandardPieces.ALL_PIECES.size());
    }

    /**
     * Check these by inspection lol.
     */
    public static void main(String[] args) {
        for (Piece piece : StandardPieces.ALL_PIECES) {
            System.out.println(piece.toArt());
            System.out.println();
        }
    }
}