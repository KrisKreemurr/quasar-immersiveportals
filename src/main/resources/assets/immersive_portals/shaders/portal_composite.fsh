#version 330
// needed for "layout(location = 0)" as of MC 26.3
#extension GL_ARB_separate_shader_objects : require
// Quasar MC: fills the portal surface with the portal's view, pixel for pixel (the view was rendered with the
// same screen size and projection rows, so the screen position is the texture position).

layout(std140) uniform PortalUniforms
{
    mat4 uViewProj;
    vec4 uScreen;
};

uniform sampler2D PortalColor;

layout(location = 0) out vec4 fragColor;

void main()
{
    vec2 uv = gl_FragCoord.xy / uScreen.xy;
    fragColor = vec4(texture(PortalColor, uv).rgb, 1.0);
}
