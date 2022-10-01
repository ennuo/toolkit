package toolkit.gl;

import cwlib.enums.GfxMaterialFlags;
import cwlib.resources.RGfxMaterial;
import cwlib.singleton.ResourceSystem;
import cwlib.structs.things.parts.PLevelSettings;
import cwlib.types.Resource;
import cwlib.types.data.ResourceDescriptor;
import executables.gfx.GfxAssembler;
import executables.gfx.GfxAssembler.OutputPort;

import static org.lwjgl.opengl.GL20.*;

import java.util.HashMap;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

public class Shader {
    public static HashMap<ResourceDescriptor, Shader> PROGRAMS = new HashMap<>();
    public static final int MAX_BONES = 100;

    public int programID;
    public ResourceDescriptor descriptor;
    public boolean twoSided = false;
    public int alphaMode, alphaLayer;

    public int view, projection;
    public int[] matrices = new int[MAX_BONES];
    public int ambcol, fogcol, suncol, sunpos, rimcol, rimcol2;
    public int campos;
    public int lighscaleadd;
    public int color;

    public int[] locations = new int[8];
    public ResourceDescriptor[] textures = new ResourceDescriptor[8];

    public Shader(int programID) {
        this.programID = programID;
        this.descriptor = null;
        this.twoSided = true;

        this.getUniforms();
    }

    public Shader(ResourceDescriptor descriptor) {
        System.out.println("Linking RGfxMaterial: " + descriptor);

        if (PROGRAMS.containsKey(descriptor))
            throw new RuntimeException("Program is already compiled and linked!");
        
        this.descriptor = descriptor;

        byte[] data = ResourceSystem.extract(descriptor);
        if (data == null)
            throw new RuntimeException("Unable to retrieve data for program!");
        RGfxMaterial gfx = new Resource(data).loadResource(RGfxMaterial.class);

        this.twoSided = (gfx.flags & GfxMaterialFlags.TWO_SIDED) != 0;
        this.alphaMode = gfx.alphaMode & 0xff;
        this.alphaLayer = gfx.alphaLayer & 0xff;

        if (this.alphaLayer == 0 && gfx.getBoxConnectedToPort(gfx.getOutputBox(), OutputPort.ALPHA_CLIP) != null)
            this.alphaLayer = 0x1;
        
        int fragmentID = glCreateShader(GL_FRAGMENT_SHADER);
        String source = GfxAssembler.generateBRDF(gfx, 0xDEADBEEF);
        glShaderSource(fragmentID, source);
        glCompileShader(fragmentID);
        if (glGetShaderi(fragmentID, GL_COMPILE_STATUS) == 0) {
            System.out.println(source);
            throw new RuntimeException("Could not compile fragment shader! " + glGetShaderInfoLog(fragmentID));            
        }


        this.programID = glCreateProgram();

        glAttachShader(this.programID, CraftworldRenderer.INSTANCE.getVertexShader());
        glAttachShader(this.programID, fragmentID);
        glLinkProgram(this.programID);

        if (glGetProgrami(this.programID, GL_LINK_STATUS) == 0)
            throw new RuntimeException("Could not link program!");

        glDeleteShader(fragmentID);

        this.getUniforms();
        this.textures = gfx.textures;

        // Preload textures
        for (ResourceDescriptor desc : gfx.textures)
            Texture.get(desc);

        PROGRAMS.put(descriptor, this);
    }

    private void getUniforms() {
        this.ambcol = glGetUniformLocation(this.programID, "ambcol");
        this.fogcol = glGetUniformLocation(this.programID, "fogcol");
        this.suncol = glGetUniformLocation(this.programID, "suncol");
        this.sunpos = glGetUniformLocation(this.programID, "sunpos");
        this.rimcol = glGetUniformLocation(this.programID, "rimcol");
        this.rimcol2 = glGetUniformLocation(this.programID, "rimcol2");

        this.view = glGetUniformLocation(this.programID, "view");
        this.projection = glGetUniformLocation(this.programID, "projection");
        for (int i = 0; i < MAX_BONES; ++i)
            this.matrices[i] = glGetUniformLocation(this.programID, "matrices[" + i + "]");
        
        this.campos = glGetUniformLocation(this.programID, "campos");

        this.lighscaleadd = glGetUniformLocation(this.programID, "lightscaleadd");

        this.color = glGetUniformLocation(this.programID, "thing_color");

        for (int i = 0; i < 8; ++i)
            this.locations[i] = glGetUniformLocation(this.programID, "s" + i);
    }

