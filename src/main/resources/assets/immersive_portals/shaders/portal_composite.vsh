#version 330
// needed for "layout(location = 0)" as of MC 26.3
#extension GL_ARB_separate_shader_objects : require
// Quasar MC: the portal surface (or, while passing through a portal, a quad over the whole screen just past the near
// plane), in camera-relative world coordinates.

layout(location = 0) in vec3 Position;

layout(std140) uniform PortalUniforms
{
    mat4 uViewProj;  // main camera projection * view rotation
    vec4 uScreen;    // xy = main screen size in pixels, z = 1 for mirrors
    vec4 uRect;      // the portal view's NDC rectangle (x0, y0, x1, y1)
    vec4 uOrigin;    // portal centre, camera-relative; w = mode: 0 surface, 1 per-pixel ray test, 2 whole screen
    vec4 uAxisW;     // xyz = unit width axis, w = half width
    vec4 uAxisH;     // xyz = unit height axis, w = half height
};

out vec3 vRay;

void main()
{
    vRay = Position; // camera at the origin: the position is the view ray (interpolates linearly over the quad)
    gl_Position = uViewProj * vec4(Position, 1.0);
}
