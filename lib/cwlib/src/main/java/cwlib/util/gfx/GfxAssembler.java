package cwlib.util.gfx;

import cwlib.CwlibConfiguration;
import cwlib.enums.BoxType;
import cwlib.enums.BrdfPort;
import cwlib.enums.CompressionFlags;
import cwlib.enums.GameShader;
import cwlib.enums.GfxMaterialFlags;
import cwlib.enums.ResourceType;
import cwlib.resources.RGfxMaterial;
import cwlib.singleton.ResourceSystem;
import cwlib.structs.gmat.MaterialBox;
import cwlib.structs.gmat.MaterialParameterAnimation;
import cwlib.structs.gmat.MaterialWire;
import cwlib.types.SerializedResource;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;
import cwlib.util.FileIO;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import javax.management.RuntimeErrorException;
import javax.print.attribute.HashPrintJobAttributeSet;

import org.joml.Vector3f;
import org.joml.Vector4f;

public class GfxAssembler
{
    public static String BRDF_CG = FileIO.getResourceFileAsString("/shaders/templates/cg/brdf" +
                                                                  ".cg");

    public static String BRDF_VITA_CG = FileIO.getResourceFileAsString("/shaders/templates/cg/brdf_vita.cg");


    public static String BRDF_GLSL = FileIO.getResourceFileAsString("/shaders/templates/glsl" +
                                                                    "/brdf" +
                                                                    ".fs");
    public static String BAKE_GLSL = FileIO.getResourceFileAsString("/shaders/templates/glsl" +
                                                                    "/bake" +
                                                                    ".fs");
    public static String ALPHA_BAKE_GLSL = FileIO.getResourceFileAsString("/shaders/templates" +
                                                                          "/glsl/alphabake.fs");

    public static HashMap<MaterialBox, Variable> LOOKUP = new HashMap<>();

    public static boolean USE_NORMAL_MAPS = false;
    public static boolean IS_GLSL = false;
    public static boolean IS_BAKER = false;
    public static boolean IS_BLENDER = false;
    public static int CURRENT_ATTRIBUTE = 0;

    public static final Vector4f ONE_W = new Vector4f();
    public static final Vector3f ZERO_3 = new Vector3f().zero();
    public static final Vector3f ONE = new Vector3f(1.0f, 1.0f, 1.0f);


    public static class Variable
    {
        public final String value;
        public final int type;

        public Variable(String value, int type)
        {
            this.value = value;
            this.type = type;
        }

        @Override
        public String toString()
        {
            return this.value;
        }
    }

    public static final String[] COMPONENTS = { ".x", ".y", ".z", ".w" };

    public static final Variable getWithSwizzle(StringBuilder shader, RGfxMaterial gfx,
                                                MaterialBox box, int port, int type)
    {
        MaterialWire wire = gfx.getWireConnectedToPort(box, port);
        if (wire == null) return null;
        Variable variable = resolve(shader, gfx, gfx.boxes.get(wire.boxFrom), type,
            wire.portFrom);

        String swizzle = new String(wire.swizzle).replaceAll("\0", "");
        if (swizzle.length() != 0)
            return new Variable(variable.value + "." + swizzle, swizzle.length());

        return variable;
    }

    public static final boolean isStaticSampler(RGfxMaterial material, int sampler)
    {

        MaterialBox last = null;
        for (int i = 0; i < material.boxes.size(); ++i)
        {
            var box = material.boxes.get(i);
            if (box.type != BoxType.TEXTURE_SAMPLE) continue;
            var params = box.getParameters();
            if (params[MaterialBox.TEXTURE_SAMPLE_INDEX] == sampler) continue;

            boolean hasAnim1 = !box.anim.getName().isEmpty();
            boolean hasAnim2 = !box.anim2.getName().isEmpty();

            if (hasAnim1 || hasAnim2 || box.subType == 1) return false;
            if (material.hasWiredInputs(i)) return false;

            if (last != null)
            {
                var oparams = last.getParameters();

                if (oparams[MaterialBox.TEXTURE_SAMPLE_OFFSET_X] != params[MaterialBox.TEXTURE_SAMPLE_OFFSET_X])
                    return false;
                if (oparams[MaterialBox.TEXTURE_SAMPLE_OFFSET_Y] != params[MaterialBox.TEXTURE_SAMPLE_OFFSET_Y])
                    return false;
                if (oparams[MaterialBox.TEXTURE_SAMPLE_SCALE_X] != params[MaterialBox.TEXTURE_SAMPLE_SCALE_X])
                    return false;
                if (oparams[MaterialBox.TEXTURE_SAMPLE_SCALE_Y] != params[MaterialBox.TEXTURE_SAMPLE_SCALE_Y])
                    return false;
            }

            last = box;
        }



        return true;
    }

