package cwlib.enums;

import cwlib.io.ValueEnum;

public enum CameraType implements ValueEnum<Integer> {
    CAMERA_ZONE(0),
    PHOTO_BOOTH(1),
    SPEECH_BUBBLE(2),
    CUTSCENE(3);
    
    private final int value;
    private CameraType(int value) {
        this.value = value;
    }

    public Integer getValue() { return this.value; }
    public static CameraType fromValue(int value) {
        for (CameraType part : CameraType.values()) {
            if (part.value == value) 
                return part;
        }
        return null;
    }
}
