package executables.gfx;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import cwlib.enums.BoxType;
import cwlib.enums.CompressionFlags;
import cwlib.enums.GfxMaterialFlags;
import cwlib.resources.RGfxMaterial;
import cwlib.singleton.ResourceSystem;
import cwlib.structs.gmat.MaterialBox;
import cwlib.types.Resource;
import cwlib.types.data.Revision;
import cwlib.util.FileIO;
import toolkit.configurations.Config;

public class GfxAssembler {
    public static String BRDF = FileIO.getResourceFileAsString("/brdf.cg");
    public static HashMap<MaterialBox, String> LOOKUP = new HashMap<>();

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

    public static final String resolve(StringBuilder shader, RGfxMaterial gmat, MaterialBox box, int type) {
        if (LOOKUP.containsKey(box))
            return LOOKUP.get(box);
        
        int index = CURRENT_ATTRIBUTE++;
        int[] params = box.getParameters();
        String variableName, assignment;
        switch (box.type) {
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

                assignment = String.format("SAMPLE_2D(%s, %s)", texVar, uv);

                if (type == OutputPort.FUZZ) {
                    shader.append(String.format("\tfloat4 %s = float4(%s.x, SAMPLE_2D(%s, iUV.zw).yz, 0.0);\n", variableName, assignment, texVar));
                    return variableName;
                }

                break;
            }
            case BoxType.MULTIPLY: {
                MaterialBox[] nodes = gmat.getBoxesConnected(box);

                if (nodes.length != 2) throw new RuntimeException("Mulitipication node must take in two inputs!");
                String l = resolve(shader, gmat, nodes[0], type);
                String r = resolve(shader, gmat, nodes[1], type);
                variableName = "mul" + index;
                assignment = String.format("(%s * %s)", l, r);

                break;
            }
            case 2: {
                return "iColor";
            }
            case 4: {
                return "iColor"; // I don't think this is accurate, but whatever
            }
            case BoxType.COLOR: {
                variableName = "col" + index;
                assignment = String.format("float4(%s, %s, %s, %s)", Float.intBitsToFloat(params[0]), Float.intBitsToFloat(params[1]), Float.intBitsToFloat(params[2]), Float.intBitsToFloat(params[3]));
                break;
            }
            case 11: {
                MaterialBox lNode = gmat.getBoxConnectedToPort(box, 0);
                MaterialBox rNode = gmat.getBoxConnectedToPort(box, 1);
                if (lNode == null || rNode == null) 
                    throw new RuntimeException("(11) node is supposed to take two inputs!");

                String l = resolve(shader, gmat, lNode, type); // color
                String r = resolve(shader, gmat, rNode, type); // diffuse

                variableName = "sum" + index;

                assignment = String.format("(%s + %s)", l, r);

                break;
            }
            case 12: {
                MaterialBox lNode = gmat.getBoxConnectedToPort(box, 0);
                MaterialBox rNode = gmat.getBoxConnectedToPort(box, 1);
                if (lNode == null || rNode == null) 
                    throw new RuntimeException("Subtraction node is supposed to take two inputs!");

                String f = String.format("%f", Float.intBitsToFloat(params[0]));
                String l = resolve(shader, gmat, lNode, type); // color
                String r = resolve(shader, gmat, rNode, type); // diffuse

                variableName = "sub" + index;
                assignment = String.format("(((%s - %s) * %s) + %s)", r, l, f, l);

                break;
            }
            case 16: {
                String input1 = resolve(shader, gmat, gmat.getBoxConnectedToPort(box, 0), type);
                String input2 = resolve(shader, gmat, gmat.getBoxConnectedToPort(box, 1), type);
                String input3 = resolve(shader, gmat, gmat.getBoxConnectedToPort(box, 2), type);
                
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
            default: {
                throw new RuntimeException("Unhandled box type! (" + (box.type) + ")");
            }
        }

        LOOKUP.put(box, variableName);
        shader.append(String.format("\tfloat4 %s = %s;\n", variableName, assignment));
        return variableName;
    }

