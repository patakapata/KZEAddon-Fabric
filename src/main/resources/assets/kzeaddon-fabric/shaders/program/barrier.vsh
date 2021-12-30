#version 330

uniform mat4 MVP;
uniform vec3 center;
uniform float fadeRadius;
uniform float useFade;

layout(location = 0) in vec3 inVertex;
layout(location = 1) in vec3 inOffset;

out vec4 vertColor;

void main(void) {
    float distance = distance(center, inOffset);
    float alpha;

    if (useFade == 1) {
        alpha = 1.0 - clamp(distance / fadeRadius, 0.0, 1.0);
    } else if(fadeRadius == -1) {
        alpha = 1.0;
    } else {
        alpha = distance <= fadeRadius ? 1.0 : 0.0;
    }

    gl_Position = MVP * vec4(inVertex + inOffset, 1.0);
    vertColor = vec4(vec3(1.0), alpha);
}
