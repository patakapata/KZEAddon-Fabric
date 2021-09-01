#version 330

uniform vec4 color;

in vec4 vertColor;

void main(void) {
    gl_FragColor = color * vertColor;
}
