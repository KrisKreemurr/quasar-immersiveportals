#version 330
// needed for "layout(location = 0)" as of MC 26.3
#extension GL_ARB_separate_shader_objects : require
// Quasar MC: paints Quasar Horizons' portal panorama (far LOD terrain, a cube of colour + depth taken from a point
// near the portal camera) into a portal view, with the same parallax correction as the main LOD panorama.

layout(location = 0) in vec2 vNdc;
layout(location = 0) out vec4 fragColor;

layout(std140) uniform PanoInfo
{
    mat4 uFaceVP[6];      // face view-projections (relative to the panorama centre), DH's depth conventions
    mat4 uInvFaceVP[6];
    mat4 uInvRay;         // inverse of (projection * view) of the portal camera, for view rays
    mat4 uPortalVP;       // the portal camera's (oblique, reverse-Z) projection * view, for the depth we write
    vec4 uCenterOffset;   // panorama centre minus portal camera position
    float uFarDepth;      // depth of an empty face pixel
    int uZeroToOne;       // DH depth range is 0..1
};

uniform sampler2D C0;
uniform sampler2D C1;
uniform sampler2D C2;
uniform sampler2D C3;
uniform sampler2D C4;
uniform sampler2D C5;
uniform sampler2D D0;
uniform sampler2D D1;
uniform sampler2D D2;
uniform sampler2D D3;
uniform sampler2D D4;
uniform sampler2D D5;

ivec2 texelFor(sampler2D s, vec2 uv)
{
    ivec2 size = textureSize(s, 0);
    return clamp(ivec2(uv * vec2(size)), ivec2(0), size - 1);
}

vec4 fetchColor(int f, vec2 uv)
{
    if (f == 0) return texelFetch(C0, texelFor(C0, uv), 0);
    if (f == 1) return texelFetch(C1, texelFor(C1, uv), 0);
    if (f == 2) return texelFetch(C2, texelFor(C2, uv), 0);
    if (f == 3) return texelFetch(C3, texelFor(C3, uv), 0);
    if (f == 4) return texelFetch(C4, texelFor(C4, uv), 0);
    return texelFetch(C5, texelFor(C5, uv), 0);
}

float fetchDepth(int f, vec2 uv)
{
    if (f == 0) return texelFetch(D0, texelFor(D0, uv), 0).r;
    if (f == 1) return texelFetch(D1, texelFor(D1, uv), 0).r;
    if (f == 2) return texelFetch(D2, texelFor(D2, uv), 0).r;
    if (f == 3) return texelFetch(D3, texelFor(D3, uv), 0).r;
    if (f == 4) return texelFetch(D4, texelFor(D4, uv), 0).r;
    return texelFetch(D5, texelFor(D5, uv), 0).r;
}

float depthToNdc(float d) { return (uZeroToOne != 0) ? d : d * 2.0 - 1.0; }

int faceFor(vec3 dir)
{
    vec3 a = abs(dir);
    if (a.x >= a.y && a.x >= a.z) return (dir.x > 0.0) ? 0 : 1;
    if (a.y >= a.z)               return (dir.y > 0.0) ? 2 : 3;
    return (dir.z > 0.0) ? 4 : 5;
}

bool lookup(vec3 dirFromCenter, out vec3 posFromCenter, out int f, out vec2 uv)
{
    f = faceFor(dirFromCenter);
    vec4 faceClip = uFaceVP[f] * vec4(dirFromCenter, 1.0);
    if (faceClip.w <= 0.0) return false;
    uv = (faceClip.xy / faceClip.w) * 0.5 + 0.5;
    float d = fetchDepth(f, uv);
    if (d == uFarDepth) return false;
    vec4 pc = uInvFaceVP[f] * vec4(uv * 2.0 - 1.0, depthToNdc(d), 1.0);
    posFromCenter = pc.xyz / pc.w;
    return true;
}

void main()
{
    vec4 p = uInvRay * vec4(vNdc, 0.5, 1.0);
    vec3 ray = normalize(p.xyz / p.w);
    vec3 camFromCenter = -uCenterOffset.xyz;

    vec3 posFromCenter;
    int f;
    vec2 uv;
    if (!lookup(normalize(camFromCenter + ray * 1000.0), posFromCenter, f, uv)) discard;
    for (int i = 0; i < 3; i++)
    {
        float t = max(dot(posFromCenter - camFromCenter, ray), 1.0);
        vec3 guess = camFromCenter + ray * t;
        vec3 refinedPos;
        int refinedFace;
        vec2 refinedUv;
        if (!lookup(normalize(guess), refinedPos, refinedFace, refinedUv)) discard;
        posFromCenter = refinedPos;
        f = refinedFace;
        uv = refinedUv;
    }

    vec3 posFromCamera = posFromCenter + uCenterOffset.xyz;
    if (dot(posFromCamera, ray) <= 0.0) discard;
    vec4 clip = uPortalVP * vec4(posFromCamera, 1.0);
    if (clip.w <= 0.0) discard;
    float depth = clip.z / clip.w;
    if (depth < 0.0 || depth > 1.0) discard; // behind the portal plane or outside the far plane
    gl_FragDepth = depth;
    fragColor = vec4(fetchColor(f, uv).rgb, 1.0);
}
