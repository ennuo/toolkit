package cwlib.structs.things.components.world;

import org.joml.Vector4f;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.Revision;

public class MoveCursor implements Serializable {
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

    @SuppressWarnings("unchecked")
    @Override public MoveCursor serialize(Serializer serializer, Serializable structure) {
        MoveCursor cursor = (structure == null) ? new MoveCursor() : (MoveCursor) structure;

        Revision revision = serializer.getRevision();
        int version = revision.getVersion();

        if (version > 0x3c1)
            cursor.selectionFrame = serializer.i32(cursor.selectionFrame);
        cursor.switchThing = serializer.thing(cursor.switchThing);
        cursor.selection = serializer.thing(cursor.selection);
        cursor.localPosition = serializer.v4(cursor.localPosition);
        cursor.localAngle = serializer.f32(cursor.localAngle);

        if (version < 0x3c2) {
            serializer.v4(null);
            serializer.v4(null);
        }

        cursor.moveAngle = serializer.v4(cursor.moveAngle);
        if (version > 0x3c1) {
            cursor.lastRCStart = serializer.v4(cursor.lastRCStart);
            cursor.lastRCDir = serializer.v4(cursor.lastRCDir);
            cursor.currRCStart = serializer.v4(cursor.currRCStart);
            cursor.currRCDir = serializer.v4(cursor.currRCDir);
            cursor.lastRoll = serializer.f32(cursor.lastRoll);
            cursor.currRoll = serializer.f32(cursor.currRoll);
        }

        cursor.prevMoveGrabbing = serializer.bool(cursor.prevMoveGrabbing);
        cursor.moveGrabbing = serializer.bool(cursor.moveGrabbing);

        if (version < 0x3c2) {
            serializer.v4(null);
            serializer.f32(0);
        } else cursor.zMovePos = serializer.f32(cursor.zMovePos);

        if (revision.isVita()) {
            int vita = revision.getBranchRevision();

            if (vita >= 0x1b)
                cursor.globalCursorPlayer = serializer.i32(cursor.globalCursorPlayer);
            if (vita >= 0x42)
                cursor.cursorPlayerThing = serializer.thing(cursor.cursorPlayerThing);

            if (vita >= 0x82) {
                cursor.globalCursorPanel = serializer.i32(cursor.globalCursorPanel);
                cursor.thingUnderCursor = serializer.i32(cursor.thingUnderCursor);
                cursor.thingUnderCursorChangedWhileDown = serializer.bool(cursor.thingUnderCursorChangedWhileDown);
                cursor.lastTouchPanel = serializer.i32(cursor.lastTouchPanel);
                cursor.lastTouchID = serializer.s32(cursor.lastTouchID);
            }
        }

        return cursor;
    }

    @Override public int getAllocatedSize() { return MoveCursor.BASE_ALLOCATION_SIZE; }
}
