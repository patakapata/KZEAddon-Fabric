#version 330 core

uniform float time;

in vec4 fragment_color;

out vec4 color;

void main() {
    color = fragment_color;
}