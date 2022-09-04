package executables.gfx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import cwlib.enums.CompressionFlags;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.resources.RGfxMaterial;
import cwlib.types.Resource;
import cwlib.types.data.Revision;
import cwlib.util.Bytes;

public class CgCompiler {
    public static long[] getBytecode(byte[] cgb) {
        int count = ((cgb[0x8] & 0xFF) << 8 | (cgb[0x9] & 0xFF)) / 0x10;
        int offset = 0x20;
        long[] code = new long[count * 0x2];
        for (int i = 0; i < code.length; ++i, offset += 0x8) {
            code[i]	= 
                    (cgb[offset + 0] & 0xFFL) << 56L |
                    (cgb[offset + 1] & 0xFFL) << 48L |
                    (cgb[offset + 2] & 0xFFL) << 40L |
                    (cgb[offset + 3] & 0xFFL) << 32L |
                    (cgb[offset + 4] & 0xFFL) << 24L |
                    (cgb[offset + 5] & 0xFFL) << 16L |
                    (cgb[offset + 6] & 0xFFL) << 8L |
                    (cgb[offset + 7] & 0xFFL) << 0L;
        }
        return code;
    }


    public static byte[] convert(byte[] shader, ArrayList<Long> pool) {
        long[] code = getBytecode(shader);

        byte[] header = Arrays.copyOfRange(shader, 0, 0x20);
        header[0x4] = (byte) 0x80; // Set version to LBP custom

        // Update code size to reflect index array length
        header[0x8] = (byte) ((code.length) >>> 8);
        header[0x9] = (byte) ((code.length & 0xff));

        header[0x1f] = header[0x1c]; // Move register count over

        int cgbSize = shader.length - (code.length * 0x8) - 0x20;
        int cgbPaddedSize = cgbSize;
        if (cgbPaddedSize % 16 != 0)
            cgbPaddedSize += (16 - (cgbPaddedSize % 16));
        byte[] cgbData = new byte[cgbPaddedSize];
        System.arraycopy(shader, 0x20 + (code.length * 0x8), cgbData, 0, cgbSize);

        // Update index array offset
        header[0x1c] = (byte) ((cgbPaddedSize) >>> 8);
        header[0x1d] = (byte) ((cgbPaddedSize & 0xff));

        int codeLookupSize = code.length * 0x2;
        if (codeLookupSize % 16 != 0)
            codeLookupSize += (16 - (codeLookupSize % 16));
        byte[] lookup = new byte[codeLookupSize];
        for (int i = 0, offset = 0; i < code.length; ++i) {
            int index = pool.indexOf(code[i]);
            lookup[offset++] = (byte) ((index) >>> 8);
            lookup[offset++] = (byte) ((index & 0xff));
        }


        return Bytes.combine(header, cgbData, lookup);
    }

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

    public static final int GLASSY = (1 << 14);

    public static final int[] LBP2_FLAGS = {
        BAKED_AO | BAKED_SHADOWS,
        SPRITELIGHT | DYNAMIC_SHADOWS | DYNAMIC_AO | DECALS,
        SPRITELIGHT | DYNAMIC_SHADOWS | DYNAMIC_AO,
        SPRITELIGHT | SHADOW_FLAGS | AO_FLAGS,

        WATER_TWEAKS | BAKED_AO | BAKED_SHADOWS,
        WATER_TWEAKS | SPRITELIGHT | DYNAMIC_SHADOWS | DYNAMIC_AO | DECALS,
        WATER_TWEAKS | SPRITELIGHT | DYNAMIC_SHADOWS | DYNAMIC_AO,
        WATER_TWEAKS | SPRITELIGHT | SHADOW_FLAGS | AO_FLAGS,

        NO_FLAGS,
        DECALS
    };

    public static final int[] LBP1_FLAGS = {
        LEGACY | LEGACY_NORMAL_PASS,
        LEGACY,
        LEGACY | DECALS,
        LEGACY | WATER_TWEAKS
    };

    public static final int MATERIAL_FLAGS = 
        NORMAL | SPECULAR | REFRACT | GLASSY;

    public static byte[] compileShaderVariant(String shader, int index, boolean legacy) {
        int flags = (legacy) ? LBP1_FLAGS[index] : LBP2_FLAGS[index];
        flags |= MATERIAL_FLAGS;
        shader = String.format("#define FLAGS %d\n%s", flags, shader);
        return NVCompiler.getShader(shader, !legacy);
    }

    public static byte[] compileLegacy(String template, RGfxMaterial gmat) {
        byte[][] shaders = new byte[4][];
        for (int i = 0; i < 4; ++i)
            shaders[i] = compileShaderVariant(template, i, true);
        gmat.shaders = shaders;
        gmat.code = null;
        byte[] data =
            Resource.compress(gmat.build(new Revision(0x272, 0x4c44, 0x0017), CompressionFlags.USE_ALL_COMPRESSION));
        return data;
    }

    public static byte[] compile(String template, RGfxMaterial gmat) {
        byte[][] shaders = new byte[10][];

        HashSet<Long> pool = new HashSet<>();
        int actualLength = 0;
        for (int i = 0; i < shaders.length; ++i) {
            shaders[i] = compileShaderVariant(template, i, false);
            long[] code = getBytecode(shaders[i]);
            actualLength += code.length;
            for (long c : code)
                pool.add(c);
        }
        ArrayList<Long> code = new ArrayList<>(pool);
        code.sort((l, r) -> Long.compareUnsigned(l, r));

        System.out.println("Actual: " + actualLength);
        System.out.println("Shared: " + code.size());

        for (int i = 0; i < shaders.length; ++i)
            shaders[i] = convert(shaders[i], code);

        gmat.shaders = shaders;
        MemoryOutputStream stream = new MemoryOutputStream(code.size() * 0x8);
        for (long c : code) stream.i64(c);
        gmat.code = stream.getBuffer();

        byte[] data =
            Resource.compress(gmat.build(new Revision(0x3a3), CompressionFlags.USE_ALL_COMPRESSION));

        return data;
    }
}
