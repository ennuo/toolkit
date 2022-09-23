package executables.gfx;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import configurations.ApplicationFlags;
import cwlib.enums.BoxType;
import cwlib.enums.GfxMaterialFlags;
import cwlib.resources.RGfxMaterial;
import cwlib.singleton.ResourceSystem;
import cwlib.structs.gmat.MaterialBox;
import cwlib.structs.gmat.MaterialWire;
import cwlib.util.FileIO;

public class GfxAssembler {
    public static String BRDF = FileIO.getResourceFileAsString("/shaders/brdf.cg");
    public static HashMap<MaterialBox, Variable> LOOKUP = new HashMap<>();

    public static boolean USE_ENV_VARIABLES = false;
    public static boolean USE_NORMAL_MAPS = false;
    public static int CURRENT_ATTRIBUTE = 0;

    public static class OutputPort {
        public static final int DIFFUSE = 0;
        public static final int ALPHA_CLIP = 1;
        public static final int SPECULAR = 2;
        public static final int BUMP = 3;
        public static final int GLOW = 4;
        public static final int REFLECTION = 6;
        public static final int UNKNOWN = 7; // 7, just adds tex * ambcol, to final color

        // 169
        public static final int ANISO = 170;
        public static final int TRANS = 171;
        public static final int COLOR_CORRECTION = 172; // ramp
        public static final int FUZZ = 173;
        public static final int BRDF_REFLECTANCE = 174;
        public static final int TOON_RAMP = 175;
    }

    public static class GfxFlags {
        public static final int NO_FLAGS = 0;
        public static final int DECALS = (1 << 0);
        public static final int WATER_TWEAKS = (1 << 1);
        public static final int SPRITELIGHT = (1 << 2);
        public static final int BAKED_AO = (1 << 3);
        public static final int DYNAMIC_AO = (1 << 4);
        public static final int AO_FLAGS = (BAKED_AO | DYNAMIC_AO);
        public static final int BAKED_SHADOWS = (1 << 5);
        public static final int DYNAMIC_SHADOWS = (1 << 6);
        public static final int SHADOW_FLAGS = (BAKED_SHADOWS | DYNAMIC_SHADOWS);
    
        public static final int LEGACY = (1 << 7);
        public static final int LEGACY_NORMAL_PASS = (1 << 8);
    
        public static final int SPECULAR = (1 << 9);
        public static final int NORMAL = (1 << 10);
    
        public static final int ALPHA = (1 << 11);
        public static final int REFRACT = (1 << 12);
        public static final int GLOW = (1 << 13);
    
        public static final int GLASS = (1 << 14);

        public static final int ORBIS = (1 << 15);
    }

    public static class Variable {
        public final String value;
        public final int type;

        public Variable(String value, int type) {
            this.value = value;
            this.type = type;
        }

        @Override public String toString() { return this.value.toString(); }
    }

    public static final Variable getWithSwizzle(StringBuilder shader, RGfxMaterial gfx, MaterialBox box, int port, int type) {
        MaterialWire wire = gfx.getWireConnectedToPort(box, port);
        if (wire == null) return null;
        Variable variable = resolve(shader, gfx, gfx.boxes[wire.boxFrom], type);
        String swizzle = new String(wire.swizzle).replaceAll("\0", "");
        if (swizzle.length() != 0)
            return new Variable(variable.value + "." + swizzle, swizzle.length());
        return variable;      
    }

