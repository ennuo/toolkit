#version 330 core

#define SAMPLE_2D(smp, uv) texture(smp, uv)
#define SAMPLE_2D_BIAS(smp, uv, bias) texture(smp, uv, bias)
#define rsqrt(value) pow(value, -0.5)
#define saturate(value) max(0, min(1, value))
#define lerp(a, b, w) (a + w * (b - a))

in vec4 uv;
in vec3 tangent;
in vec3 normal;
in vec3 vec2eye;
in vec3 wpos;
in vec4 shadowmap_position;

out vec4 FragColor;

uniform vec4 thing_color;

uniform sampler2D s0, s1, s2, s3, s4, s5, s6, s7;
uniform sampler2D shadowtex;
uniform sampler2D cbuf;
uniform vec4 ambcol, fogcol, suncol, rimcol, rimcol2;
uniform vec3 sunpos;
uniform vec2 lightscaleadd;

vec4 GetDiffuse() {
ENV.AUTO_DIFFUSE_SETUP
}

void main() {
    FragColor = vec4(
        GetDiffuse().xyz,
        1.0
    );
}