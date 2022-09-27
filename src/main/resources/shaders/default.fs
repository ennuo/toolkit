#version 400 core

#define SPECULAR (1 << 9)
#define NORMAL (1 << 10)
#define ALPHA (1 << 11)
#define REFRACT (1 << 12)
#define GLOW (1 << 13)
#define GLASS (1 << 14)
#define ST7 (1 << 22)
#define ANISO (1 << 16)
#define TRANS (1 << 17)
#define COLOR_CORRECTION (1 << 18)
#define FUZZ (1 << 19)
#define BRDF_REFLECTANCE (1 << 20)
#define LIGHTING_RAMP (1 << 21)

#define BLEND_MODE_DISABLE 0
#define BLEND_MODE_ALPHA_BLEND 1
#define BLEND_MODE_ADDITIVE 2
#define BLEND_MODE_ADDITIVE_NO_ALPHA 3
#define BLEND_MODE_PREMULTIPLIED_ALPHA 4

#define PROPERTIES ENV.MATERIAL_PROPERTIES
#define ALPHA_MODE ENV.ALPHA_MODE

#define SAMPLE_2D(smp, uv) texture(smp, uv)
#define rsqrt(value) pow(value, -0.5)
#define saturate(value) max(0, min(1, value))
#define lerp(a, b, w) (a + w * (b - a))

in vec4 uv;
in vec3 tangent;
in vec3 normal;
in vec3 vec2eye;
in vec3 wpos;

out vec4 FragColor;

const float alpha_test_level = ENV.ALPHA_TEST_LEVEL;
const float cosine_power = ENV.COSINE_POWER;
const float bump_level = ENV.BUMP_LEVEL;
const float reflection_blur = ENV.REFLECTION_BLUR;

const float normal_mul = 0.15;
const float normal_add = 0.5;
const float rim_round = 0.9;

uniform vec4 thing_color;

uniform sampler2D s0, s1, s2, s3, s4, s5, s6, s7;
uniform vec4 ambcol, fogcol, suncol, rimcol, rimcol2;
uniform vec3 sunpos;
uniform vec2 lightscaleadd;

vec4 BumpMap(vec3 normal, vec3 tangent, vec4 iSample) {
    half2 n = vec2(0.0, 1.0) - iSample.yw - 0.501953;
    half3 c = (n.y * tangent) + (n.x * cross(tangent, normal));
    float3 v = (-c * bump_level) + (normal.xyz / rsqrt(dot(n, n) - 0.25));
    return vec4(normalize(v), 1.0);
}

vec4 GetDiffuse() {
ENV.AUTO_DIFFUSE_SETUP
}

vec4 GetBump() {
ENV.AUTO_NORMAL_SETUP
}

vec4 GetSpecular() {
ENV.AUTO_SPECULAR_SETUP
}

vec4 GetAlpha() {
ENV.AUTO_ALPHA_SETUP
}

void main() {
    vec3 N = GetBump().xyz;
    vec3 V = normalize(vec2eye);
    vec3 R = reflect(V, N);
    vec3 L = normalize((vec2eye - sunpos) * lightscaleadd.x);

    float NdotV = dot(N, V);
    float RdotL = dot(R, L);
    float NdotL = dot(N, L);

    vec3 ambientColor = ((N.y * normal_mul) + normal_add) * ambcol.rgb;
    vec3 sunColor = suncol.rgb;
    vec3 rimColor = (pow(saturate(NdotV + rim_round), 2) *
        lerp(rimcol.rgb, rimcol2.rgb, ((N.y + 1.0) / 2.0)));

    vec3 reflection = vec3(0.0);

    vec3 diffuseColor = ambientColor + sunColor + rimColor;
    float specularLobe = pow(saturate(-RdotL), cosine_power);

    vec3 prod = diffuseColor * diffuseColor;
    vec3 specular = GetSpecular().xyz;
    vec3 lighting = (((specularLobe * prod) * specular) + (specular * reflection)) * 2.0;

    vec3 Kd = GetDiffuse().xyz;
    FragColor = vec4((Kd * prod) + lighting, 1.0);

    FragColor.x = sqrt(FragColor.x);
    FragColor.y = sqrt(FragColor.y);
    FragColor.z = sqrt(FragColor.z);

    float factor = max(max(FragColor.x, max(FragColor.y, FragColor.z)), 1.0);

    float alpha_level = 1.0;
    #if (PROPERTIES & ALPHA)
        vec4 alpha = GetAlpha();

        #if (ALPHA_MODE == BLEND_MODE_PREMULTIPLIED_ALPHA)
            alpha_level = alpha.x;
        #elif (ALPHA_MODE != BLEND_MODE_DISABLE)
            alpha_level = alpha.w;
            if (alpha.w < alpha_test_level) alpha_level = 0.0;
        #else
            if (alpha.w < alpha_test_level) discard;
        #endif
        
    #endif

    FragColor = vec4(
        FragColor.xyz / factor,
        alpha_level
    );
}