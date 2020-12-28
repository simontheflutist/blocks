package model.player;

import lombok.Getter;

/**
 * The four players of the Block game, and an empty value to denote a square that
 * no player owns.
 */
public enum Player {
    // colors: https://stackoverflow.com/questions/5762491/how-to-print-color-in-console-using-system-out-println
    A("\u001B[44m" + "A " + "\033[0m"),
    B("\u001B[42m" + "B " + "\033[0m"),
    C("\u001B[41m" + "C " + "\033[0m"),
    D("\u001B[43m" + "D " + "\033[0m"),
    NO_PLAYER("\u001B[47m" + "  " + "\033[0m");
    @Getter
    private String displayName;
    Player(String displayName) {
        this.displayName = displayName;
    }
}
