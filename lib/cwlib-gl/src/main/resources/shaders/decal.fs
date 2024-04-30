#version 420 core

in vec2 TexCoord;
out vec4 FragColor;

layout (binding = 0) uniform sampler2D s_Decal;

void main(void) 
{
    FragColor = texture(s_Decal, TexCoord);
}