package cwlib.gl.objects;

import static org.lwjgl.opengl.GL30.*;

public class GlProgram
{
    private int id;

    public GlProgram(String vertexSource, String fragmentSource)
    {
        int vertex = compileShader(vertexSource, GL_VERTEX_SHADER);
        int fragment = compileShader(fragmentSource, GL_FRAGMENT_SHADER);
        id = compileProgram(vertex, fragment);

        glDeleteShader(vertex);
        glDeleteShader(fragment);
    }

    public void bind()
    {
        glUseProgram(id);
    }

    public static int compileProgram(int vertex, int fragment)
    {
        int program = glCreateProgram();
        glAttachShader(program, vertex);
        glAttachShader(program, fragment);
        glLinkProgram(program);
        if (glGetProgrami(program, GL_LINK_STATUS) == 0)
            throw new AssertionError("Could not link shader program! " + glGetProgramInfoLog(program));
        return program;
    }

    public static int compileShader(String source, int type)
    {
        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == 0)
        {
            System.out.println(source);
            throw new AssertionError("Could not compile shader! " + glGetShaderInfoLog(shader));
        }
        return shader;
    }

    public void Destroy()
    {
        if (id != 0)
        {
            glDeleteProgram(id);
            id = 0;
        }
    }
}
