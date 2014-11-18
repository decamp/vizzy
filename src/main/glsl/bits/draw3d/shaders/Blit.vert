#version 330

uniform mat4 PROJ_VIEW_MAT;

layout( location = 0 ) in vec4 inVert;
layout( location = 1 ) in vec2 inTex0;

smooth out vec2 tex0;

void main() {
	gl_Position = PROJ_VIEW_MAT * inVert;
	tex0 = inTex0;
}
