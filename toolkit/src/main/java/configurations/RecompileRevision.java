package configurations;

import com.google.gson.annotations.JsonAdapter;

import cwlib.io.gson.RevisionSerializer;
import cwlib.types.data.Revision;

public class RecompileRevision 
{
    public final String name;

    @JsonAdapter(RevisionSerializer.class)
    public final Revision revision;

    public RecompileRevision(String name, Revision revision)
    {
        this.name = name;
        this.revision = revision;
    }
}
