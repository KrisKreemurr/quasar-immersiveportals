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
    // 26.x projects with NDC +y = up and flips rows when rasterizing, so pixel row 0 (top) is NDC y = +1, and the
    // portal image's row 0 is the top of its rectangle (y1). x runs left to right in both.
    vec2 p = gl_FragCoord.xy / uScreen.xy;
    vec2 ndc = vec2(p.x * 2.0 - 1.0, 1.0 - p.y * 2.0);
    vec2 uv = vec2((ndc.x - uRect.x) / (uRect.z - uRect.x),
                   (uRect.w - ndc.y) / (uRect.w - uRect.y));
    fragColor = vec4(texture(PortalColor, uv).rgb, 1.0);
}
