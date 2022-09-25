#version 400 core

#define SAMPLE_2D(smp, uv) texture(smp, uv)
#define rsqrt(value) pow(value, -0.5)
#define saturate(value) max(0, min(1, value))
#define lerp(a, b, w) (a + w * (b - a))

in vec3 wpos;
in vec3 normal;
in vec3 tangent;
in vec4 uv;

out vec4 FragColor;

const vec4 iColor = vec4(1.0, 1.0, 1.0, 1.0);

const float CosinePower = ENV.COSINE_POWER;
const float BumpLevel = ENV.BUMP_LEVEL;
const float normal_mul = 0.15;
const float normal_add = 0.5;
const float rim_round = 0.9;

uniform sampler2D s0, s1, s2, s3, s4, s5, s6, s7;
uniform vec4 ambcol, fogcol, suncol, rimcol, rimcol2;
uniform vec3 sunpos, vec2eye;

vec4 BumpMap(vec3 normal, vec3 tangent, vec4 iSample) {
    half2 n = vec2(0.0, 1.0) - iSample.yw - 0.501953;
    half3 c = (n.y * tangent) + (n.x * cross(tangent, normal));
    float3 v = (-c * BumpLevel) + (normal.xyz / rsqrt(dot(n, n) - 0.25));
    return vec4(normalize(v), 1.0);
}

vec4 GetDiffuse() {
ENV.DIFFUSE
}

vec4 GetBump() {
ENV.BUMP
}

vec4 GetSpecular() {
ENV.SPECULAR
}

void main() {
    vec3 N = GetBump().xyz;
    vec3 V = normalize(vec2eye);
    vec3 R = reflect(V, N);
    vec3 L = normalize(vec2eye - sunpos);

    float NdotV = dot(N, V);
    float RdotL = dot(R, L);
    float NdotL = dot(N, L);

    vec3 ambientColor = ((N.y * normal_mul) + normal_add) * ambcol.rgb;
    vec3 sunColor = suncol.rgb;
    vec3 rimColor = (pow(saturate(NdotV + rim_round), 2) *
        lerp(rimcol.rgb, rimcol2.rgb, ((N.y + 1.0) / 2.0)));

    vec3 reflection = vec3(0.0);

    vec3 diffuseColor = ambientColor + sunColor + rimColor;
    float specularLobe = pow(saturate(-RdotL), CosinePower);

    vec3 prod = diffuseColor * diffuseColor;
    vec3 specular = GetSpecular().xyz;
    vec3 lighting = (((specularLobe * prod) * specular) + (specular * reflection)) * 2.0;

    vec3 Kd = GetDiffuse().xyz;
    FragColor = vec4((Kd * prod) + lighting, 1.0);

    FragColor.rgb = pow(FragColor.rgb, vec3(1.0 / 2.2));
}