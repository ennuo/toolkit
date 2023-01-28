package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;

public class PTransition implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    public int colorIndex;

    @GsonRevision(lbp3=true, min=0x1a)
    public String label;

    @GsonRevision(lbp3=true, min=0x19)
    public boolean enabled;

    @GsonRevision(lbp3=true, min=0x17d)
    public boolean showColor, playAudio;

    @SuppressWarnings("unchecked")
    @Override public PTransition serialize(Serializer serializer, Serializable structure) {
        PTransition transition = (structure == null) ? new PTransition() : (PTransition) structure;
        int subVersion = serializer.getRevision().getSubVersion();

        transition.colorIndex = serializer.s32(transition.colorIndex);

        if (subVersion <= 0xf)
            serializer.i32(0);

        if (subVersion >= 0x1a)
            transition.label = serializer.wstr(transition.label);

        if (subVersion > 0x18)
            transition.enabled = serializer.bool(transition.enabled);

        if (subVersion >= 0x14e && subVersion < 0x156)
            serializer.i32(0);

        if (subVersion > 0x17c) {
            transition.showColor = serializer.bool(transition.showColor);
            transition.playAudio = serializer.bool(transition.playAudio);
        }

        return transition;
    }


    @Override public int getAllocatedSize() {
        int size = PTransition.BASE_ALLOCATION_SIZE;
        if (this.label != null)
            size += (this.label.length() * 0x2);
        return size;
    }
}
