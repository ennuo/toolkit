package cwlib.enums;

import cwlib.io.ValueEnum;

public enum TutorialState implements ValueEnum<Integer> {
    UNPLAYED(0x0),
    SKIPPED(0x1),
    PLAYED(0x2),
    COMPLETED(0x3);

    private final int value;
    private TutorialState(int value) { this.value = value; }
    public Integer getValue() { return this.value; }

    public static TutorialState fromValue(byte value) {
        for (TutorialState state : TutorialState.values()) {
            if (state.value == value) 
                return state;
        }
        return TutorialState.UNPLAYED;
    }
}