    public static final Variable resolve(StringBuilder shader, RGfxMaterial gmat, MaterialBox box, int type) {
        if (LOOKUP.containsKey(box))
            return LOOKUP.get(box);
        
        int index = CURRENT_ATTRIBUTE++;
        int[] params = box.getParameters();
        int returnType = 4;
        String variableName, assignment;
        switch (box.type) {
            case BoxType.OUTPUT: { throw new RuntimeException("Why?"); }
            case BoxType.TEXTURE_SAMPLE: {
                String texVar = "s" + params[5];
                variableName = "smp" + index;
                String channel = (params[4] == 1 || params[4] == 256) ? "zw" : "xy";
                if (type == OutputPort.REFLECTION) {
                    assignment = String.format("SAMPLE_2D_BIAS(%s, iUV, ReflectionBlur)", texVar);
                    break;
                }

                String uv = "iUV." + channel;
                float sx = Float.intBitsToFloat(params[0]);
                float sy = Float.intBitsToFloat(params[1]);
                if (sx != 1.0f || sy != 1.0f)
                    uv = String.format("(%s * float2(%f, %f))", uv, sx, sy);
                float ox = Float.intBitsToFloat(params[2]);
                float oy = Float.intBitsToFloat(params[3]);
                if (ox != 0.0f || oy != 0.0f)
                    uv += String.format(" + float2(%f, %f)", ox, oy);

                Variable add = getWithSwizzle(shader, gmat, box, 0, type);
                if (add != null)
                    uv = String.format("(%s) + %s", uv, add);
                Variable scale = getWithSwizzle(shader, gmat, box, 1, type);
                if (scale != null)
                    uv = String.format("(%s) * %s", uv, scale);
                Variable sub = getWithSwizzle(shader, gmat, box, 2, type);
                if (sub != null)
                    uv = String.format("(%s) - %s", uv, sub);


                assignment = String.format("SAMPLE_2D(%s, %s)", texVar, uv);

                if (type == OutputPort.FUZZ) {
                    shader.append(String.format("\tfloat4 %s = float4(%s.x, SAMPLE_2D(%s, iUV.zw).yz, 0.0);\n", variableName, assignment, texVar));
                    return new Variable(variableName, 4);
                }

                break;
            }
            case BoxType.THING_COLOR: return new Variable("iColor", 4);
            case BoxType.COLOR: {
                variableName = "col" + index;
                assignment = String.format("float4(%s, %s, %s, %s)", Float.intBitsToFloat(params[0]), Float.intBitsToFloat(params[1]), Float.intBitsToFloat(params[2]), Float.intBitsToFloat(params[3]));
                break;
            }
            case BoxType.CONSTANT: return new Variable(String.format("%s", Float.intBitsToFloat(params[0])), 1);
            case BoxType.CONSTANT2: {
                return new Variable(String.format("float2(%s, %s)", 
                    Float.intBitsToFloat(params[0]),
                    Float.intBitsToFloat(params[1])), 2);
            }
            case BoxType.CONSTANT3: {
                return new Variable(String.format("float3(%s, %s, %s)", 
                    Float.intBitsToFloat(params[0]),
                    Float.intBitsToFloat(params[1]),
                    Float.intBitsToFloat(params[2])), 3);
            }
            case BoxType.CONSTANT4: {
                return new Variable(String.format("float4(%s, %s, %s, %s)", 
                    Float.intBitsToFloat(params[0]),
                    Float.intBitsToFloat(params[1]),
                    Float.intBitsToFloat(params[2]),
                    Float.intBitsToFloat(params[3])), 4);
            }
            // 8
            case BoxType.MULTIPLY_ADD: {
                MaterialBox node = gmat.getBoxConnectedToPort(box, 0);
                if (node == null) 
                    throw new RuntimeException("MAD node is supposed to take one input!");
                
                Variable input = getWithSwizzle(shader, gmat, box, 0, type);
                returnType = input.type;
                
                variableName = "mad" + index;
                assignment = String.format("((%s * %s) + %s)", input,
                    Float.intBitsToFloat(params[0]),
                    Float.intBitsToFloat(params[1]));
                break;
                
            }
            case BoxType.MULTIPLY: {
                Variable l = getWithSwizzle(shader, gmat, box, 0, type);
                Variable r = getWithSwizzle(shader, gmat, box, 1, type);

                if (l == null || r == null) 
                    throw new RuntimeException("Multiply node is supposed to take two inputs!");
                
                returnType = l.type;
                if (r.type > l.type)
                    returnType = r.type;

                variableName = "mul" + index;
                assignment = String.format("(%s * %s)", l, r);

                break;
            }
            case BoxType.ADD: {
                Variable l = getWithSwizzle(shader, gmat, box, 0, type);
                Variable r = getWithSwizzle(shader, gmat, box, 1, type);
                if (l == null || r == null) 
                    throw new RuntimeException("Add node is supposed to take two inputs!");
                
                returnType = l.type;
                if (r.type > l.type)
                    returnType = r.type;

                variableName = "sum" + index;

                assignment = String.format("(%s + %s)", l, r);

                break;
            }
            case BoxType.MIX: {
                Variable l = getWithSwizzle(shader, gmat, box, 0, type);
                Variable r = getWithSwizzle(shader, gmat, box, 1, type);
                if (l == null || r == null) 
                    throw new RuntimeException("Mix node is supposed to take two inputs!");

                returnType = l.type;
                if (r.type > l.type)
                    returnType = r.type;

                String f = String.format("%f", Float.intBitsToFloat(params[0]));
                Variable w = getWithSwizzle(shader, gmat, box, 2, type);
                if (w != null) f = w.toString();
            

                variableName = "mix" + index;
                assignment = String.format("(((%s - %s) * %s) + %s)", r, l, f, l);

                break;
            }
            case BoxType.MAKE_FLOAT2:
            case BoxType.MAKE_FLOAT3:
            case BoxType.MAKE_FLOAT4: {
                int count = box.type - 11;
                String[] inputs = new String[count];
                for (int i = 0; i < inputs.length; ++i) {
                    Variable variable = getWithSwizzle(shader, gmat, box, i, type);
                    if (variable != null)
                        inputs[i] = variable.value;
                    else
                        inputs[i] = String.format("%f", Float.intBitsToFloat(params[i]));
                }
                
                return new Variable(
                    String.format("float%d(%s)", count, String.join(", ", inputs)),
                    count
                );
            }
            case BoxType.BLEND: {
                Variable input1 = getWithSwizzle(shader, gmat, box, 0, type);
                Variable input2 = getWithSwizzle(shader, gmat, box, 1, type);
                Variable input3 = getWithSwizzle(shader, gmat, box, 2, type);
                
                returnType = input1.type;
                if (input2.type > returnType)
                    returnType = input2.type;
                if (input3.type > returnType)
                    returnType = input3.type;

                variableName = "blnd" + index;

                assignment = String.format("((%s - %s) * saturate(%s.x * 100.0 - 15.0) + %s)", input2, input1, input3, input1);
                
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
            case BoxType.EXPONENT: {
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
            default: {
                throw new RuntimeException("Unhandled box type! (" + (box.type) + ")");
            }
        }

        Variable variable = new Variable(variableName, returnType);
        LOOKUP.put(box, variable);

        shader.append(String.format("\tfloat%d %s = %s;\n", returnType, variableName, assignment));
        
        return variable;
    }

    public static final String setupPath(RGfxMaterial gfx, MaterialBox box, int port) {
        CURRENT_ATTRIBUTE = 0;
        LOOKUP.clear();
        if (box != null) {

            StringBuilder builder = new StringBuilder(1000);
            Variable variable = resolve(builder, gfx, box, port);

            if (port == OutputPort.BUMP) {
                String function = USE_NORMAL_MAPS ? "NormalMap" : "BumpMap";
                builder.append(String.format("\treturn %s(iNormal, iTangent, %s);", function, variable));
            } else
                builder.append(String.format("\treturn %s;", variable));

            return builder.toString();
        }

        if (port == OutputPort.ALPHA_CLIP)
            return "\treturn float4(1.); // This material does not have alpha masking";
        return "\treturn float4(0.); // This material doesn't use this.";
    }

    public static final String generateBRDF(RGfxMaterial material, int flags) {
        MaterialBox normal = material.getBoxConnectedToPort(material.getOutputBox(), OutputPort.BUMP);
        MaterialBox diffuse = material.getBoxConnectedToPort(material.getOutputBox(), OutputPort.DIFFUSE);
        MaterialBox alpha = material.getBoxConnectedToPort(material.getOutputBox(), OutputPort.ALPHA_CLIP);
        MaterialBox fuzz = material.getBoxConnectedToPort(material.getOutputBox(), OutputPort.FUZZ);
        MaterialBox aniso = material.getBoxConnectedToPort(material.getOutputBox(), OutputPort.ANISO);
        MaterialBox cc = material.getBoxConnectedToPort(material.getOutputBox(), OutputPort.COLOR_CORRECTION);
        MaterialBox ramp = material.getBoxConnectedToPort(material.getOutputBox(), OutputPort.TOON_RAMP);
        MaterialBox unknown = material.getBoxConnectedToPort(material.getOutputBox(), OutputPort.UNKNOWN);
        MaterialBox specular = material.getBoxConnectedToPort(material.getOutputBox(), OutputPort.SPECULAR);
        MaterialBox glow = material.getBoxConnectedToPort(material.getOutputBox(), OutputPort.GLOW);
        MaterialBox reflection = material.getBoxConnectedToPort(material.getOutputBox(), OutputPort.REFLECTION);
        
        ArrayList<String> properties = new ArrayList<>();

        if (alpha != null) properties.add("ALPHA");
        else if ((material.flags & GfxMaterialFlags.ALPHA_CLIP) != 0) {
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

        
        String shader = BRDF;
        if (flags != -1)
            shader = shader.replace("ENV.COMPILE_FLAGS", "" + flags);
        shader = shader.replace("ENV.MATERIAL_PROPERTIES", String.format("(%s)", String.join(" | ", properties)));
        shader = shader.replace("ENV.AUTO_NORMAL_SETUP", setupPath(material, normal, OutputPort.BUMP));
        shader = shader.replace("ENV.AUTO_REFLECTION_SETUP", setupPath(material, reflection, OutputPort.REFLECTION));
        shader = shader.replace("ENV.AUTO_SPECULAR_SETUP", setupPath(material, specular, OutputPort.SPECULAR));
        shader = shader.replace("ENV.AUTO_DIFFUSE_SETUP", setupPath(material, diffuse, OutputPort.DIFFUSE));
        shader = shader.replace("ENV.AUTO_GLOW_SETUP", setupPath(material, glow, OutputPort.GLOW));
        shader = shader.replace("ENV.AUTO_ALPHA_SETUP", setupPath(material, alpha, OutputPort.ALPHA_CLIP));
        shader = shader.replace("ENV.AUTO_ST7_SETUP", setupPath(material, unknown, OutputPort.UNKNOWN));
        shader = shader.replace("ENV.AUTO_FUZZ_SETUP", setupPath(material, fuzz, OutputPort.FUZZ));
        shader = shader.replace("ENV.AUTO_ANISO_SETUP", setupPath(material, aniso, OutputPort.ANISO));
        shader = shader.replace("ENV.AUTO_COLOR_CORRECTION_SETUP", setupPath(material, cc, OutputPort.COLOR_CORRECTION));
        shader = shader.replace("ENV.AUTO_RAMP_SETUP", setupPath(material, ramp, OutputPort.TOON_RAMP));

        if (!USE_ENV_VARIABLES) {
            shader = shader.replace("ENV.ALPHA_TEST_LEVEL", String.format("%f", material.alphaTestLevel));
            shader = shader.replace("ENV.ALPHA_MODE", "" + material.alphaMode);
            
            shader = shader.replace("ENV.COSINE_POWER", String.format("%f", material.cosinePower * 22.0f));
            shader = shader.replace("ENV.BUMP_LEVEL", String.format("%f", material.bumpLevel));
            
            shader = shader.replace("ENV.REFLECTION_BLUR", String.format("%f", material.reflectionBlur - 1.0f));
            shader = shader.replace("ENV.REFRACTIVE_INDEX", String.format("%f", material.refractiveIndex));

            shader = shader.replace("ENV.FRESNEL_FALLOFF_POWER", String.format("%f", material.refractiveFresnelFalloffPower));
            shader = shader.replace("ENV.FRESNEL_MULTIPLIER", String.format("%f", material.refractiveFresnelMultiplier));
            shader = shader.replace("ENV.FRESNEL_OFFSET", String.format("%f", material.refractiveFresnelOffset));
            shader = shader.replace("ENV.FRESNEL_SHIFT", String.format("%f", material.refractiveFresnelShift));

            shader = shader.replace("ENV.FUZZ_LIGHTING_BIAS", String.format("%f", ((float)((int)material.fuzzLightingBias & 0xff)) / 255.0f));
            shader = shader.replace("ENV.FUZZ_LIGHTING_SCALE", String.format("%f", ((float)((int)material.fuzzLightingScale & 0xff)) / 255.0f));

            shader = shader.replace("ENV.IRIDESCENCE_ROUGHNESS", String.format("%f", ((float)((int)material.iridesenceRoughness & 0xff)) / 255.0f));
        }

        return shader;
    }

    public static String run(String... commands) {
        try {
            ProcessBuilder pb = new ProcessBuilder(commands);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null)
                    output.append(line + System.lineSeparator());
                p.waitFor();
            }
            return output.toString().trim();
        } catch (Exception ex) { 
            return "Process failed to execute!";
        }
    }


    public static byte[] getShader(String string) { return getShader(string, false, false); }
    public static byte[] getShader(String string, boolean cgb, boolean orbis) {
        File directory = ResourceSystem.getWorkingDirectory();

        File shader = new File(directory, "shader");
        File compiled = new File(directory, "compiled");

        if (shader.exists()) shader.delete();
        if (compiled.exists()) compiled.delete();

        FileIO.write(string.getBytes(), shader.getAbsolutePath());
    
        String profile = "sce_fp_rsx";
        File compiler = ApplicationFlags.SCE_CGC_EXECUTABLE;
        if (orbis) {
            compiler = ApplicationFlags.SCE_PSSL_EXECUTABLE;
            profile = "sce_ps_orbis";
        }

        String msg;
        if (cgb && !orbis) 
            msg = run(compiler.getAbsolutePath(), "-profile", profile, "-o", compiled.getAbsolutePath(), shader.getAbsolutePath(), "-mcgb");
        else
            msg = run(compiler.getAbsolutePath(), "-profile", profile, "-o", compiled.getAbsolutePath(), shader.getAbsolutePath());

        shader.delete();
        if (compiled.exists()) {
            byte[] data = FileIO.read(compiled.getAbsolutePath());
            compiled.delete();
            return data;
        } 
        
        throw new RuntimeException(msg);
    }
}
