package engine;

import engine.evaluators.MaterialEvaluator;
import engine.evaluators.MaterialMinusOthersEvaluator;
import model.player.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

class GameStateTest {
    public static void main(String[] args) throws Exception {
        GameState game = GameState.newGame();
        Random random = new Random();
        BoardEvaluator evaluator = new MaterialEvaluator(random);
        Engine engine = new Engine(evaluator, 100);


        while (!game.isOver()) {
            EvaluatedGameState evaluatedGameState = engine.evaluate(game, 3, false);
            game = evaluatedGameState.getBestMove();
            System.out.println(game);
            System.out.println(evaluator.evaluate(game).rounded());
            for (Player player : Player.values()) {
                Iterable<? extends int[]> sticky = game.getBoard().getStickyLocationsForPlayer(player);
                System.out.print(player.getDisplayName() + " sticky: ");
                sticky.forEach((int[] i) -> {
                    System.out.print("(" + i[0] + ", " + i[1] + "); ");
                });
                System.out.println();
            }
            System.out.println();
        }
    }


}