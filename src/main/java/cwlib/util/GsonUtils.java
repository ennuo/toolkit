package cwlib.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import cwlib.io.gson.GUIDSerializer;
import cwlib.io.gson.Matrix4fSerializer;
import cwlib.io.gson.NetworkOnlineIDSerializer;
import cwlib.io.gson.NetworkPlayerIDSerializer;
import cwlib.io.gson.PatchSerializer;
import cwlib.io.gson.SHA1Serializer;
import cwlib.io.gson.SlotIDSerializer;
import cwlib.io.gson.Vector2fSerializer;
import cwlib.io.gson.Vector3fSerializer;
import cwlib.io.gson.Vector4fSerializer;
import cwlib.types.data.NetworkOnlineID;
import cwlib.types.data.NetworkPlayerID;
import cwlib.structs.slot.SlotID;
import cwlib.types.data.GUID;
import cwlib.types.data.SHA1;
import cwlib.types.mods.patches.ModPatch;

public final class GsonUtils {
    /**
     * Default GSON serializer object with type-adapters
     * pre-set.
     */
    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .serializeSpecialFloatingPointValues()
        .registerTypeAdapter(ModPatch.class, new PatchSerializer())
        .registerTypeAdapter(Vector2f.class, new Vector2fSerializer())
        .registerTypeAdapter(Vector3f.class, new Vector3fSerializer())
        .registerTypeAdapter(Vector4f.class, new Vector4fSerializer())
        .registerTypeAdapter(Matrix4f.class, new Matrix4fSerializer())
        .registerTypeAdapter(SHA1.class, new SHA1Serializer())
        .registerTypeAdapter(SlotID.class, new SlotIDSerializer())
        .registerTypeAdapter(GUID.class, new GUIDSerializer())
        .registerTypeAdapter(NetworkPlayerID.class, new NetworkPlayerIDSerializer())
        .registerTypeAdapter(NetworkOnlineID.class, new NetworkOnlineIDSerializer())
        .create();

    /**
     * Deserializes a JSON string to an object.
     * @param <T> Type to deserialize
     * @param json JSON object to deserialize
     * @param clazz Class to deserialize
     * @return Deserialized object
     */
    public static <T> T fromJSON(String json, Class<T> clazz) { 
        return GSON.fromJson(json, clazz);
    }

    /**
     * Serializes an object to a JSON string.
     * @param object Object to serialize
     * @return Serialized JSON string
     */
    public static String toJSON(Object object) {
        return GSON.toJson(object);
    }
}
