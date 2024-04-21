#version 420 core

#define SAMPLE_2D(smp, uv) texture(smp, uv)
#define SAMPLE_2D_BIAS(smp, uv, bias) texture(smp, uv, bias)
#define rsqrt(value) pow(value, -0.5)
#define saturate(value) max(0, min(1, value))
#define lerp(a, b, w) (a + w * (b - a))

#define float1 float
#define float2 vec2
#define float3 vec3
#define float4 vec4
#define half vec

in vec4 uv;
in vec2 decal_uv;

out vec4 PixelColor;

const float AlphaTestLevel = ENV.ALPHA_TEST_LEVEL;
const vec4 thing_color = vec4(1.0, 1.0, 1.0, 1.0);

layout (binding = 0) uniform sampler2D s0;
layout (binding = 1) uniform sampler2D s1;
layout (binding = 2) uniform sampler2D s2;
layout (binding = 3) uniform sampler2D s3;
layout (binding = 4) uniform sampler2D s4;
layout (binding = 5) uniform sampler2D s5;
layout (binding = 6) uniform sampler2D s6;
layout (binding = 7) uniform sampler2D s7;

vec4 GetAlpha()
{
ENV.ALPHA_SETUP
}

vec4 GetSample()
{
ENV.SAMPLE_SETUP
}

void main()
{
    if (GetAlpha().w < AlphaTestLevel) discard;
    PixelColor = GetSample();
}