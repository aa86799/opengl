uniform mat4 u_Matrix;
attribute vec3 a_Position;
varying vec3 v_Color;

void main()                    
{
    //mix 函数，在两个不同的颜色间做平滑插值。
    v_Color = mix(vec3(0.180, 0.467, 0.153),    // A dark green 
                  vec3(0.660, 0.670, 0.680),    // A stony gray 
                  a_Position.y);
		
    gl_Position = u_Matrix * vec4(a_Position, 1.0);    
}