    public static final Variable resolve(StringBuilder shader, RGfxMaterial gmat, MaterialBox box
        , int type, int to_port)
    {
        if (LOOKUP.containsKey(box))
            return LOOKUP.get(box);

        int index = CURRENT_ATTRIBUTE++;
        int[] params = box.getParameters();
        int returnType = 4;
        String variableName, assignment;
        switch (box.type)
        {
            case BoxType.OUTPUT: { throw new RuntimeException("Why?"); }
            case BoxType.TEXTURE_SAMPLE:
            {
                String texVar = "s" + params[5];
                variableName = "smp" + index;
                String channel = params[4] == 1 ? "zw" : "xy";
                
                if (type == BrdfPort.REFLECT)
                {

                    if (IS_GLSL)
                        assignment = String.format("SAMPLE_2D_BIAS(%s, iReflectCoord," +
                                                   " " +
                                                   "ReflectionBlur)", texVar);
                    else
                        assignment = String.format("SAMPLE_2D_BIAS(%s, iUV, " +
                                                   "ReflectionBlur)",
                            texVar);

                    break;
                }

                String uv = "iUV." + channel;


                boolean hasAnim1 = !box.anim.getName().isEmpty();
                boolean hasAnim2 = !box.anim2.getName().isEmpty();

                if (box.subType == 0)
                {
                    if (hasAnim1 && hasAnim2)
                    {
                        uv = String.format(
                            "float2(dot(%s.xyz, float3(%s, 1.0)), dot(%s.xyz, " +
                            "float3(%s, 1.0)" +
                            "))",
                            "t" + box.anim.getName(),
                            uv,
                            "t" + box.anim2.getName(),
                            uv
                        );
                    }
                    else if (hasAnim1)
                    {
                        uv = String.format("((%s - %s.zw) * %s.xy)",
                            uv, "t" + box.anim.getName(), "t" + box.anim.getName()
                        );
                    }
                    else // if (!gmat.useVitaShaderSource || !isStaticSampler(gmat, params[MaterialBox.TEXTURE_SAMPLE_INDEX]))
                    {
                        float sx = Float.intBitsToFloat(params[0]);
                        float sy = Float.intBitsToFloat(params[1]);
                        if (sx != 1.0f || sy != 1.0f)
                            uv = String.format(Locale.ROOT, "(%s * float2(%f, %f))"
                                , uv, sx, sy);
                        float ox = Float.intBitsToFloat(params[2]);
                        float oy = Float.intBitsToFloat(params[3]);
                        if (ox != 0.0f || oy != 0.0f)
                            uv += String.format(Locale.ROOT, " + float2(%f, %f)",
                                ox, oy);

                        Variable add = getWithSwizzle(shader, gmat, box, 0, type);
                        if (add != null)
                            uv = String.format("(%s) + %s", uv,
                                (add.value.indexOf(".") == -1 && add.type != 1) ?
                                    add.value + ".xy" : add.value);
                        Variable scale = getWithSwizzle(shader, gmat, box, 1, type);
                        if (scale != null)
                            uv = String.format("(%s) * %s", uv,
                                (scale.value.indexOf(".") == -1 && scale.type != 1) ?
                                    scale.value + ".xy" : scale.value);
                        Variable sub = getWithSwizzle(shader, gmat, box, 2, type);
                        if (sub != null)
                            uv = String.format("(%s) - %s", uv,
                                (sub.value.indexOf(".") == -1 && sub.type != 1) ?
                                    sub.value + ".xy" : sub.value);
                    }
                }
                else if (box.subType == 1)
                {
                    String x = String.format("dot(float3(%s, 1.0), float3(%f, %f, %f))"
                        , uv,
                        Float.intBitsToFloat(params[0]),
                        Float.intBitsToFloat(params[1]),
                        Float.intBitsToFloat(params[2]));
                    if (hasAnim1)
                    {
                        x = String.format("dot(float3(%s, 1.0), %s.xyz)", uv,
                            "t" + box.anim.getName());
                    }

                    String y = String.format("dot(float3(%s, 1.0), float3(%f, %f, %f))"
                        , uv,
                        Float.intBitsToFloat(params[3]),
                        Float.intBitsToFloat(params[6]),
                        Float.intBitsToFloat(params[7]));
                    if (hasAnim2)
                    {
                        y = String.format("dot(float3(%s, 1.0), %s.xyz)", uv,
                            "t" + box.anim2.getName());
                    }

                    uv = String.format("float2(%s, %s)", x, y);
                }

                assignment = String.format("SAMPLE_2D(%s, %s)", texVar, uv);


                if (type == BrdfPort.FUZZ)
                {
                    shader.append(String.format("\tfloat4 %s = float4(%s.x, SAMPLE_2D" +
                                                "(%s, iUV.zw)" +
                                                ".yz, 0.0);\n", variableName,
                        assignment, texVar));
                    return new Variable(variableName, 4);
                }

                break;
            }
            case BoxType.THING_COLOR:
                return new Variable("iColor", 4);
            case BoxType.COLOR:
            {
                variableName = "col" + index;
                if (box.anim.getName().isEmpty())
                {
                    assignment = String.format(
                        Locale.ROOT, "float4(%f, %f, %f, %f)",
                        Float.intBitsToFloat(params[0]),
                        Float.intBitsToFloat(params[1]),
                        Float.intBitsToFloat(params[2]),
                        Float.intBitsToFloat(params[3])
                    );
                }
                else assignment = "t" + box.anim.getName();

                break;
            }
            case BoxType.CONSTANT:
                return new Variable(String.format(Locale.ROOT, "%f",
                    Float.intBitsToFloat(params[0])), 1);
            case BoxType.CONSTANT2:
            {
                return new Variable(String.format(Locale.ROOT, "float2(%f, %f)",
                    Float.intBitsToFloat(params[0]),
                    Float.intBitsToFloat(params[1])), 2);
            }
            case BoxType.CONSTANT3:
            {
                return new Variable(String.format(Locale.ROOT, "float3(%f, %f, %f)",
                    Float.intBitsToFloat(params[0]),
                    Float.intBitsToFloat(params[1]),
                    Float.intBitsToFloat(params[2])), 3);
            }
            case BoxType.CONSTANT4:
            {
                return new Variable(String.format(Locale.ROOT, "float4(%f, %f, %f, %f)",
                    Float.intBitsToFloat(params[0]),
                    Float.intBitsToFloat(params[1]),
                    Float.intBitsToFloat(params[2]),
                    Float.intBitsToFloat(params[3])), 4);
            }
            // 8
            case BoxType.MULTIPLY_ADD:
            {
                MaterialBox node = gmat.getBoxConnectedToPort(box, 0);
                if (node == null)
                    throw new RuntimeException("MAD node is supposed to take one input!");

                Variable input = getWithSwizzle(shader, gmat, box, 0, type);
                returnType = input.type;

                variableName = "mad" + index;
                assignment = String.format(Locale.ROOT, "((%s * %f) + %f)", input,
                    Float.intBitsToFloat(params[0]),
                    Float.intBitsToFloat(params[1]));
                break;

            }
            case BoxType.MULTIPLY:
            {
                Variable l = getWithSwizzle(shader, gmat, box, 0, type);
                Variable r = getWithSwizzle(shader, gmat, box, 1, type);

                if (l == null || r == null)
                    throw new RuntimeException("Multiply node is supposed to take two " +
                                               "inputs!");

                returnType = l.type;
                if (r.type > l.type)
                    returnType = r.type;

                variableName = "mul" + index;
                if (type == BrdfPort.BUMP)
                    assignment = String.format("((%s - 0.5) + %s)", l, r);
                else
                    assignment = String.format("(%s * %s)", l, r);

                break;
            }
            case BoxType.ADD:
            {
                Variable l = getWithSwizzle(shader, gmat, box, 0, type);
                Variable r = getWithSwizzle(shader, gmat, box, 1, type);
                if (l == null || r == null)
                    throw new RuntimeException("Add node is supposed to take two " +
                                               "inputs!");

                returnType = l.type;
                if (r.type > l.type)
                    returnType = r.type;

                variableName = "sum" + index;

                assignment = String.format("(%s + %s)", l, r);

                break;
            }
            case BoxType.MIX:
            {
                Variable l = getWithSwizzle(shader, gmat, box, 0, type);
                Variable r = getWithSwizzle(shader, gmat, box, 1, type);
                if (l == null || r == null)
                    throw new RuntimeException("Mix node is supposed to take two " +
                                               "inputs!");

                returnType = l.type;
                if (r.type > l.type)
                    returnType = r.type;

                String f = String.format(Locale.ROOT, "%f",
                    Float.intBitsToFloat(params[0]));
                Variable w = getWithSwizzle(shader, gmat, box, 2, type);
                if (w != null) f = w.toString();


                variableName = "mix" + index;
                assignment = String.format("(((%s - %s) * %s) + %s)", r, l, f, l);

                break;
            }
            case BoxType.MAKE_FLOAT2:
            case BoxType.MAKE_FLOAT3:
            case BoxType.MAKE_FLOAT4:
            {
                int count = box.type - 11;
                String[] inputs = new String[count];
                for (int i = 0; i < inputs.length; ++i)
                {
                    Variable variable = getWithSwizzle(shader, gmat, box, i, type);
                    if (variable != null)
                    {
                        inputs[i] = variable.value;
                        if (variable.type == 3)
                            inputs[i] = String.format("((%s.x + %s.y + %s.z) / 3.0)", variable.value, variable.value, variable.value);
                        if (variable.type == 4)
                            inputs[i] = String.format("luminance(%s)", variable.value);
                    }
                    else
                        inputs[i] = String.format(Locale.ROOT, "%f",
                            Float.intBitsToFloat(params[i]));
                }

                return new Variable(
                    String.format("float%d(%s)", count, String.join(", ", inputs)),
                    count
                );
            }
            case BoxType.BLEND:
            {
                Variable input1 = getWithSwizzle(shader, gmat, box, 0, type);
                Variable input2 = getWithSwizzle(shader, gmat, box, 1, type);
                Variable input3 = getWithSwizzle(shader, gmat, box, 2, type);

                returnType = input1.type;
                if (input2.type > returnType)
                    returnType = input2.type;
                if (input3.type > returnType)
                    returnType = input3.type;

                variableName = "blnd" + index;

                assignment = String.format("((%s - %s) * saturate(%s.x * 100.0 - 15.0) + " +
                                           "%s)",
                    input2, input1, input3, input1);

                break;

                // (INPUT2 - INPUT1) * saturate(INPUT3.X * 100.0f - 15.0f) + INPUT1

                // IS 15.0F BASED ON PARAMETERS?
                // THE ONE USED HERE WAS
                // PARAM[0] = 0.15
                // PARAM[1] = 0.16


                // IS THIS BLEND MATERIAL?
                // PARAM[0] = LOWER CURVE
                // PARAM[1] = UPPER CURVE?

                // INPUT[0] = MAT 1
                // INPUT[1] = MAT 2
                // INPUT[2] = MASK
                // return v;
            }
            case BoxType.EXPONENT:
            {
                MaterialBox node = gmat.getBoxConnectedToPort(box, 0);
                if (node == null)
                    throw new RuntimeException("MAD node is supposed to take one input!");

                Variable input = getWithSwizzle(shader, gmat, box, 0, type);
                returnType = input.type;

                variableName = "ex" + index;
                assignment = String.format("pow(%s, %s)",
                    input,
                    Float.intBitsToFloat(params[0])
                );

                break;
            }


            // Don't want to cache these into variables since it's basically
            // just a swizzle operation.
            case BoxType.BL_SEPARATE_XYZ:
            {
                Variable input = getWithSwizzle(shader, gmat, box, 0, type);
                return new Variable(String.format("%s%s", input.toString(), COMPONENTS[to_port]), 1);
            }

            case BoxType.BL_TEXTURE_COORDINATE:
            {
                final int NORMAL = 1;
                final int UV = 2;
                final int WINDOW = 5;

                returnType = 3;

                // We don't want to cache the variables for this node
                switch (to_port)
                {
                    case NORMAL: return new Variable("iNormal", 3);
                    case UV: return new Variable("float3(iUV.xy, 0.0)", 3);
                    case WINDOW: return new Variable("((float3(0.0, 1.0, 0.0) - (iPosition.xyz * float3(0.00078125, 0.00138889, 1.0))) * (float3(2.0, 2.0, 1.0) * float3(1.0, 0.5625, 1.0)))", 3);
                }

                throw new RuntimeException("Unsupported texture coordinate port! " + to_port);
            }

            case BoxType.BL_MAPPING:
            {
                final int POINT = 0;
                int mappingType = params[0];
                
                if (mappingType != POINT)
                    throw new RuntimeException("Only mapping types of POINT are supported!");

                returnType = 3;

                String vector = null;
                String location = null;
                String rotation = null;
                String scale = null;

                Vector3f defaultVector = gmat.getFloat3(params[1]);
                Vector3f defaultLocation = gmat.getFloat3(params[2]);
                Vector3f defaultRotation = gmat.getFloat3(params[3]);
                Vector3f defaultScale = gmat.getFloat3(params[4]);

                Variable vectorInput = getWithSwizzle(shader, gmat, box, 0, type);
                Variable locationalInput = getWithSwizzle(shader, gmat, box, 1, type);
                Variable rotationalInput = getWithSwizzle(shader, gmat, box, 2, type);
                Variable scaleInput = getWithSwizzle(shader, gmat, box, 3, type);

                vector = vectorInput != null ? vectorInput.toString() : String.format(Locale.ROOT, "float3(%f, %f, %f)", defaultVector.x, defaultVector.y, defaultVector.z);
                location = locationalInput != null ? locationalInput.toString() : String.format(Locale.ROOT, "float3(%f, %f, %f)", defaultLocation.x, defaultLocation.y, defaultLocation.z);
                rotation = rotationalInput != null ? rotationalInput.toString() : String.format(Locale.ROOT, "float3(%f, %f, %f)", defaultRotation.x, defaultRotation.y, defaultRotation.z);
                scale = scaleInput != null ? scaleInput.toString() : String.format(Locale.ROOT, "float3(%f, %f, %f)", defaultScale.x, defaultScale.y, defaultScale.z);


                variableName = "mapping" + index;

                // rotation needs to be flipped?

    
                // scale then rotate then translate

                // If we have any rotation, we'll use a matrix to transform the vector,
                // otherwhise we'll just do a traditional mad operation
                if (rotationalInput == null && defaultRotation.equals(ZERO_3))
                {
                    if (scaleInput == null && locationalInput == null)
                    {
                        if (defaultScale.equals(ONE) && defaultLocation.equals(ZERO_3))
                            assignment = vectorInput.toString();
                        else if (defaultScale.equals(ONE))
                            assignment = String.format("(%s + %s)", vector, location);
                        else
                            assignment = String.format("(%s * %s)", vector, scale);
                    }
                    else if (locationalInput == null)
                    {
                        if (defaultLocation.equals(ZERO_3))
                            assignment = String.format("(%s * %s)", vector, scale);
                        else
                            assignment = String.format("((%s * %s) + %s)", vector, scale, location);
                    }
                    // scale input is null
                    else
                    {
                        // location input 100% exists
                        // scale could be 1

                        if (defaultScale.equals(ONE))
                            assignment = String.format("(%s + %s)", vector, location);
                        else
                            assignment = String.format("(%s * %s) + %s", vector, scale, location);

                    }
                }
                else
                {
                    assignment = String.format("(rotate((%s * %s), %s) + %s)", vector, scale, rotation, location);
                }

                break;
            }

            //
            //
            // Blender nodes
            //
            //

            case BoxType.BL_TEXTURE_SAMPLE:
            {
                String sampler = "s" + params[0];                

                String vector = "iUV.xy";
                Variable input = getWithSwizzle(shader, gmat, box, 0, type);
                if (input != null)
                {
                    vector = input.toString();
                    if (input.type > 2)
                        vector = String.format("%s.xy", vector);
                    else if (input.type == 1)
                        vector = String.format("float2(%s)", vector);
                }

                assignment = String.format("SAMPLE_2D(%s, %s)", sampler, vector);
                variableName = "smp" + index;

                break;
            }

            case BoxType.BL_MIX:
            {
                final int MIX = 0;
                final int MULTIPLY = 2;
                final int ADD = 7;


                int blendType = params[0];
                int blendFlags = params[1];                
                float defaultFactor = Float.intBitsToFloat(params[2]);
                Vector4f defaultA = gmat.getFloat4(params[3]);
                Vector4f defaultB = gmat.getFloat4(params[4]);

                Variable factorInput = getWithSwizzle(shader, gmat, box, 0, type);
                Variable aInput = getWithSwizzle(shader, gmat, box, 1, type);
                Variable bInput = getWithSwizzle(shader, gmat, box, 2, type);

                if (factorInput != null && factorInput.type != 1)
                    throw new RuntimeException("factor input must be 1!!!");

                String a = aInput != null ? aInput.toString() : String.format(Locale.ROOT, "float4(%f,%f,%f,%f)", defaultA.x, defaultA.y, defaultA.z, defaultA.w);
                String b = bInput != null ? bInput.toString() : String.format(Locale.ROOT, "float4(%f,%f,%f,%f)", defaultB.x, defaultB.y, defaultB.z, defaultB.w);
                String factor = factorInput != null ? factorInput.toString() : Float.toString(defaultFactor);

                if (aInput != null && aInput.type != 4)
                {
                    if (aInput.type != 1) throw new RuntimeException("invalid cast");
                    a = String.format("float4(%s)", a);
                }

                if (bInput != null && bInput.type != 4)
                {
                    if (bInput.type != 1) throw new RuntimeException("invalid cast");
                    b = String.format("float4(%s)", b);
                }

                switch (blendType)
                {
                    case MIX:
                    {
                        variableName = "mix"+index;
                        assignment = String.format("((1.0 - %s) * %s + %s * %s)", factor, a, factor, b);
                        break;
                    }
                    case MULTIPLY:
                    {
                        variableName = "mul"+index;
                        assignment = String.format("(%s * ((1.0 - %s) + %s * %s))", 
                            a, factor, factor, b
                        );
                        break;
                    }
                    case ADD:
                    {
                        variableName = "add"+index;
                        assignment = String.format("(%s + (%s * %s))", 
                            a, factor, b
                        );

                        break;
                    }
                    default:
                        throw new RuntimeException("Unsupported blend type for mix node! " + blendType);
                }

                break;
            }

            case BoxType.BL_MATH:
            {
                returnType = 1;


                float aValue = Float.intBitsToFloat(params[2]);
                float bValue = Float.intBitsToFloat(params[3]);
                float cValue = Float.intBitsToFloat(params[4]);

                var aVariable = getWithSwizzle(shader, gmat, box, 0, type);
                var bVariable = getWithSwizzle(shader, gmat, box, 1, type);
                var cVariable = getWithSwizzle(shader, gmat, box, 2, type);

                if (aVariable != null && aVariable.type != 1) throw new RuntimeException("TEMP FAIL!");
                if (bVariable != null && bVariable.type != 1) throw new RuntimeException("TEMP FAIL!");
                if (cVariable != null && cVariable.type != 1) throw new RuntimeException("TEMP FAIL!");



                String a = aVariable != null ? aVariable.toString() : Float.toString(aValue);
                String b = bVariable != null ? bVariable.toString() : Float.toString(bValue);
                String c = cVariable != null ? cVariable.toString() : Float.toString(cValue);

                var operation = gmat.getString(params[0]);
                variableName = operation.toLowerCase() + index;
                switch (operation)
                {
                    case "ADD":
                    {
                        assignment = String.format("(%s + %s)", a, b);
                        break;
                    }
                    case "MULTIPLY":
                    {
                        assignment = String.format("(%s * %s)", a, b);
                        break;
                    }
                    case "POWER":
                    {
                        assignment = String.format("pow(%s, %s)", a, b);
                        break;
                    }
                    default:
                    {
                        throw new RuntimeException(operation + " is not a supported math operation!");
                    }
                }

                break;
            }

            case BoxType.BL_VECTORMATH:
            {
                returnType = 3;

                Vector3f aValue = gmat.getFloat3(params[1]);
                Vector3f bValue = gmat.getFloat3(params[2]);

                var aVariable = getWithSwizzle(shader, gmat, box, 0, type);
                var bVariable = getWithSwizzle(shader, gmat, box, 1, type);


                if (aVariable != null && aVariable.type == 4) aVariable = new Variable(aVariable.toString() + ".xyz", 3);
                if (bVariable != null && bVariable.type == 4) bVariable = new Variable(bVariable.toString() + ".xyz", 3);

                String a = aVariable != null ? aVariable.toString() : String.format(Locale.ROOT, "float3(%f, %f, %f)", aValue.x, aValue.y, aValue.z);
                String b = bVariable != null ? bVariable.toString() : String.format(Locale.ROOT, "float3(%f, %f, %f)", bValue.x, bValue.y, bValue.z);


                var operation = gmat.getString(params[0]);
                variableName = operation.toLowerCase() + index;
                switch (operation)
                {
                    case "NORMALIZE":
                    {
                        assignment = String.format("normalize(%s)", a);
                        break;
                    }
                    case "ADD":
                    {
                        assignment = String.format("(%s + %s)", a, b);
                        break;
                    }
                    case "DOT_PRODUCT":
                    {
                        returnType = 1;
                        assignment = String.format("dot(%s, %s)", a, b);
                        break;
                    }
                    case "CROSS_PRODUCT":
                    {
                        assignment = String.format("cross(%s, %s)", a, b);
                        break;
                    }
                    default:
                    {
                        throw new RuntimeException(operation + " is not a supported math operation!");
                    }
                }

                break;
            }

            case BoxType.BL_VALUE:
            {
                float bValue = Float.intBitsToFloat(params[1]);
                return new Variable(Float.toString(bValue), 1);
            }

            case BoxType.BL_NORMAL_MAP:
            {
                returnType = 3;

                float strengthValue = Float.intBitsToFloat(params[0]);
                var strengthVariable = getWithSwizzle(shader, gmat, box, 0, type);

                Vector4f colorValue = gmat.getFloat4(params[1]);
                var colorVariable = getWithSwizzle(shader, gmat, box, 1, type);

                String strength = strengthVariable != null ? strengthVariable.toString() : Float.toString(strengthValue);
                String color = colorVariable != null ? colorVariable.toString() : String.format(Locale.ROOT, "float4(%s, %s, %s, %s)", colorValue.x, colorValue.y, colorValue.z, colorValue.w);

                variableName = "nrm" + index;
                assignment = String.format("NormalMap(iNormal, iTangent, %s, %s)", color, strength);

                break;
            }

            default:
            {
                throw new RuntimeException("Unhandled box type! (" + (box.type) + ")");
            }
        }

        Variable variable = new Variable(variableName, returnType);
        LOOKUP.put(box, variable);

        shader.append(String.format("\tfloat%d %s = %s;\n", returnType, variableName,
            assignment));

        return variable;
    }

