package engine;

import engine.evaluators.MaterialEvaluator;
import engine.evaluators.MaterialMinusOthersEvaluator;

import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class GameStateTest {
    public static void main(String[] args) throws Exception {
        GameState game = GameState.newGame();
        Random random = new Random();
        BoardEvaluator evaluator = new MaterialMinusOthersEvaluator(random);
        Engine engine = new Engine(evaluator, 4, 500000);


        while (!game.isOver()) {
            EvaluatedGameState evaluatedGameState = engine.evaluate(game, 4, true);
            game = evaluatedGameState.getBestMove();
//            System.out.println("Table size: " + engine.transpositionTable.size());
            System.out.println(game);
            System.out.println(evaluator.evaluate(game).rounded());
            System.out.println();
        }
    }


}