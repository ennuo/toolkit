package cwlib.structs.things.components.world;

import org.joml.Vector4f;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.Revision;

public class MoveCursor implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x100;

    public int selectionFrame;
    public Thing switchThing, selection;
    public Vector4f localPosition;
    public float localAngle;
    public Vector4f moveAngle, lastRCStart, lastRCDir, currRCStart, currRCDir;
    public float lastRoll, currRoll;
    public boolean prevMoveGrabbing, moveGrabbing;
    public float zMovePos;
    public int globalCursorPlayer;
    public Thing cursorPlayerThing;
    public int globalCursorPanel, thingUnderCursor;
    public boolean thingUnderCursorChangedWhileDown;
    public int lastTouchPanel, lastTouchID; // s32

    @Override
    public void serialize(Serializer serializer)
    {
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();

        if (version > 0x3c1)
            selectionFrame = serializer.i32(selectionFrame);
        switchThing = serializer.thing(switchThing);
        selection = serializer.thing(selection);
        localPosition = serializer.v4(localPosition);
        localAngle = serializer.f32(localAngle);

        if (version < 0x3c2)
        {
            serializer.v4(null);
            serializer.v4(null);
        }

        moveAngle = serializer.v4(moveAngle);
        if (version > 0x3c1)
        {
            lastRCStart = serializer.v4(lastRCStart);
            lastRCDir = serializer.v4(lastRCDir);
            currRCStart = serializer.v4(currRCStart);
            currRCDir = serializer.v4(currRCDir);
            lastRoll = serializer.f32(lastRoll);
            currRoll = serializer.f32(currRoll);
        }

        prevMoveGrabbing = serializer.bool(prevMoveGrabbing);
        moveGrabbing = serializer.bool(moveGrabbing);

        if (version < 0x3c2)
        {
            serializer.v4(null);
            serializer.f32(0);
        }
        else zMovePos = serializer.f32(zMovePos);

        if (revision.isVita())
        {
            int vita = revision.getBranchRevision();

            if (vita >= 0x1b) // 0x3d5
                globalCursorPlayer = serializer.i32(globalCursorPlayer);
            if (vita >= 0x42) // 0x3e2
                cursorPlayerThing = serializer.thing(cursorPlayerThing);
            if (vita >= 0x6d)
                globalCursorPanel = serializer.s32(globalCursorPanel);

            if (vita >= 0x82)
            {
                thingUnderCursor = serializer.i32(thingUnderCursor);
                thingUnderCursorChangedWhileDown =
                    serializer.bool(thingUnderCursorChangedWhileDown);
                lastTouchPanel = serializer.i32(lastTouchPanel);
                lastTouchID = serializer.s32(lastTouchID);
            }
        }
    }

    @Override
    public int getAllocatedSize()
    {
        return MoveCursor.BASE_ALLOCATION_SIZE;
    }
}
