#version 120

uniform float time;

varying vec2 texCoord;

void main() {
    texCoord = gl_MultiTexCoord0.st;
    gl_Position = ftransform();
}
