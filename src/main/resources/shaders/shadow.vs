#version 330 core

layout (location = 0) in vec4 iPosition;
layout (location = 1) in vec4 iNormal;
layout (location = 2) in vec4 iTangent;
layout (location = 3) in vec4 iUV;
layout (location = 4) in vec4 iBones;
layout (location = 5) in vec4 iBoneWeights;

uniform mat4 light;
uniform mat4 matrices[100];

void main() {
    mat4 skin =
        iBoneWeights.x * matrices[int(iBones.x)] +
        iBoneWeights.y * matrices[int(iBones.y)] +
        iBoneWeights.z * matrices[int(iBones.z)] +
        iBoneWeights.w * matrices[int(iBones.w)];

    vec4 worldPos = skin * vec4(iPosition.xyz, 1.0);

    gl_Position = light * worldPos;

}