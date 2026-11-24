#version 330
// needed for "layout(location = 0)" as of MC 26.3
#extension GL_ARB_separate_shader_objects : require
// Quasar MC: fills the portal surface with the portal's view, pixel for pixel (the view was rendered for just the
// portal's screen rectangle, so a screen position maps into the texture through that rectangle).

layout(std140) uniform PortalUniforms
{
    mat4 uViewProj;
    vec4 uScreen;
    vec4 uRect;
};

uniform sampler2D PortalColor;

layout(location = 0) out vec4 fragColor;

void main()
{
    // screen pixel -> NDC (the axis convention the view was rendered in) -> position inside the portal rectangle
    vec2 ndc = gl_FragCoord.xy / uScreen.xy * 2.0 - 1.0;
    vec2 uv = (ndc - uRect.xy) / (uRect.zw - uRect.xy);
    fragColor = vec4(texture(PortalColor, uv).rgb, 1.0);
}