    public static final String setupPath(RGfxMaterial gfx, MaterialBox box, int port)
    {
        if (box == null && gfx.isBlenderMaterial())
        {
            var outputBox = gfx.boxes.get(gfx.getOutputBox());

            if (port == BrdfPort.DIFFUSE && outputBox.getParameters()[0] != 0)
            {
                Vector4f c = gfx.getFloat4(outputBox.getParameters()[0]);
                return String.format(Locale.ROOT, "\treturn float4(%s, %s, %s, %s);", c.x, c.y, c.z, c.w);
            }

            if (port == BrdfPort.SELF_ILLUMINATION && outputBox.getParameters()[1] != 0)
            {
                Vector4f c = gfx.getFloat4(outputBox.getParameters()[1]);
                return String.format(Locale.ROOT, "\treturn float4(%s, %s, %s, %s);", c.x, c.y, c.z, c.w);
            }

            if (port == BrdfPort.SPECULAR && outputBox.getParameters()[2] != 0)
            {
                Vector4f c = gfx.getFloat4(outputBox.getParameters()[2]);
                return String.format(Locale.ROOT, "\treturn float4(%s, %s, %s, %s);", c.x, c.y, c.z, c.w);
            }
        }

        CURRENT_ATTRIBUTE = 0;
        LOOKUP.clear();
        if (box != null)
        {

            StringBuilder builder = new StringBuilder(1000);
            MaterialWire wire = gfx.getWireConnectedToPort(gfx.getOutputBox(), port);
            Variable variable = resolve(builder, gfx, box, port, wire != null ? wire.portFrom : 0);

            if (port == BrdfPort.BUMP && !IS_BLENDER)
            {
                if (gfx.useVitaShaderSource)
                {
                    builder.append(String.format("\treturn %s;", variable));
                }
                else if (!IS_BAKER)
                {
                    String function = USE_NORMAL_MAPS ? "NormalMap" : "BumpMap";
                    builder.append(String.format("\treturn %s(iNormal, iTangent, %s, BumpLevel);",
                        function,
                        variable));
                }
                else
                {
                    builder.append(String.format("\treturn float4(1.0 - %s.w, %s.y, 1" +
                                                 ".0, 1.0);",
                        variable, variable));
                }

            }
            else
            {
                if (port == BrdfPort.BUMP && IS_BLENDER || variable.type == 4)
                    builder.append(String.format("\treturn %s;", variable));
                else if (variable.type == 3)
                    builder.append(String.format("\treturn float4(%s, 1.0);", variable));
                else
                    builder.append(String.format("\treturn float4(%s);", variable));
            }
                

            return builder.toString().replaceAll("float1", "float");
        }

        if (port == BrdfPort.BUMP && IS_GLSL)
            return "\treturn float4(normalize(normal), 1.0);";
        if (port == BrdfPort.OPACITY)
            return "\treturn float4(1.0); // This material does not have alpha masking";
        if (port == BrdfPort.SPECULAR && IS_BAKER)
            return "\treturn float4(1.0, 1.0, 1.0, 1.0);\n";

        return "\treturn float4(0.0); // This material doesn't use this.";
    }

