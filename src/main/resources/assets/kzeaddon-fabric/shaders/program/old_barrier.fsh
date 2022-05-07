#version 110

uniform vec3 color;
uniform float useFade;

varying float alpha;

void main(void) {
    gl_FragColor = vec4(color, alpha);
}
