uniform mat4 u_Matrix;
attribute vec3 a_Position;  
varying vec3 v_Position;

void main()                    
{                                	  	          
    v_Position = a_Position;	
    // Make sure to convert from the right-handed coordinate system of the
    // world to the left-handed coordinate system of the cube map, otherwise,
    // our cube map will still work but everything will be flipped.
    //反转 z 分量，把世界右手坐标系，转为天空盒需要的左手坐标系；如果不转换，也能工作，只是看上去纹理是反向的
    v_Position.z = -v_Position.z; 
	           
    gl_Position = u_Matrix * vec4(a_Position, 1.0);
    //把坐标的 z 分量设置为 w 分量； 那么在透视除法后，z 分量都=1，即在远平面上及场景中一切物体的后面
    gl_Position = gl_Position.xyww;
}    