    public static final String generateBakedShaderSource(RGfxMaterial material, int port,
                                                         boolean doAlphaClip)
    {
        IS_GLSL = true;
        IS_BAKER = true;
        int output = material.getOutputBox();
        MaterialBox box = material.getBoxConnectedToPort(output, port);

        String constants = "";
        if (material.parameterAnimations != null && material.parameterAnimations.length != 0)
        {
            constants = '\n' + constants;
            for (MaterialParameterAnimation animation : material.parameterAnimations)
            {
                constants = String.format(Locale.ROOT,
                    "vec4 t%s = vec4(%f, %f, %f, %f);\n",
                    animation.getName(),
                    animation.baseValue.x, animation.baseValue.y,
                    animation.baseValue.z, animation.baseValue.w
                ) + constants;
            }
            constants = "// Parameter animation uniforms \n" + constants;
        }

        String shader = BAKE_GLSL;
        if (doAlphaClip)
        {
            shader = ALPHA_BAKE_GLSL;
            shader = shader.replace("ENV.ALPHA_TEST_LEVEL", String.format(Locale.ROOT, "%f",
                material.alphaTestLevel));
            int alphaPort = BrdfPort.OPACITY;
            MaterialBox alpha = material.getBoxConnectedToPort(output, BrdfPort.OPACITY);
            if (alpha == null)
            {
                alphaPort = BrdfPort.DIFFUSE;
                alpha = material.getBoxConnectedToPort(output, BrdfPort.DIFFUSE);
            }

            shader = shader.replace("ENV.ALPHA_SETUP", constants + setupPath(material, alpha,
                alphaPort));
        }

        shader = shader.replace("ENV.SAMPLE_SETUP", constants + setupPath(material, box, port));
        shader = shader.replaceAll("iUV", "uv");
        shader = shader.replaceAll("iDecalUV", "decal_uv");
        shader = shader.replaceAll("iVec2Eye", "vec2eye");
        shader = shader.replaceAll("iTangent", "tangent");
        shader = shader.replaceAll("iNormal", "normal");
        shader = shader.replaceAll("iColor", "thing_color");

        return shader;
    }

