package model.board;

import com.google.common.collect.ImmutableList;
import model.piece.Piece;
import model.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    private final Board smallBoard = new Board(2, 2);
    private final Piece LPiece = new Piece(2, 2,
            ImmutableList.of(0, 1, 1), ImmutableList.of(0, 0, 1));
    private final Piece vert2Piece = new Piece(2, 1,
            ImmutableList.of(0, 1), ImmutableList.of(0, 0));

    /**
     * Large empty board
     */
    private Board largeEmptyBoard;

    /**
     * Medium board is 5x5 and has a 1x4 piece along the top belonging to player A.
     */
    private Board mediumBoard;

    @BeforeEach
    void setupMediumBoard() {
        mediumBoard = new Board(5, 5);
        Piece piece = new Piece(1, 4,
                ImmutableList.<Integer>builder().add(0).add(0).add(0).add(0).build(),
                ImmutableList.<Integer>builder().add(0).add(1).add(2).add(3).build());
        mediumBoard = mediumBoard.move(0, 0, piece, Player.A).get();
    }

    @BeforeEach
    void setupLargeBoard() {
        largeEmptyBoard = new Board(20,20);
    }

    @Test
    void toArt() {
        assertEquals("  \n  ", smallBoard.toArt());
    }

    /**
     * Board is empty 2x2, piece is vertical 3x1
     */
    @Test
    void fitsWithoutOverlap_tooBigForSmallBoard() {
        Piece piece = new Piece(3, 1, ImmutableList.of(0, 1, 2), ImmutableList.of(0, 0, 0));
        assertFalse(smallBoard.fitsWithoutOverlap(0, 0, piece));
    }

    /**
     * Board is empty 2x2, piece is L
     */
    @Test
    void fitsWithoutOverlap_justRightForSmallBoard() {
        assertTrue(smallBoard.fitsWithoutOverlap(0, 0, LPiece));
    }

    /**
     * Board is empty 2x2, piece is L but tries to go out of bounds
     */
    @Test
    void fitsWithoutOverlap_oobSmallBoard() {
        assertFalse(smallBoard.fitsWithoutOverlap(1, 0, LPiece));
    }

    /**
     * Attempts to place the L right under the top piece.
     */
    @Test
    void doesNotTouchSideOfOwnPiece_MediumBoard_Touching() {
        assertFalse(mediumBoard.doesNotTouchSideOfOwnPiece(1, 0, LPiece, Player.A));
    }

    /**
     * Attempts to place the L under the top piece, leaving a gap
     */
    @Test
    void doesNotTouchSideOfOwnPiece_MediumBoard_NotTouching() {
        assertTrue(mediumBoard.doesNotTouchSideOfOwnPiece(2, 0, LPiece, Player.A));
    }

    /**
     * Vertical 2-piece at the rightmost edge
     */
    @Test
    void touchesCornerOfOwnPiece_Touching() {
        assertTrue(mediumBoard.touchesCornerOfOwnPiece(1, 4, vert2Piece, Player.A));
    }

    /**
     * Vertical 2-piece at the rightmost edge of another player's piece
     */
    @Test
    void touchesCornerOfOwnPiece_WrongPlayer() {
        assertFalse(mediumBoard.touchesCornerOfOwnPiece(1, 4, vert2Piece, Player.B));
    }

    /**
     * L piece with a gap
     */
    @Test
    void touchesCornerOfOwnPiece_NotTouching() {
        assertFalse(mediumBoard.touchesCornerOfOwnPiece(2, 0, LPiece, Player.A));
    }

    @Test
    void move_firstMoveNotInCorner() {
        Optional<Board> result = largeEmptyBoard.move(1, 1, LPiece, Player.A);
        assertTrue(result.isEmpty());
    }

    @Test
    void move_firstMoveWrongCorner() {
        Optional<Board> result = largeEmptyBoard.move(0, 0, LPiece, Player.B);
        assertTrue(result.isEmpty());
    }

    @Test
    void move_firstMoveSuccessful() {
        Optional<Board> result = largeEmptyBoard.move(0, 0, LPiece, Player.A);
        assertTrue(result.isPresent());
        Board resultBoard = result.get();

        assertEquals(Player.A, resultBoard.board[0][0]);
        assertEquals(Player.A, resultBoard.board[1][0]);
        assertEquals(Player.A, resultBoard.board[1][1]);
        assertEquals(Player.NO_PLAYER, resultBoard.board[0][1]);
    }

    @Test
    void move_firstMove_oob() {
        assertTrue(largeEmptyBoard.move(19, 0, LPiece, Player.B).isEmpty());
    }

    @Test
    void move_fullRound() {
        // Player A
        Board board = largeEmptyBoard.move(0, 0,
                LPiece.rotate().rotate().rotate(),
                Player.A).get();
        // Player B
        board = board.move(18, 0, vert2Piece, Player.B).get();
        // Player C
        board = board.move(19, 18, vert2Piece.rotate(), Player.C).get();
        // Player D
        board = board.move(0, 18, LPiece.flip(true, true), Player.D).get();
        // Verify counts
        assertEquals(3, board.nSquaresOccupied.get(Player.A));
        assertEquals(2, board.nSquaresOccupied.get(Player.B));
        assertEquals(2, board.nSquaresOccupied.get(Player.C));
        assertEquals(3, board.nSquaresOccupied.get(Player.D));
        assertEquals(400 - 3 - 2 - 2 - 3, board.nSquaresOccupied.get(Player.NO_PLAYER));
    }

    private Board setupFirstRound() {
        // Player A
        Board board = largeEmptyBoard.move(0, 0,
                LPiece.rotate().rotate().rotate(),
                Player.A).get();
        // Player B
        board = board.move(18, 0, vert2Piece, Player.B).get();
        // Player C
        board = board.move(19, 18, vert2Piece.rotate(), Player.C).get();
        // Player D
        board = board.move(0, 18, LPiece.flip(true, true), Player.D).get();
        return board;
    }

    @Test
    void move_secondRound_oob() {
        Board board = setupFirstRound();
        assertTrue(board.move(0, 1, vert2Piece, Player.A).isEmpty());
    }

    @Test
    void move_secondRound_touching_side() {
        Board board = setupFirstRound();
        assertTrue(board.move(2, 0, vert2Piece, Player.A).isEmpty());
    }

    @Test
    void move_secondRound_gap() {
        Board board = setupFirstRound();
        assertTrue(board.move(3, 3, vert2Piece, Player.A).isEmpty());
    }

    @Test
    void fitsWithoutOverlap_twoPlayer_overlap() {
        Board board = setupFirstRound();
        assertFalse(board.fitsWithoutOverlap(0, 0, LPiece));
    }
}