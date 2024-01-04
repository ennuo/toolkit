#version 330 core

layout (location = 0) in vec4 iPosition;
layout (location = 1) in vec4 iNormal;
layout (location = 2) in vec4 iTangent;
layout (location = 3) in vec4 iUV;
layout (location = 4) in vec4 iBones;
layout (location = 5) in vec4 iBoneWeights;

uniform mat4 light;
uniform mat4 view;
uniform mat4 projection;
uniform mat4 matrices[100];


uniform sampler2D morph_lut;
uniform float target_weights[32];

uniform vec3 campos;

out vec4 uv;
out vec3 tangent;
out vec3 normal;
out vec3 vec2eye;
out vec3 wpos;
out vec4 shadowmap_position;

void main() {
    mat4 skin =
        iBoneWeights.x * matrices[int(iBones.x)] +
        iBoneWeights.y * matrices[int(iBones.y)] +
        iBoneWeights.z * matrices[int(iBones.z)] +
        iBoneWeights.w * matrices[int(iBones.w)];


    vec4 worldPos = vec4(iPosition.xyz, 1.0);
    vec4 worldNormal = vec4(iNormal.xyz, 1.0);

    const int MAX_TARGET_COUNT = 32;
    const int BLOCK_SIZE = (1024 * 512) / MAX_TARGET_COUNT;
    for (int i = 0; i < MAX_TARGET_COUNT; i++) {
        if (target_weights[i] == 0.0) continue;

        float offset = (BLOCK_SIZE * i) + gl_VertexID;
        vec2 coord = vec2(
            (mod(offset, 1024.0) / 1024.0),
            ((offset / 1024.0) / 1024.0)
        );

        vec4 target_pos = texture2D(morph_lut, coord);
        vec4 target_normal = texture2D(morph_lut, coord + vec2(0.0, 0.5));

        worldPos += (target_pos * target_weights[i]);
        worldNormal += (target_normal * target_weights[i]);
    }
    
    worldPos = skin * worldPos;
    vec4 cameraPos = view * worldPos;

    vec2eye = worldPos.xyz - campos;

    wpos = worldPos.xyz;
    normal = normalize(mat3(skin) * worldNormal.xyz);
    tangent = normalize(mat3(skin) * iTangent.xyz);

    uv = iUV;

    shadowmap_position = light * worldPos;

    gl_Position = projection * cameraPos;
}