    public static final String generateShaderSource(RGfxMaterial material, int flags,
                                                    boolean useEnvironmentVariables)
    {
        IS_GLSL = (flags == 0xDEADBEEF);
        IS_BAKER = false;
        IS_BLENDER = material.isBlenderMaterial();

        String shader = material.useVitaShaderSource ? BRDF_VITA_CG : (IS_GLSL ? BRDF_GLSL : BRDF_CG);
        

        if (material.sourceShaderName != null && !material.sourceShaderName.isEmpty())
        {
            var file = new File(CwlibConfiguration.SHADER_SOURCE_DIRECTORY, material.sourceShaderName + ".cg");
            if (file.exists())
                shader = FileIO.readString(file.toPath());
        }


        if (!IS_GLSL && material.parameterAnimations != null && material.parameterAnimations.length != 0)
        {
            shader = '\n' + shader;
            for (MaterialParameterAnimation animation : material.parameterAnimations)
            {
                shader = String.format(Locale.ROOT,
                    "uniform float4 t%s = float4(%f, %f, %f, %f);\n",
                    animation.getName(),
                    animation.baseValue.x, animation.baseValue.y,
                    animation.baseValue.z, animation.baseValue.w
                ) + shader;
            }
            shader = "// Parameter animation uniforms \n" + shader;
        }

        int output = material.getOutputBox();

        ArrayList<String> properties = new ArrayList<>();

        MaterialBox normal = material.getBoxConnectedToPort(output, BrdfPort.BUMP);
        MaterialBox diffuse = material.getBoxConnectedToPort(output, BrdfPort.DIFFUSE);
        MaterialBox alpha = material.getBoxConnectedToPort(output, BrdfPort.OPACITY);
        MaterialBox fuzz = material.getBoxConnectedToPort(output, BrdfPort.FUZZ);
        MaterialBox aniso = material.getBoxConnectedToPort(output, BrdfPort.ANISO);
        MaterialBox cc = material.getBoxConnectedToPort(output, BrdfPort.COLOR_CORRECTION);
        MaterialBox ramp = material.getBoxConnectedToPort(output, BrdfPort.TOON_RAMP);
        MaterialBox unknown = material.getBoxConnectedToPort(output, BrdfPort.UNKNOWN);
        MaterialBox specular = material.getBoxConnectedToPort(output, BrdfPort.SPECULAR);
        MaterialBox glow = material.getBoxConnectedToPort(output, BrdfPort.SELF_ILLUMINATION);
        MaterialBox reflection = material.getBoxConnectedToPort(output, BrdfPort.REFLECT);

        if (alpha != null) properties.add("ALPHA");
        else if ((material.flags & GfxMaterialFlags.ALPHA_CLIP) != 0)
        {
            alpha = diffuse;
            properties.add("ALPHA");
        }

        if (specular != null || (material.isBlenderMaterial() && material.boxes.get(output).getParameters()[2] != 0)) properties.add("SPECULAR");
        if (normal != null) properties.add("NORMAL");
        if (glow != null || (material.isBlenderMaterial() && material.boxes.get(output).getParameters()[1] != 0)) properties.add("GLOW");
        if (reflection != null) properties.add("REFRACT");
        if (material.alphaLayer == 0xc0 || material.alphaLayer == -64 || material.forceGenerateAsGlassy)
        {
            properties.add("GLASS");
            if (material.alphaMode == 0)
                material.alphaMode = 1;
        }
        
        if (unknown != null) properties.add("ST7");
        if (aniso != null) properties.add("ANISO");
        if (cc != null) properties.add("COLOR_CORRECTION");
        if (fuzz != null) properties.add("FUZZ");
        if (ramp != null) properties.add("LIGHTING_RAMP");

        if (properties.size() == 0)
            properties.add("NO_FLAGS");

        shader = shader.replace("ENV.AUTO_NORMAL_SETUP", setupPath(material, normal,
            BrdfPort.BUMP));
        shader = shader.replace("ENV.AUTO_REFLECTION_SETUP", setupPath(material, reflection,
            BrdfPort.REFLECT));
        shader = shader.replace("ENV.AUTO_SPECULAR_SETUP", setupPath(material, specular,
            BrdfPort.SPECULAR));
        shader = shader.replace("ENV.AUTO_DIFFUSE_SETUP", setupPath(material, diffuse,
            BrdfPort.DIFFUSE));
        shader = shader.replace("ENV.AUTO_GLOW_SETUP", setupPath(material, glow,
            BrdfPort.SELF_ILLUMINATION));
        shader = shader.replace("ENV.AUTO_ALPHA_SETUP", setupPath(material, alpha,
            BrdfPort.OPACITY));
        shader = shader.replace("ENV.AUTO_ST7_SETUP", setupPath(material, unknown,
            BrdfPort.UNKNOWN));
        shader = shader.replace("ENV.AUTO_FUZZ_SETUP", setupPath(material, fuzz,
            BrdfPort.FUZZ));
        shader = shader.replace("ENV.AUTO_ANISO_SETUP", setupPath(material, aniso,
            BrdfPort.ANISO));
        shader = shader.replace("ENV.AUTO_COLOR_CORRECTION_SETUP", setupPath(material, cc,
            BrdfPort.COLOR_CORRECTION));
        shader = shader.replace("ENV.AUTO_RAMP_SETUP", setupPath(material, ramp,
            BrdfPort.TOON_RAMP));


        if (flags != -1)
            shader = shader.replace("ENV.COMPILE_FLAGS", "" + flags);
        shader = shader.replace("ENV.MATERIAL_PROPERTIES", String.format("(%s)", String.join(
            " | "
            , properties)));

        if (!useEnvironmentVariables)
        {
            shader = shader.replace("ENV.ALPHA_TEST_LEVEL", String.format(Locale.ROOT, "%f",
                material.alphaTestLevel));
            shader = shader.replace("ENV.ALPHA_MODE", "" + material.alphaMode);
            
            shader = shader.replace("ENV.COSINE_POWER", String.format(Locale.ROOT, "%f",
                material.cosinePower * (material.useVitaShaderSource ? 88.0f : 22.0f)));
            shader = shader.replace("ENV.BUMP_LEVEL", String.format(Locale.ROOT, "%f",
                material.bumpLevel * 2.0f));

            shader = shader.replace("ENV.REFLECTION_BLUR", String.format(Locale.ROOT, "%f",
                material.reflectionBlur - 1.0f));
            shader = shader.replace("ENV.REFRACTIVE_INDEX", String.format(Locale.ROOT, "%f",
                material.refractiveIndex));

            shader = shader.replace("ENV.FRESNEL_FALLOFF_POWER", String.format(Locale.ROOT,
                "%f",
                material.refractiveFresnelFalloffPower));
            shader = shader.replace("ENV.FRESNEL_MULTIPLIER", String.format(Locale.ROOT, "%f",
                material.refractiveFresnelMultiplier));
            shader = shader.replace("ENV.FRESNEL_OFFSET", String.format(Locale.ROOT, "%f",
                material.refractiveFresnelOffset));
            shader = shader.replace("ENV.FRESNEL_SHIFT", String.format(Locale.ROOT, "%f",
                material.refractiveFresnelShift));

            shader = shader.replace("ENV.FUZZ_LIGHTING_BIAS", String.format(Locale.ROOT, "%f",
                ((float) ((int) material.fuzzLightingBias & 0xff)) / 255.0f));
            shader = shader.replace("ENV.FUZZ_LIGHTING_SCALE", String.format(Locale.ROOT,
                "%f",
                ((float) ((int) material.fuzzLightingScale & 0xff)) / 255.0f));

            shader = shader.replace("ENV.IRIDESCENCE_ROUGHNESS", String.format(Locale.ROOT,
                "%f",
                ((float) ((int) material.iridesenceRoughness & 0xff)) / 255.0f));
        }

        if (IS_GLSL)
        {
            shader = shader.replaceAll("iUV", "uv");
            shader = shader.replaceAll("iDecalUV", "decal_uv");
            shader = shader.replaceAll("iVec2Eye", "vec2eye");
            shader = shader.replaceAll("iTangent", "tangent");
            shader = shader.replaceAll("iNormal", "normal");
            shader = shader.replaceAll("iColor", "thing_color");
        }

        return shader;
    }

