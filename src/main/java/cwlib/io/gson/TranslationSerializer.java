package cwlib.io.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class TranslationSerializer implements JsonSerializer<Matrix4f>, JsonDeserializer<Matrix4f> {
    @Override public Matrix4f deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
        if (je.isJsonArray())
            return jdc.deserialize(je, Matrix4f.class);
        else if (je.isJsonObject()) {
            JsonObject object = je.getAsJsonObject();

            Vector3f translation = new Vector3f();
            if (object.has("translation"))
                translation = jdc.deserialize(object.get("translation"), Vector3f.class);
            
            Vector4f rotation = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
            if (object.has("rotation"))
                rotation = jdc.deserialize(object.get("rotation"), Vector4f.class);
            
            Vector3f scale = new Vector3f(1.0f, 1.0f, 1.0f);
            if (object.has("scale")) 
                scale = jdc.deserialize(object.get("scale"), Vector3f.class);

            return new Matrix4f()
                .identity()
                .translationRotateScale(
                    translation, 
                    new Quaternionf(rotation.x, rotation.y, rotation.z, rotation.w), 
                    scale
                );
        }
        return new Matrix4f().identity();
    }

    @Override public JsonElement serialize(Matrix4f matrix, Type type, JsonSerializationContext jsc) {
        JsonObject object = new JsonObject();

        float[] elements = matrix.get(new float[16]);

        Vector3f translation = new Vector3f(elements[12], elements[13], elements[14]);
        
        Vector3f col0 = new Vector3f(elements[0], elements[1], elements[2]);
        Vector3f col1 = new Vector3f(elements[4], elements[5], elements[6]);
        Vector3f col2 = new Vector3f(elements[8], elements[9], elements[10]);

        boolean canDecompose = true;
        if (col0.dot(col1) != 0.0f) canDecompose = false;
        if (col1.dot(col2) != 0.0f) canDecompose = false;
        if (col0.dot(col2) != 0.0f) canDecompose = false;
        if (!canDecompose)
            return jsc.serialize(matrix, Matrix4f.class);

        Vector3f scale = new Vector3f(col0.length(), col1.length(), col2.length());

        col0 = col0.div(scale.x);
        col1 = col1.div(scale.y);
        col2 = col2.div(scale.z);

        if (col0.cross(col1, new Vector3f()).dot(col2) < 0.0f) {
            col0.mul(-1.0f);
            col1.mul(-1.0f);
            col2.mul(-1.0f);
            scale.mul(-1.0f);
        }

        col0 = col0.normalize();
        col1 = col1.normalize();
        col2 = col2.normalize();

        Vector4f rotation = new Vector4f();
        float[] te = new Matrix4f().set(new Matrix3f(col0, col1, col2)).get(new float[16]);
        float m11 = te[ 0 ], m12 = te[ 4 ], m13 = te[ 8 ],
        m21 = te[ 1 ], m22 = te[ 5 ], m23 = te[ 9 ],
        m31 = te[ 2 ], m32 = te[ 6 ], m33 = te[ 10 ];

        float trace = m11 + m22 + m33;

        if (trace > 0.0f) {
            float s = 2.0f * ((float) Math.sqrt(trace + 1.0f));
            rotation.w = 0.25f * s;
            s = 1.0f / s;
            rotation.x = ( m32 - m23 ) * s;
            rotation.y = ( m13 - m31 ) * s;
            rotation.z =  ( m21 - m12 ) * s;
        } else if (m11 > m22 && m11 > m33) {
            float s = 2.0f * ((float)Math.sqrt(1.0f + m11 - m22 - m33));
            rotation.w = ( m32 - m23 ) / s;
            rotation.x = 0.25f * s;
            rotation.y = ( m12 + m21 ) / s;
            rotation.z = ( m13 + m31 ) / s;
        } else if (m22 > m33) {
            float s = 2.0f * ((float)Math.sqrt(1.0f + m22 - m11 - m33));
            rotation.w = ( m13 - m31 ) / s;
            rotation.x = ( m12 + m21 ) / s;
            rotation.y =  0.25f * s;
            rotation.z = ( m23 + m32 ) / s;
        } else {
            float s = 2.0f * ((float)Math.sqrt(1.0f + m33 - m11 - m22 ));
            rotation.w = ( m21 - m12 ) / s;
            rotation.x = ( m13 + m31 ) / s;
            rotation.y = ( m23 + m32 ) / s;
            rotation.z = 0.25f * s;
        }

        if (rotation.w < 0.0f)
            rotation = rotation.negate();
        rotation = rotation.normalize();

        object.add("translation", jsc.serialize(translation));
        object.add("rotation", jsc.serialize(rotation));
        object.add("scale", jsc.serialize(scale));

        return object;
    }
}
