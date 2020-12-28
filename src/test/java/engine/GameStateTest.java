package engine;

import engine.evaluators.MaterialEvaluator;

import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class GameStateTest {
    public static void main(String[] args) {
        GameState game = GameState.newGame();
        Engine engine = new Engine(new MaterialEvaluator(), 10);
        Random random = new Random();



//        for (;;) {
//            System.out.println(game + "\n");
//            game =
//
//        }
//        System.out.println(game);
    }


}