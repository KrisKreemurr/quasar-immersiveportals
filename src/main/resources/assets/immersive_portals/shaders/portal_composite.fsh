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
};

uniform sampler2D PortalColor;

layout(location = 0) in vec3 vRay;

layout(location = 0) out vec4 fragColor;

void main()
{
    if (uOrigin.w > 0.5 && uOrigin.w < 1.5) {
        vec3 n = cross(uAxisW.xyz, uAxisH.xyz);
        float denom = dot(vRay, n);
        if (abs(denom) < 1e-7) discard;
        float t = dot(uOrigin.xyz, n) / denom;
        if (t <= 0.0) discard;
        vec3 p = vRay * t - uOrigin.xyz;
        if (abs(dot(p, uAxisW.xyz)) > uAxisW.w || abs(dot(p, uAxisH.xyz)) > uAxisH.w) discard;
    }
    // the view was rendered with the screen's projection rows (at a fraction of the screen's size): the same screen
    // fraction is the same texture position
    vec2 uv = gl_FragCoord.xy / uScreen.xy;
    if (uScreen.z > 0.5) {
        uv.x = 1.0 - uv.x; // mirror: the view was rendered unreflected
    }
    // w: opacity for the crossing hold's fade-out (0 = not set: opaque)
    fragColor = vec4(texture(PortalColor, uv).rgb, uScreen.w > 0.0 ? uScreen.w : 1.0);
}