    public static Shader get(ResourceDescriptor descriptor) {
        if (descriptor == null) return CraftworldRenderer.INSTANCE.getFallbackShader();
        if (PROGRAMS.containsKey(descriptor))
            return PROGRAMS.get(descriptor);
        if (ResourceSystem.extract(descriptor) == null)
            return CraftworldRenderer.INSTANCE.getFallbackShader();
        return new Shader(descriptor);
    }

    public void setUniformFloat4(int location, Vector4f v) {
        if (location == -1) return;
        glUniform4f(location, v.x, v.y, v.z, v.w);
    }

    public void setUniformFloat3(int location, Vector3f v) {
        if (location == -1) return;
        glUniform3f(location, v.x, v.y, v.z);
    }

    public void setUniformFloat2(int location, Vector2f v) {
        if (location == -1) return;
        glUniform2f(location, v.x, v.y);
    }

    public void setUniformMatrix(int location, Matrix4f matrix) {
        if (location == -1) return;
        if (matrix == null) matrix = new Matrix4f().identity();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glUniformMatrix4fv(location, false, matrix.get(stack.mallocFloat(16)));
        }
    }

    public void bind(PLevelSettings lighting, Matrix4f[] matrices, Vector4f color) {
        glUseProgram(this.programID);
        
        // if (this.twoSided) glDisable(GL_CULL_FACE);
        // else glEnable(GL_CULL_FACE);

        if (this.alphaMode != 0) {
            glEnable(GL_BLEND);
            if (this.alphaMode == 4) 
                glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
            else if (this.alphaMode == 3)
                glBlendFunc(GL_ONE, GL_ONE);
            else if (this.alphaMode == 2)
                glBlendFunc(GL_SRC_ALPHA, GL_ONE);
            else
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        } else glDisable(GL_BLEND);
        

        // Set level lighting uniforms
        setUniformFloat4(this.ambcol, lighting.ambientColor);
        setUniformFloat4(this.fogcol, lighting.fogColor);
        setUniformFloat4(this.suncol, lighting.sunColor);
        setUniformFloat4(this.rimcol, lighting.rimColor);
        setUniformFloat4(this.rimcol2, lighting.rimColor2);
        setUniformFloat3(this.sunpos, new Vector3f(lighting.sunPosition).mul(lighting.sunPositionScale));

        // Set camera uniforms
        setUniformFloat3(this.campos, CraftworldRenderer.MAIN_CAMERA.getTranslation());
        setUniformFloat2(this.lighscaleadd, new Vector2f(lighting.sunMultiplier, lighting.exposure));
        
        setUniformMatrix(this.projection, CraftworldRenderer.MAIN_CAMERA.getProjectionMatrix());
        for (int i = 0; i < matrices.length; ++i)
            setUniformMatrix(this.matrices[i], matrices[i]);
        setUniformMatrix(this.view, CraftworldRenderer.MAIN_CAMERA.getViewMatrix());

        if (color != null)
            setUniformFloat4(this.color, color);

        for (int i = 0; i < 8; ++i) {
            if (this.textures[i] == null) continue;
            Texture texture = Texture.get(this.textures[i]);
            if (texture == null) continue;

            glActiveTexture(GL_TEXTURE0 + i);

            // glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            // glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

            glUniform1i(this.locations[i], i);
            glBindTexture(GL_TEXTURE_2D, texture.textureID);
        }

        glActiveTexture(GL_TEXTURE0);
    }

    public void delete() {
        glDeleteProgram(this.programID);
        PROGRAMS.remove(this.descriptor);
    }

}
