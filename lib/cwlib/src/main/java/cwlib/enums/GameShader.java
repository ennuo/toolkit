package cwlib.enums;

import java.util.Arrays;

import javax.management.RuntimeErrorException;

import cwlib.CwlibConfiguration;
import cwlib.resources.RGfxMaterial;
import cwlib.types.data.Revision;

public enum GameShader
{
    LBP1("LBP1", new Revision(Revisions.LBP1_MAX, Branch.LEERDAMMER.getID(), Revisions.LD_SHADER)),
    LBP_STUPID_BUILD("LBP1 Private Beta", new Revision(0x132)),
    LBP2_PRE_ALPHA("LBP2 Pre-Alpha", new Revision(0x332)),
    LBP2("LBP2", new Revision(0x393)),
    LBP3_PS4("LBP3 PS4", new Revision(0x393)),
    VITA("LBP Vita", new Revision(Branch.DOUBLE11.getHead(), Branch.DOUBLE11.getID(), Branch.DOUBLE11.getRevision()));

    public static GameShader[] COMPILABLE = Arrays.asList(GameShader.values()).stream().filter(x -> x.compilable()).toArray(GameShader[]::new);

    private final String name;
    private final Revision revision;
    private GameShader(String name, Revision revision)
    {
        this.name = name;
        this.revision = revision;
    }

    public String getName() { return name; }
    public Revision getRevision() { return revision; }

    public boolean hasAlphaMode()
    {
        return this != LBP1 && this != LBP_STUPID_BUILD;
    }

    public int getShaderCount()
    {
        switch (this)
        {
            case LBP1:
                return 4;
            case LBP_STUPID_BUILD:
                return 3;
            case LBP2:
            case LBP3_PS4:
                return 10;
            case LBP2_PRE_ALPHA:
                return 4;
            case VITA:
                return 24;
            default:
                throw new RuntimeException("Just to make the compiler happy!");
        }
    }
    
    public boolean compilable()
    {
        if (this == LBP3_PS4) return CwlibConfiguration.CAN_COMPILE_ORBIS_SHADERS;
        if (this == VITA) return CwlibConfiguration.CAN_COMPILE_PSP2_SHADERS;
        return CwlibConfiguration.CAN_COMPILE_CELL_SHADERS;
    }

    @Override public String toString() { return name; }

    public static GameShader fromMaterial(RGfxMaterial material, Revision revision)
    {
        if (revision.isLBP3())
            return material.shaders[0][0x25] == 'h' ? GameShader.LBP3_PS4 : GameShader.LBP2;
        
        int version = revision.getVersion();
        if (version == 0x132)
            return GameShader.LBP_STUPID_BUILD;

        if (revision.isVita())
            return GameShader.VITA;
        
        if (revision.isLBP2())
            return version < 0x34f ? GameShader.LBP2_PRE_ALPHA : GameShader.LBP2;

        return GameShader.LBP1;
    }
}
