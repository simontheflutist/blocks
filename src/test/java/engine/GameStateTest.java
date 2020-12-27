package engine;

import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class GameStateTest {
    public static void main(String[] args) {
        GameState game = GameState.newGame();
        Random random = new Random();

        while (true) {
            game = game.randomNextMove(random);
            if (game.isOver()) {
                break;
            }
        }

        System.out.println(game);
    }
}