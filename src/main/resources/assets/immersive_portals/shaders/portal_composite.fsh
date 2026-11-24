#version 330
// needed for "layout(location = 0)" as of MC 26.3
#extension GL_ARB_separate_shader_objects : require
// Quasar MC: fills the portal surface with the portal's view, pixel for pixel: the view is rendered at screen size
// with the same projection rows (only its culling is narrowed to the portal).

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
    // the view was rendered with the screen's projection rows (at a fraction of the screen's size): the same screen
    // fraction is the same texture position
    vec2 uv = gl_FragCoord.xy / uScreen.xy;
    fragColor = vec4(texture(PortalColor, uv).rgb, 1.0);
}
