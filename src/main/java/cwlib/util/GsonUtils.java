package cwlib.util;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.HashSet;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import cwlib.io.gson.GsonRevision;
import cwlib.io.gson.GsonRevisions;
import cwlib.io.gson.Matrix4fSerializer;
import cwlib.io.gson.Vector2fSerializer;
import cwlib.io.gson.Vector3fSerializer;
import cwlib.io.gson.Vector4fSerializer;
import cwlib.enums.Branch;
import cwlib.types.data.Revision;
import cwlib.structs.things.Thing;

public final class GsonUtils {
    public static Revision REVISION = new Revision(Branch.MIZUKI.getHead(), Branch.MIZUKI.getID(), Branch.MIZUKI.getRevision());
    public static final HashMap<Integer, Thing> THINGS = new HashMap<>();
    public static final HashSet<Thing> UNIQUE_THINGS = new HashSet<>();


    /**
     * Default GSON serializer object with type-adapters
     * pre-set.
     */
    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .serializeSpecialFloatingPointValues()
        .enableComplexMapKeySerialization()
        .serializeNulls()
        .setExclusionStrategies(new ExclusionStrategy() {
            @Override public boolean shouldSkipField(FieldAttributes field) {
                boolean skip = false;
                
                if (field.getAnnotation(GsonRevision.class) != null) {
                    GsonRevision revision = field.getAnnotation(GsonRevision.class);
                    int head = (revision.lbp3()) ? REVISION.getSubVersion() : REVISION.getVersion();
                    
                    if (revision.branch() != -1 && REVISION.getBranchID() != revision.branch()) skip = true;
                    if (revision.max() != -1 && head > revision.max()) skip = true;
                    if (revision.min() != -1 && head < revision.min()) skip = true;
                }

                if (field.getAnnotation(GsonRevisions.class) != null) {
                    GsonRevision[] revisions = field.getAnnotation(GsonRevisions.class).value();
                    boolean anyTrue = false;
                    for (GsonRevision revision : revisions) {
                        int head = (revision.lbp3()) ? REVISION.getSubVersion() : REVISION.getVersion();

                        boolean max = ((revision.max() == -1) || (revision.max() >= head));
                        boolean min = ((revision.min() == -1) || (revision.min() <= head));
                        boolean branch = ((revision.branch() == -1) || (revision.branch() == REVISION.getBranchID()));

                        if (max && min && branch) anyTrue = true;
                    }
                    skip = !anyTrue;
                }

                return skip;
            }
            
            @Override public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }   
        })
        .registerTypeAdapter(Vector2f.class, new Vector2fSerializer())
        .registerTypeAdapter(Vector3f.class, new Vector3fSerializer())
        .registerTypeAdapter(Vector4f.class, new Vector4fSerializer())
        .registerTypeAdapter(Matrix4f.class, new Matrix4fSerializer())
        .create();

    /**
     * Deserializes a JSON string to an object.
     * @param <T> Type to deserialize
     * @param json JSON object to deserialize
     * @param clazz Class to deserialize
     * @return Deserialized object
     */
    public static <T> T fromJSON(String json, Class<T> clazz) { 
        THINGS.clear();
        UNIQUE_THINGS.clear();
        return GSON.fromJson(json, clazz);
    }

    /**
     * Serializes an object to a JSON string.
     * @param object Object to serialize
     * @return Serialized JSON string
     */
    public static String toJSON(Object object) {
        THINGS.clear();
        UNIQUE_THINGS.clear();
        return GSON.toJson(object);
    }
}
