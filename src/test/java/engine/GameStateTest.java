package engine;

import engine.evaluators.MaterialEvaluator;

import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class GameStateTest {
    public static void main(String[] args) throws Exception {
        GameState game = GameState.newGame();
        Engine engine = new Engine(new MaterialEvaluator(), 10);
        Random random = new Random();

        for (int i = 0; i < 2; i++) {
            EvaluatedGameState evaluatedGameState = engine.evaluate(game, 1);
            game = evaluatedGameState.getBestMove();
            System.out.println(game);
            System.out.println();
        }



//        for (;;) {
//            System.out.println(game + "\n");
//            game =
//
//        }
//        System.out.println(game);
    }


}