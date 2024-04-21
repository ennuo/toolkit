#version 420 core
layout (location = 0) in vec4 iVertex;

out vec2 TexCoord;

void main()
{
    vec2 pos = 1.0 - (2.0 * iVertex.xy);
    gl_Position = vec4(pos, 0.0, 1.0);
    TexCoord = iVertex.zw;
}