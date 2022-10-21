#version 330 core

#define SAMPLE_2D(smp, uv) texture(smp, uv)
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

const float normal_mul = 0.15;
const float normal_add = 0.5;
const float rim_round = 0.9;

uniform vec4 thing_color = vec4(1.0, 1.0, 1.0, 1.0);

uniform vec4 ambcol, fogcol, suncol, rimcol, rimcol2;
uniform vec3 sunpos;
uniform vec2 lightscaleadd;

void main() {
    vec3 N = normalize(normal);
    vec3 V = normalize(vec2eye);
    vec3 R = reflect(V, N);
    vec3 L = normalize((vec2eye - sunpos) * lightscaleadd.x);

    float NdotV = dot(N, V);
    float RdotL = dot(R, L);
    float NdotL = dot(N, L);

    vec3 ambientColor = ((N.y * normal_mul) + normal_add) * ambcol.rgb;
    vec3 sunColor = suncol.rgb * saturate(-NdotL);
    vec3 rimColor = (pow(saturate(NdotV + rim_round), 2) *
        lerp(rimcol.rgb, rimcol2.rgb, ((N.y + 1.0) / 2.0)));

    vec3 reflection = vec3(0.0);

    vec3 diffuseColor = ambientColor + sunColor + rimColor;
    FragColor = vec4(thing_color.rgb * diffuseColor * diffuseColor, 1.0);

    FragColor.rgb = sqrt(abs(FragColor.rgb));
    float factor = max(max(FragColor.x, max(FragColor.y, FragColor.z)), 1.0);
    FragColor = vec4(
        FragColor.xyz / factor,
        1.0
    );
}