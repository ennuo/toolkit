package executables.gfx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import cwlib.enums.GameShader;
import cwlib.enums.GfxMaterialFlags;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.resources.RGfxMaterial;
import cwlib.util.Bytes;

public class CgAssembler {
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

    public static final int ORBIS = (1 << 15);

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

    public static byte[] compileShaderVariant(String source, int gmatFlags, int index, GameShader shader) {
        int flags = (shader == GameShader.LBP1) ? LBP1_FLAGS[index] : LBP2_FLAGS[index];
        if (shader == GameShader.LBP3_PS4) flags |= ORBIS;
        if (shader == GameShader.LBP2_PRE_ALPHA) flags |= WATER_TWEAKS;
        
        if ((gmatFlags & GfxMaterialFlags.RECEIVE_SHADOWS) != 0)
            flags |= (1 << 22);
        if ((gmatFlags & GfxMaterialFlags.RECEIVE_SUN) != 0)
            flags |= (1 << 23);
        if ((gmatFlags & GfxMaterialFlags.RECEIVE_SPRITELIGHTS) != 0)
            flags |= (1 << 24);
        
        source = source.replace("ENV.COMPILE_FLAGS", "" + flags);

        return GfxAssembler.getShader(source, shader);
    }

    public static void compile(String template, RGfxMaterial gmat, GameShader shader) {
        HashSet<Long> pool = new HashSet<>();
        for (int i = 0; i < gmat.shaders.length; ++i) {
            gmat.shaders[i] = compileShaderVariant(template, gmat.flags, i, shader);
            if (shader == GameShader.LBP2) {
                long[] code = getBytecode(gmat.shaders[i]);
                for (long c : code)
                    pool.add(c);
            }
        }

        if (shader == GameShader.LBP2) {
            ArrayList<Long> code = new ArrayList<>(pool);
            code.sort((l, r) -> Long.compareUnsigned(l, r));
            for (int i = 0; i < gmat.shaders.length; ++i)
                gmat.shaders[i] = convert(gmat.shaders[i], code);
            MemoryOutputStream stream = new MemoryOutputStream(code.size() * 0x8);
            for (long c : code) stream.i64(c);
                gmat.code = stream.getBuffer();
        } else gmat.code = null;
    }
}
