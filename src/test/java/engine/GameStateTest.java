package engine;

import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class GameStateTest {
    public static void main(String[] args) {
        GameState game = GameState.newGame();

        for (;;) {
            System.out.println(game + "\n");
            game = game.bestMove((ours, theirs) ->
                    0.0d + theirs.getBoard().getNSquaresOccupied().get(ours.nowPlaying()),
                    3, 0.0);
            if (game.isOver()) {
                break;
            }

        }
        System.out.println(game);
    }


}