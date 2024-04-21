#version 420 core

layout (location = 0) in vec4 iUV;
layout (location = 1) in vec2 iDecalUV;
layout (location = 2) in vec2 iImageUV;

out vec4 uv;
out vec2 decal_uv;

void main() {
    uv = iUV;
    decal_uv = iDecalUV;
    
    vec2 pos = 1.0 - (2.0 * iImageUV.xy);
    gl_Position = vec4(pos, 0.0, 1.0);
}