    public static final String setupPath(RGfxMaterial gfx, MaterialBox box, int port) {
        CURRENT_ATTRIBUTE = 0;
        LOOKUP.clear();
        if (box != null) {

            StringBuilder builder = new StringBuilder(1000);
            String variable = resolve(builder, gfx, box, port);

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
        MaterialBox ramp = material.getBoxConnectedToPort(material.getOutputBox(), OutputPort.TOON_RAMP);
        MaterialBox specular = material.getBoxConnectedToPort(material.getOutputBox(), OutputPort.SPECULAR);
        MaterialBox glow = material.getBoxConnectedToPort(material.getOutputBox(), OutputPort.GLOW);
        MaterialBox reflection = material.getBoxConnectedToPort(material.getOutputBox(), OutputPort.REFLECTION);

        ArrayList<String> properties = new ArrayList<>();

        if (normal != null) properties.add("NORMAL");

        if (alpha != null) properties.add("ALPHA");
        else if ((material.flags & GfxMaterialFlags.ALPHA_CLIP) != 0) {
            alpha = diffuse;
            properties.add("ALPHA");
        }

        if (specular != null) properties.add("SPECULAR");
        if (glow != null) properties.add("GLOW");
        if (reflection != null) properties.add("REFRACT");
        if (fuzz != null) properties.add("FUZZ");
        if (aniso != null) properties.add("ANISO");
        if (ramp != null) properties.add("LIGHTING_RAMP");
        if (material.alphaLayer == 0xc0) 
            properties.add("GLASS");
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
        shader = shader.replace("ENV.AUTO_FUZZ_SETUP", setupPath(material, fuzz, OutputPort.FUZZ));
        shader = shader.replace("ENV.AUTO_ANISO_SETUP", setupPath(material, aniso, OutputPort.ANISO));
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

        if (fuzz != null) 
            FileIO.write(shader.getBytes(), "C:/Users/Aidan/Desktop/shader.cg");
        
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
        File loc = new File("E:/util/sce-cgc.exe");
        if (orbis) {
            loc = new File("E:/util/orbis-wave-psslc.exe");
            profile = "sce_ps_orbis";
        }

        String msg;
        if (cgb && !orbis) 
            msg = run(loc.getAbsolutePath(), "-profile", profile, "-o", compiled.getAbsolutePath(), shader.getAbsolutePath(), "-mcgb");
        else
            msg = run(loc.getAbsolutePath(), "-profile", profile, "-o", compiled.getAbsolutePath(), shader.getAbsolutePath());

        shader.delete();
        if (compiled.exists()) {
            byte[] data = FileIO.read(compiled.getAbsolutePath());
            compiled.delete();
            return data;
        } 
        
        throw new RuntimeException(msg);
    }

    public static byte[] compile(RGfxMaterial material) {
        try {
            String normal = GfxAssembler.generateBRDF(material, GfxFlags.LEGACY | GfxFlags.LEGACY_NORMAL_PASS);
            String color = GfxAssembler.generateBRDF(material, GfxFlags.LEGACY);
            String decal = GfxAssembler.generateBRDF(material, GfxFlags.LEGACY | GfxFlags.DECALS);
            String water = GfxAssembler.generateBRDF(material, GfxFlags.LEGACY | GfxFlags.WATER_TWEAKS);
    
            byte[] normalShader = getShader(normal);
            byte[] colorShader = getShader(color);
            byte[] decalShader = getShader(decal);   
            byte[] waterShader = getShader(water);
            
            if (normalShader == null || colorShader == null || decalShader == null || waterShader == null) return null;
    
            material.shaders = new byte[][] { normalShader, colorShader, decalShader, waterShader };
            return Resource.compress(material.build(new Revision(0x272, 0x4c44, 0x0017), CompressionFlags.USE_ALL_COMPRESSION));
        } catch (Exception ex) {
            ex.printStackTrace();
            return null; 
        }
    }
}
