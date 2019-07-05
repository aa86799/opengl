precision mediump float;

uniform sampler2D u_TextureUnit;
varying vec2 v_TextureCoordinates;

void main()
{
    gl_FragColor = texture2D(u_TextureUnit, v_TextureCoordinates);
}

/*
 * sampler2D u_TextureUnit 纹理单元，实际的纹理数据； sampler2D 表示一个二维纹理数据的数组。
 * v_TextureCoordinates 顶顶着色器传递过来的 纹理坐标；
 * 着色器函数 texture2D， 它会读入纹理中特定坐标处的颜色，再赋值给 gl_FragColor ，以设置片断的颜色。
 */