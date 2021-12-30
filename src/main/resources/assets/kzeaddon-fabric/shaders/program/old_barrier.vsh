#version 110

uniform mat4 mvp;
uniform float fadeRadius;
uniform float useFade;
uniform vec3 center;

attribute vec3 position;
attribute vec3 offset;

varying float alpha;

void main(void) {
    float distance = distance(center, offset);

    if (useFade == 1.0) {
        alpha = 1.0 - clamp(distance / fadeRadius, 0.0, 1.0);
    } else if (fadeRadius == -1.0) {
        alpha = 1.0;
    } else {
        alpha = distance <= fadeRadius ? 1.0 : 0.0;
    }

    gl_Position = mvp * vec4(position + offset, 1.0);
}
