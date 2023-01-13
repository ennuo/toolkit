package cwlib.types.databases;

import cwlib.io.streams.MemoryInputStream;
import cwlib.types.data.GUID;
import cwlib.types.databases.RemapDB.RemapDBRow;
import java.io.File;
import java.util.Arrays;
import java.util.Iterator;

public class RemapDB implements Iterable<RemapDBRow> {
    public static class RemapDBRow {
        private String path;
        private GUID from;
        private GUID to;
        
        public String getPath() { return this.path; }
        public GUID getFrom() { return this.from; }
        public GUID getTo() { return this.to; }
    }
    
    private int revision;
    private RemapDBRow[] rows;
    
    public RemapDB(File file) {
        MemoryInputStream stream = new MemoryInputStream(file.getAbsolutePath());
        this.revision = stream.i32();
        int count = stream.i32();
        this.rows = new RemapDBRow[count];
        for (int i = 0; i < count; ++i) {
            RemapDBRow row = new RemapDBRow();
            row.to = stream.guid();
            row.from = stream.guid();
            rows[i] = row;
        }
        count = stream.i32();
        for (int i = 0; i < count; ++i) {
            this.rows[i].path = stream.str();
            stream.guid(); // Same as row.to
        }
    }
    
    public int getRevision() { return this.revision; }
    public RemapDBRow[] getRows() { return this.rows; }
    
    @Override public Iterator<RemapDBRow> iterator() {
        return Arrays.stream(this.rows).iterator();
    }
}
