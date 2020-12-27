package engine;

import lombok.Value;
import model.piece.Piece;

/**
 * Represents a player's option to place PIECE at ROW, COLUMN.
 */
@Value
public class Move {
    final int row;
    final int column;
    final Piece piece;
}
