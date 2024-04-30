package cwlib.structs.things.components.npc;

import org.joml.Vector3f;
import org.joml.Vector4f;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class Input implements Serializable
{
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

    @Override
    public void serialize(Serializer serializer)
    {
        alive = serializer.bool(alive);
        leftStick = serializer.v3(leftStick);
        rightStick = serializer.v3(rightStick);
        buttons = serializer.i32(buttons);
        buttonsOld = serializer.i32(buttonsOld);
        sensorData = serializer.shortarray(sensorData);
        sensorDathSmooth = serializer.floatarray(sensorDathSmooth);
        sensorDir = serializer.v4(sensorDir);
        sensorDirOld = serializer.v4(sensorDirOld);
        pressureData = serializer.shortarray(pressureData);
        playerNumber = serializer.i32(playerNumber);
        controllingPauseMenu = serializer.bool(controllingPauseMenu);
        if (serializer.getRevision().getVersion() < 0x210)
            serializer.bool(false);
    }

    @Override
    public int getAllocatedSize()
    {
        return Input.BASE_ALLOCATION_SIZE;
    }
}
