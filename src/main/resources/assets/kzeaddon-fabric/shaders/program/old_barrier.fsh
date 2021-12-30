#version 110

uniform vec3 color;

varying float alpha;

void main(void) {
    gl_FragColor = vec4(color, alpha);
}
