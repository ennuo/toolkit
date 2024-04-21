#version 330 core

#define NO_FLAGS (0)
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

#define SHADER_MODEL_BRDF 0
#define SHADER_MODEL_PBR 1
#define SHADER_MODEL_UNLIT 2

#define PROPERTIES ENV.MATERIAL_PROPERTIES
#define ALPHA_MODE ENV.ALPHA_MODE
#define SHADER_MODEL ENV.SHADER_MODEL

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
in vec3 tangent;
in vec3 normal;
in vec3 vec2eye;
in vec3 wpos;
in vec4 shadowmap_position;

out vec4 FragColor;

const float alpha_test_level = ENV.ALPHA_TEST_LEVEL;
const float cosine_power = ENV.COSINE_POWER;
const float bump_level = ENV.BUMP_LEVEL;
const float ReflectionBlur = ENV.REFLECTION_BLUR;

const float normal_mul = 0.15;
const float normal_add = 0.5;
const float rim_round = 0.9;
const float RefractiveIndex = ENV.REFRACTIVE_INDEX;
const vec2 oores = vec2(0.00078125, 0.00138889);

uniform vec4 thing_color;

uniform sampler2D s0, s1, s2, s3, s4, s5, s6, s7;
uniform sampler2D shadowtex;
uniform sampler2D cbuf;
uniform vec4 ambcol, fogcol, suncol, rimcol, rimcol2;
uniform vec3 sunpos;
uniform vec2 lightscaleadd;

vec4 BumpMap(vec3 iNormal, vec3 iTangent, vec4 iBump) {
    vec2 bump = iBump.yw - 0.501961;
    vec3 bitangent = cross(iTangent.xyz, iNormal.xyz);
    vec3 n = normalize(
        (iNormal.xyz * sqrt(abs(dot(bump, bump) - 0.25))) - 
        (bump_level * ((bump.x * bitangent) + (bump.y * iTangent.xyz)))
    );

    return vec4(n, 1.0);
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

vec4 GetGlow() {
ENV.AUTO_GLOW_SETUP
}

vec4 GetReflection(vec2 iReflectCoord) {
ENV.AUTO_REFLECTION_SETUP
}

void main() {
    vec3 N = GetBump().xyz;
    vec3 V = normalize(vec2eye);
    vec3 R = -reflect(V, N);
    vec3 L = normalize((vec2eye - sunpos) * (100177.7265625));

    // 47 C3 A8 DD 47 CD 69 FC

    float NdotV = dot(N, V);
    float RdotL = dot(R, L);
    float NdotL = dot(N, L);

    float shadow = 1.0;
    float ao = 1.0;
    
    vec3 ambientColor = ((N.y * normal_mul) + normal_add) * ambcol.rgb;
    vec3 sunColor = suncol.rgb * saturate(-NdotL) * shadow;
    vec3 rimColor = shadow * clamp((pow(saturate(NdotV + rim_round), 2) *
        lerp(rimcol.rgb, rimcol2.rgb, ((N.y + 1.0) / 2.0))), 0.0, 1.0);
    
    vec3 diffuseColor = ambientColor + sunColor + rimColor;
    float specularLobe = pow(saturate(RdotL), cosine_power);

    vec3 Kd = GetDiffuse().rgb;
    
    vec3 prod = diffuseColor * diffuseColor;

    vec3 reflection = vec3(0.0);
    if ((PROPERTIES & REFRACT) != 0) {
        reflection = GetReflection(reflect(V, N).xy).rgb;
        reflection *= dot(prod, float3(0.299072, 0.568914, 0.114014));
    }

    vec3 lighting = reflection;

    if ((PROPERTIES & SPECULAR) != 0) {
        vec3 specular = GetSpecular().rgb;
        lighting = (((specularLobe * prod) * specular) + (specular * reflection)) * 2.0;
    } else lighting = reflection;

    if ((PROPERTIES & GLASS) != 0) {
        vec2 spos = gl_FragCoord.xy * oores;
        vec2 cuv = (N.xy * (RefractiveIndex * vec2(9.0, -16.0))) + vec2(spos.x, 1.0 - spos.y);
        float n = saturate((dot(N.xy, N.xy) * 10.0) - 0.5);
        FragColor = vec4(
            (prod * pow(n, 2)) + ((Kd) * texture(cbuf, cuv).rgb) + (lighting * 2.0),
            1.0
        );
    } else {
        FragColor = vec4((Kd * prod) + lighting, 1.0);
    }

    if ((PROPERTIES & GLOW) != 0) {
        vec3 glow = GetGlow().rgb;
        FragColor.rgb += glow;
    }

    if ((PROPERTIES & GLASS) == 0) {
        FragColor.rgb = sqrt(abs(FragColor.rgb));
    }

    float alpha_level = 1.0;

    if ((PROPERTIES & ALPHA) != 0) {
        vec4 alpha = GetAlpha();
        if (ALPHA_MODE == BLEND_MODE_PREMULTIPLIED_ALPHA) alpha_level = alpha.x;
        else if (ALPHA_MODE != BLEND_MODE_DISABLE) {
            alpha_level = alpha.w;
            if (alpha.w < alpha_test_level) alpha_level = 0.0;
        } else {
            if (alpha.w < alpha_test_level) discard;
        }
    }

    if (((PROPERTIES & ALPHA) != 0) && ((PROPERTIES & GLOW) != 0)) {
        FragColor.rgb *= alpha_level;
    }

    FragColor = vec4(
        FragColor.rgb,
        alpha_level
    );
}