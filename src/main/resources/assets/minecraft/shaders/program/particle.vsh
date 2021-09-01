#version 330

uniform mat4 MVP;
uniform float time;
uniform vec3 cam_right_ws;
uniform vec3 cam_up_ws;

layout(location = 0) in vec3 modelspace_vertex;
layout(location = 1) in vec4 offset;

void main(void) {
    vec3 vertexPosition_worldspace =
    offset.xyz
    + cam_right_ws * modelspace_vertex.x * offset.w
    + cam_up_ws * modelspace_vertex.y * offset.w;

    gl_Position = MVP * vec4(vertexPosition_worldspace, 1.0);
}