    public static String run(String... commands)
    {
        try
        {
            ProcessBuilder pb = new ProcessBuilder(commands);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(p.getInputStream())))
            {
                String line;
                while ((line = reader.readLine()) != null)
                    output.append(line + System.lineSeparator());
                p.waitFor();
            }

            System.out.println(output.toString().trim());

            return output.toString().trim();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return "Process failed to execute!";
        }
    }


    public static byte[] getShader(String source)
    {
        return getShader(source, GameShader.LBP1);
    }

    public static byte[] getShader(String source, GameShader shader)
    {
        File directory = ResourceSystem.getWorkingDirectory();

        File inputFile = new File(directory, "shader");
        File outputFile = new File(directory, "compiled");

        if (inputFile.exists()) inputFile.delete();
        if (outputFile.exists()) outputFile.delete();

        FileIO.write(source.getBytes(), inputFile.getAbsolutePath());

        String profile = "sce_fp_rsx";
        File compiler = CwlibConfiguration.SCE_CGC_EXECUTABLE;
        File stripper = CwlibConfiguration.SCE_CGC_STRIP_EXECUTABLE;
        if (shader == GameShader.LBP3_PS4)
        {
            compiler = CwlibConfiguration.SCE_PSSL_EXECUTABLE;
            profile = "sce_ps_orbis";
            stripper = null;
        }

        if (shader == GameShader.VITA)
        {
            compiler = CwlibConfiguration.SCE_PSP2_CGC_EXECUTABLE;
            profile = "sce_fp_psp2";
        }

        String msg;
        if (shader == GameShader.LBP3_PS4)
            msg = run(compiler.getAbsolutePath(), "-profile", profile, "-o",
                outputFile.getAbsolutePath(), inputFile.getAbsolutePath(), "-nodx10clamp",
                "-write-constant-block", "-sbiversion", "0", "-dont-strip-default-cb");
        else if (shader != GameShader.LBP1 && shader != GameShader.LBP_STUPID_BUILD && shader != GameShader.VITA)
            msg = run(compiler.getAbsolutePath(), "-profile", profile, "-o",
                outputFile.getAbsolutePath(), inputFile.getAbsolutePath(), "-mcgb",
                "--nofastmath", "--nofastprecision", "--O0");
        else
            msg = run(compiler.getAbsolutePath(), "-profile", profile, "-o",
                outputFile.getAbsolutePath(), inputFile.getAbsolutePath());

        if (stripper != null && stripper.exists())
        {
            if (shader == GameShader.VITA)
            {
                run(stripper.getAbsolutePath(), "-o",
                    outputFile.getAbsolutePath(), outputFile.getAbsolutePath());
            }
            else
            {
                run(stripper.getAbsolutePath(), "-semantic", "-param", "-sampler", "-varying",
                    outputFile.getAbsolutePath());
            }
        }

        inputFile.delete();
        if (outputFile.exists())
        {
            byte[] data = FileIO.read(outputFile.getAbsolutePath());
            outputFile.delete();
            return data;
        }

        throw new RuntimeException(msg);
    }
}
