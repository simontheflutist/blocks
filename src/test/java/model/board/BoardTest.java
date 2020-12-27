package model.board;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    private final Board smallBoard = new Board(2, 2);

    @Test
    void toArt() {
        assertEquals("  \n  ", smallBoard.toArt());
    }

    @Test
    void fullSmallBoard() {
        assertEquals("AB\nCD", 0);
    }
}