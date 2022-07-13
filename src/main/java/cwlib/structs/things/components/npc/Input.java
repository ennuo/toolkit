package cwlib.structs.things.components.npc;

import org.joml.Vector3f;
import org.joml.Vector4f;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class Input implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x100;
    
    public boolean alive;
    public Vector3f leftStick, rightStick;
    public int buttons, buttonsOld;
    public short[] sensorData;
    public float[] sensorDathSmooth;
    public Vector4f sensorDir, sensorDirOld;
    public short[] pressureData;
    public int playerNumber;
    public boolean controllingPauseMenu;

    @SuppressWarnings("unchecked")
    @Override public Input serialize(Serializer serializer, Serializable structure) {
        Input input = (structure == null) ? new Input() : (Input) structure;

        input.alive = serializer.bool(input.alive);
        input.leftStick = serializer.v3(input.leftStick);
        input.rightStick = serializer.v3(input.rightStick);
        input.buttons = serializer.i32(input.buttons);
        input.buttonsOld = serializer.i32(input.buttonsOld);
        input.sensorData = serializer.shortarray(input.sensorData);
        input.sensorDathSmooth = serializer.floatarray(input.sensorDathSmooth);
        input.sensorDir = serializer.v4(input.sensorDir);
        input.sensorDirOld = serializer.v4(input.sensorDirOld);
        input.pressureData = serializer.shortarray(input.pressureData);
        input.playerNumber = serializer.i32(input.playerNumber);
        input.controllingPauseMenu = serializer.bool(input.controllingPauseMenu);
        if (serializer.getRevision().getVersion() < 0x210)
            serializer.bool(false);

        return input;
    }

    @Override public int getAllocatedSize() { return Input.BASE_ALLOCATION_SIZE; }
}
