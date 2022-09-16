package cwlib.structs.inventory;

import com.google.gson.annotations.JsonAdapter;

import cwlib.io.Serializable;
import cwlib.io.gson.CreationHistorySerializer;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;

/**
 * Represents all the users who have had
 * some form of contribution to this resource.
 */
@JsonAdapter(CreationHistorySerializer.class)
public class CreationHistory implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x4;

    public String[] creators;

    @SuppressWarnings("unchecked")
    @Override public CreationHistory serialize(Serializer serializer, Serializable structure) {
        CreationHistory history = (structure == null) ? new CreationHistory() : (CreationHistory) structure;

        boolean isFixed = serializer.getRevision().getVersion() > 0x37c;
        if (serializer.isWriting()) {
            MemoryOutputStream stream = serializer.getOutput();
            if (history.creators != null) {
                stream.i32(history.creators.length);
                for (String editor : history.creators) {
                    if (isFixed) stream.str(editor, 0x14);
                    else stream.wstr(editor);
                }
            }
            else stream.i32(0);
            return history;
        } 

        MemoryInputStream stream = serializer.getInput();
        history.creators = new String[stream.i32()];
        for (int i = 0; i < history.creators.length; ++i) {
            if (isFixed) history.creators[i] = stream.str(0x14);
            else history.creators[i] = stream.wstr();
        }
        return history;
    }

    @Override public int getAllocatedSize() { 
        int size = BASE_ALLOCATION_SIZE;
        if (this.creators != null)
            for (String editor : this.creators)
                size += ((editor.length() * 2) + 4);
        return size;
    }
}
