uniform mat4 u_Matrix;
uniform float u_Time;//每帧的当前间

attribute vec3 a_Position;
attribute vec3 a_Color;
attribute vec3 a_DirectionVector;
attribute float a_ParticleStartTime; //开始时间，每帧中粒子的开始时间不同

varying vec3 v_Color;
varying float v_ElapsedTime; //消逝的时间：从开始的粒子计算起，该时间越来越小

void main()                    
{
    v_Color = a_Color;
    v_ElapsedTime = u_Time - a_ParticleStartTime;
    vec3 currentPosition = a_Position + (a_DirectionVector * v_ElapsedTime);
    float gravityFactor = v_ElapsedTime * v_ElapsedTime / 10.0; //重力因子
    currentPosition.y -= gravityFactor;
    gl_Position = u_Matrix * vec4(currentPosition, 1.0); //仅在投影矩阵相乘时，才转换成四分量。前期为3分量，不影响 w 分量
    gl_PointSize = 50.0;

}
/*
 * attribute vec4 a_Position 含四个分量的向量：x y z w 坐标   xyz对应一个三维的坐标   w是一个特殊的坐标
 * 每个顶点都会调用一次顶点的着色器；a_Position 接收当前顶点的位置
 * 如果没有指定，默认情况下，前三个向量坐标为0， w为1
 *
 * 一个顶点会有几个属性？比如颜色和位置， attribute就是把这些属性放进着色器的手段
 *   一般用attribute变量来表示一些顶点的数据，如：顶点坐标，法线，纹理坐标，顶点颜色等。
 *   attribute变量是只能在vertex shader中使用的变量
 *  在application中，一般用函数 glBindAttribLocation（）来绑定每个attribute变量的位置，然后用函数glVertexAttribPointer（）为每个attribute变量赋值。
 *
 * main() 着色器的入口点； 将定义的位置 a_Position 复制到 输出变量 gl_Position
 * OpenGL会将 gl_Position 中存储的值，作为当前顶点的最终位置，并组装成 点、线、三角形
 *
 * gl_PointSize 点的大小(简单看作像素)
 *    OpenGL把一个点分解成片断时，会生成一个或多个片断，它们是以 gl_Position 为中心的正方形，边长等于 gl_PointSize
 *
 * varying变量会将值发送给 fragment shader的。
 * 一般vertex shader修改varying变量的值，然后fragment shader使用该varying变量的值。
 *    因此varying变量在vertex和fragment shader二者之间的声明必须是一致的。application不能使用此变量。
 * 如顶点0的a_Color是红色，顶点1的a_Color是绿色，将a_Color赋值给v_Color，即告诉OpenGL每个片断都接收一个混合后的颜色：
 *      越接近0的片断，显得更红；越接近1的片断，显得更绿
 *
 * 定义一个 uniform mat4 u_Matrix;  一个4x4的矩阵
 * uniform 变量，是外部application 程序传递给（vertex和fragment）shader的变量。
 */