#version 330 core

layout(location = 0) in vec3 vertex_position;
layout(location = 1) in vec4 vertex_color;

uniform mat4 MVP;

out vec4 fragment_color;

void main() {
    gl_Position = MVP * vec4(vertex_position, 1.0);

    float red = mod(vertex_position.x, 16.0) / 16.0;
    float green = mod(vertex_position.y, 16.0) / 16.0;
    float blue = mod(vertex_position.z, 16.0) / 16.0;

    fragment_color = vec4(red, green, blue, 1.0);
}
