#version 330
// needed for "layout(location = 0)" as of MC 26.3
#extension GL_ARB_separate_shader_objects : require
// Quasar MC: full-screen triangle for painting a portal's far-terrain panorama.

layout(location = 0) in vec3 Position;
layout(location = 0) out vec2 vNdc;

void main()
{
    vNdc = Position.xy;
    gl_Position = vec4(Position.xy, 0.0, 1.0);
}
