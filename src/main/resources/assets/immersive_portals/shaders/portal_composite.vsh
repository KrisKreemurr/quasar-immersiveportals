#version 330
// needed for "layout(location = 0)" as of MC 26.3
#extension GL_ARB_separate_shader_objects : require
// Quasar MC: the portal surface, in camera-relative world coordinates.

layout(location = 0) in vec3 Position;

layout(std140) uniform PortalUniforms
{
    mat4 uViewProj;  // main camera projection * view rotation
    vec4 uScreen;    // xy = size of the portal view image in pixels
};

void main()
{
    gl_Position = uViewProj * vec4(Position, 1.0);
}
