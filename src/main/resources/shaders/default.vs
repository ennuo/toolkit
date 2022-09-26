#version 430 core

layout (location = 0) in vec4 iPosition;
layout (location = 1) in vec4 iNormal;
layout (location = 2) in vec4 iTangent;
layout (location = 3) in vec4 iUV;
layout (location = 4) in vec4 iBones;
layout (location = 5) in vec4 iBoneWeights;

uniform mat4 view;
uniform mat4 projection;
uniform mat4 matrices[100];

uniform vec3 campos;

out vec4 uv;
out vec3 tangent;
out vec3 normal;
out vec3 vec2eye;
out vec3 wpos;

void main() {
    mat4 skin =
        iBoneWeights.x * matrices[int(iBones.x)] +
        iBoneWeights.y * matrices[int(iBones.y)] +
        iBoneWeights.z * matrices[int(iBones.z)] +
        iBoneWeights.w * matrices[int(iBones.w)];

    // skin = matrices[0];
    
    vec4 worldPos = skin * iPosition;
    vec4 cameraPos = view * worldPos;

    vec2eye = worldPos.xyz - campos;

    wpos = worldPos.xyz;
    normal = normalize(mat3(skin) * iNormal.xyz);
    tangent = normalize(mat3(skin) * iTangent.xyz);

    // normal = iNormal.xyz;
    // tangent = iTangent.xyz;

    uv = iUV;

    gl_Position = projection * cameraPos;
}