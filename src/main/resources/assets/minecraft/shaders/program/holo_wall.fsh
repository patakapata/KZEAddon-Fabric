#version 330

uniform sampler2D depthTexture;

uniform vec3 position;
uniform vec3 center;
uniform mat4 invProjMat;
uniform mat4 invViewMat;
uniform vec2 resolution;
uniform float visibleDistance;

out vec4 finalColor;

vec3 worldpos(float depth) {
    float z = depth * 2.0 - 1.0;
    vec4 clipSpacePosition = vec4(gl_FragCoord.xy / resolution.xy * 2 - 1.0, z, 1.0);
    vec4 viewSpacePosition = invProjMat * clipSpacePosition;
    viewSpacePosition /= viewSpacePosition.w;
    vec4 worldSpacePosition = invViewMat * viewSpacePosition;

    return position + worldSpacePosition.xyz;
}

void main(void) {
    float depth = texture2D(depthTexture, gl_FragCoord.xy / resolution.xy).r;
    vec3 pos = worldpos(depth);
    float dist = distance(pos, center);

    if(dist / visibleDistance > 3.0) discard;

    finalColor = vec4(vec3(1), clamp(mod(dist, visibleDistance) / visibleDistance, 0.0, 1.0));
}
