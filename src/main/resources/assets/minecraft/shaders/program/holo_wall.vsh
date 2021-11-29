#version 330

layout(location = 0) in vec3 inVertex;

void main(void) {
    gl_Position = vec4(inVertex, 1.0);
}
