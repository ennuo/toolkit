package cwlib.util.gfx;

import cwlib.CwlibConfiguration;
import cwlib.enums.BoxType;
import cwlib.enums.BrdfPort;
import cwlib.enums.GameShader;
import cwlib.enums.GfxMaterialFlags;
import cwlib.resources.RGfxMaterial;
import cwlib.singleton.ResourceSystem;
import cwlib.structs.gmat.MaterialBox;
import cwlib.structs.gmat.MaterialParameterAnimation;
import cwlib.structs.gmat.MaterialWire;
import cwlib.util.FileIO;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class GfxAssembler
{
    public static String BRDF_CG = FileIO.getResourceFileAsString("/shaders/templates/cg/brdf" +
                                                                  ".cg");
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
    public static int CURRENT_ATTRIBUTE = 0;

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

    public static final Variable resolve(StringBuilder shader, RGfxMaterial gmat, MaterialBox box
        , int type, int port)
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
                String channel = (params[4] == 1 || params[4] == 257) ? "zw" : "xy";
                if (type == BrdfPort.REFLECTION)
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
                    else
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
                        inputs[i] = variable.value;
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
        CURRENT_ATTRIBUTE = 0;
        LOOKUP.clear();
        if (box != null)
        {

            StringBuilder builder = new StringBuilder(1000);
            // MaterialWire wire = gfx.getWireConnectedToPort(box, port);
            Variable variable = resolve(builder, gfx, box, port, 0);

            if (port == BrdfPort.BUMP)
            {

                if (!IS_BAKER)
                {
                    String function = USE_NORMAL_MAPS ? "NormalMap" : "BumpMap";
                    builder.append(String.format("\treturn %s(iNormal, iTangent, %s);",
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
                builder.append(String.format("\treturn float4(%s);", variable));

            return builder.toString();
        }

        if (port == BrdfPort.BUMP && IS_GLSL)
            return "\treturn float4(normalize(normal), 1.0);";
        if (port == BrdfPort.ALPHA_CLIP)
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
            int alphaPort = BrdfPort.ALPHA_CLIP;
            MaterialBox alpha = material.getBoxConnectedToPort(output, BrdfPort.ALPHA_CLIP);
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

        String shader = IS_GLSL ? BRDF_GLSL : BRDF_CG;

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
        MaterialBox alpha = material.getBoxConnectedToPort(output, BrdfPort.ALPHA_CLIP);
        MaterialBox fuzz = material.getBoxConnectedToPort(output, BrdfPort.FUZZ);
        MaterialBox aniso = material.getBoxConnectedToPort(output, BrdfPort.ANISO);
        MaterialBox cc = material.getBoxConnectedToPort(output, BrdfPort.COLOR_CORRECTION);
        MaterialBox ramp = material.getBoxConnectedToPort(output, BrdfPort.TOON_RAMP);
        MaterialBox unknown = material.getBoxConnectedToPort(output, BrdfPort.UNKNOWN);
        MaterialBox specular = material.getBoxConnectedToPort(output, BrdfPort.SPECULAR);
        MaterialBox glow = material.getBoxConnectedToPort(output, BrdfPort.GLOW);
        MaterialBox reflection = material.getBoxConnectedToPort(output, BrdfPort.REFLECTION);

        if (alpha != null) properties.add("ALPHA");
        else if ((material.flags & GfxMaterialFlags.ALPHA_CLIP) != 0)
        {
            alpha = diffuse;
            properties.add("ALPHA");
        }

        if (specular != null) properties.add("SPECULAR");
        if (normal != null) properties.add("NORMAL");
        if (glow != null) properties.add("GLOW");
        if (reflection != null) properties.add("REFRACT");
        if (material.alphaLayer == 0xc0)
            properties.add("GLASS");
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
            BrdfPort.REFLECTION));
        shader = shader.replace("ENV.AUTO_SPECULAR_SETUP", setupPath(material, specular,
            BrdfPort.SPECULAR));
        shader = shader.replace("ENV.AUTO_DIFFUSE_SETUP", setupPath(material, diffuse,
            BrdfPort.DIFFUSE));
        shader = shader.replace("ENV.AUTO_GLOW_SETUP", setupPath(material, glow,
            BrdfPort.GLOW));
        shader = shader.replace("ENV.AUTO_ALPHA_SETUP", setupPath(material, alpha,
            BrdfPort.ALPHA_CLIP));
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
                material.cosinePower * 22.0f));
            shader = shader.replace("ENV.BUMP_LEVEL", String.format(Locale.ROOT, "%f",
                material.bumpLevel));

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
            return output.toString().trim();
        }
        catch (Exception ex)
        {
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

        String msg;
        if (shader == GameShader.LBP3_PS4)
            msg = run(compiler.getAbsolutePath(), "-profile", profile, "-o",
                outputFile.getAbsolutePath(), inputFile.getAbsolutePath(), "-nodx10clamp",
                "-write-constant-block", "-sbiversion", "0", "-dont-strip-default-cb");
        else if (shader != GameShader.LBP1)
            msg = run(compiler.getAbsolutePath(), "-profile", profile, "-o",
                outputFile.getAbsolutePath(), inputFile.getAbsolutePath(), "-mcgb",
                "--nofastmath", "--nofastprecision", "--O0");
        else
            msg = run(compiler.getAbsolutePath(), "-profile", profile, "-o",
                outputFile.getAbsolutePath(), inputFile.getAbsolutePath());

        if (stripper != null && stripper.exists())
            run(stripper.getAbsolutePath(), "-semantic", "-param", "-sampler", "-varying",
                outputFile.getAbsolutePath());

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
