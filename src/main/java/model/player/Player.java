package model.player;

import lombok.Getter;

/**
 * The four players of the Block game, and an empty value to denote a square that
 * no player owns.
 */
public enum Player {
    // colors: https://stackoverflow.com/questions/5762491/how-to-print-color-in-console-using-system-out-println
    A( "\033[0;34m" + "█ " + "\033[0m"),
    B("\033[0;32m" + "█ " + "\033[0m"),
    C("\033[0;31m" + "█ " + "\033[0m"),
    D("\033[0;33m" + "█ " + "\033[0m"),
    NO_PLAYER("  " + "\033[0m");

    @Getter
    private String displayName;
    Player(String displayName) {
        this.displayName = displayName;
    }
}
