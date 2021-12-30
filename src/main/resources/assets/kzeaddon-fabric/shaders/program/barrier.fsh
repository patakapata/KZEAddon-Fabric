#version 330

uniform vec4 color;

in vec4 vertColor;

void main(void) {
    vec4 finalColor = color * vertColor;

    if (finalColor.a <= 0.0) discard;

    gl_FragColor = finalColor;
}
