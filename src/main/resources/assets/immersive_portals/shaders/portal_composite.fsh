#version 330
// needed for "layout(location = 0)" as of MC 26.3
#extension GL_ARB_separate_shader_objects : require
// Quasar MC: fills the portal surface with the portal's view, pixel for pixel: the view is rendered at screen size
// with the same projection rows (only its culling is narrowed to the portal).
// Mode 1 (camera within a hand's width of the portal): drawn over the whole screen, and each pixel keeps the portal's
// view only where its view ray goes through the portal rectangle - exact at any distance and angle, and never cut by
// the near plane. Mode 2 (camera just past the plane, before the teleport tick): the whole screen.

layout(std140) uniform PortalUniforms
{
    mat4 uViewProj;
    vec4 uScreen;    // xy = screen size, z = 1 for mirrors, w = opacity (crossing hold)
    vec4 uRect;
    vec4 uOrigin;    // w = mode
    vec4 uAxisW;
    vec4 uAxisH;
    mat4 uPictureViewProj; // mode 3: projection * view rotation the picture was taken with
};

uniform sampler2D PortalColor;

layout(location = 0) in vec3 vRay;

layout(location = 0) out vec4 fragColor;

void main()
{
    bool rayTest = uOrigin.w > 0.5 && uOrigin.w < 1.5;
    bool lookBack = uOrigin.w > 3.5; // mode 4: third-person camera through the portal
    if (rayTest || lookBack) {
        vec3 n = cross(uAxisW.xyz, uAxisH.xyz);
        float denom = dot(vRay, n);
        bool hits = false;
        if (abs(denom) >= 1e-7) {
            float t = dot(uOrigin.xyz, n) / denom;
            vec3 p = vRay * t - uOrigin.xyz;
            hits = t > 0.0 && abs(dot(p, uAxisW.xyz)) <= uAxisW.w && abs(dot(p, uAxisH.xyz)) <= uAxisH.w;
        }
        // mode 1 keeps only what goes through the portal; mode 4 keeps everything else (looking back through the
        // portal shows the player's own world, already in the main image)
        if (hits == lookBack) discard;
    }
    // the view was rendered with the screen's projection rows (at a fraction of the screen's size): the same screen
    // fraction is the same texture position
    vec2 uv = gl_FragCoord.xy / uScreen.xy;
    if (uOrigin.w > 2.5) {
        // frozen picture: where this pixel's view ray landed in the picture. The screen<->NDC mapping (and its y
        // direction) is the same for both cameras, so shift by the NDC difference, signed by how NDC runs on screen.
        vec4 now = uViewProj * vec4(vRay, 1.0);
        vec4 then = uPictureViewProj * vec4(vRay, 1.0);
        vec2 ndcNow = now.xy / now.w;
        vec2 ndcThen = then.w > 1e-6 ? then.xy / then.w : ndcNow;
        vec2 s = vec2(dFdx(ndcNow.x) < 0.0 ? -1.0 : 1.0, dFdy(ndcNow.y) < 0.0 ? -1.0 : 1.0);
        uv = clamp(uv + s * (ndcThen - ndcNow) * 0.5, vec2(0.0), vec2(1.0));
    }
    if (uScreen.z > 0.5) {
        uv.x = 1.0 - uv.x; // mirror: the view was rendered unreflected
    }
    // w: opacity for the crossing hold's fade-out (0 = not set: opaque)
    fragColor = vec4(texture(PortalColor, uv).rgb, uScreen.w > 0.0 ? uScreen.w : 1.0);
}
