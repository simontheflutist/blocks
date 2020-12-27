package model.player;

import lombok.Getter;

/**
 * The four players of the Block game, and an empty value to denote a square that
 * no player owns.
 */
public enum Player {
    A("A"),
    B("B"),
    C("C"),
    D("D"),
    NO_PLAYER(" ");
    @Getter
    private String displayName;
    Player(String displayName) {
        this.displayName = displayName;
    }
}
