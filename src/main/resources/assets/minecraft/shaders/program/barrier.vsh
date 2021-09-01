#version 330

uniform mat4 MVP;
uniform vec3 center;
uniform float radius;

layout(location = 0) in vec3 inVertex;
layout(location = 1) in vec3 inOffset;

out vec4 vertColor;

void main(void) {
    float distance = distance(center, inOffset);
    gl_Position = MVP * vec4(inVertex + inOffset, 1.0);
    vertColor = vec4(vec3(1.0), 1.0 - clamp(distance / radius, 0.0, 1.0));
}
