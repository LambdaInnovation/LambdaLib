#version 330 core

// Uniform
uniform mat4 pvp_matrix;
uniform vec3 player_pos;

// Per-vertex
layout (location = 0) in vec3 position;

// Per-instance
layout (location = 1) in vec3 offset;
layout (location = 2) in float size;
layout (location = 3) in vec3 color;


out vec3 v_color;

void main() {
    vec3 vert_pos = (position * size) + offset - player_pos;

	gl_Position = pvp_matrix * vec4(vert_pos, 1);
	v_color = color;
}
