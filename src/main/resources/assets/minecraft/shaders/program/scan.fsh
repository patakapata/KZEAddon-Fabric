#version 120

uniform mat4 invViewMat;
uniform mat4 invProjMat;
uniform vec3 pos;
uniform vec3 center;
uniform sampler2D depthTex;
uniform float time;
uniform float radius;
uniform float width;
uniform float directDepthDisplay;

varying vec2 texCoord;

vec3 modelpos(float depth) {
    float z = depth * 2.0 - 1.0;
    vec4 clipSpacePos = vec4(texCoord * 2.0 - 1.0, z, 1.0);
    vec4 viewSpacePos = invProjMat * clipSpacePos;
    viewSpacePos /= viewSpacePos.w;
    vec4 worldSpacePos = invViewMat * viewSpacePos;

    return worldSpacePos.xyz;
}

float oMin(float a, float b, float c) {
    return min(min(a, b), c);
}

float oMax(float a, float b, float c) {
    return max(max(a, b), c);
}

void main() {
    float depth = texture2D(depthTex, texCoord).r;

    if(directDepthDisplay != 0.0) {
        gl_FragColor = vec4(vec3(depth), 1.0);
    } else {
        vec3 p = modelpos(depth);
        float length = width / abs(distance(center, p) - radius);

        gl_FragColor = vec4(0.0, 1.0, 0.0, clamp(length, 0.0, 1.0));
    }
}
