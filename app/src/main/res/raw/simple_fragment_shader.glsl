precision mediump float;

//uniform vec4 u_Color; //glUniform4f  rgba
//uniform vec3 u_Color; //glUniform3f rgb

varying vec4 v_Color;

void main()
{
//    gl_FragColor = u_Color;
    gl_FragColor = v_Color;
}

/*
 * precision 定义浮点数据类型的精度，可选值：lowp(低精度)  mediump(中等精度)  highp(高精度)
 *      顶点着色器，默认精度就是highp的，不能设置；因为OpenGL设计者觉得 位置的精确度 很重要
 *
 * uniform 变量，是外部application 程序传递给（vertex和fragment）shader的变量。
 *      因此它是application通过函数glUniform**（）函数赋值的。
 *      在（vertex和fragment）shader程序内部，uniform变量就像是C语言里面的常量(const)，它不能被shader程序修改
 *   如果uniform变量在vertex和fragment两者之间声明方式完全一样，则它可以在vertex和fragment共享使用。
 *     （相当于一个被vertex和fragment shader共享的全局变量）
 *   uniform变量一般用来表示：变换矩阵，材质，光照参数和颜色等信息。
 *
 * vec4 u_Color：含四个分量的向量：r g b a
 * main(): 着色器入口
 * 将 u_Color 赋值给输出变量 gl_FragColor; OpenGL会将 gl_FragColor 中存储的颜色，作为当前片断的最终颜色
 *
 * gl_FragColor = v_Color;
 *      如果片断属于一条直线，则会由两个顶点计算出混合后的颜色；
 *      如果片断属于一个三角形，则会由三个顶点计算出混合后的颜色；
 * 颜色混合以 线性插值(linear interpolation) 实现。
 *   沿着一条直线做 线性插值
 *      若有一个直线，左顶点为红色red， 右顶点为绿色green；
 *      从左向右，则左顶点100%red、0%green，中间50%red、50green，右顶点0%red、100%green
 *      每种颜色的强度依赖于: 每个片断到颜色顶点的距离  (这里从左向右，即片断到左顶点的距离)
 *      公式：blended_value = vertex_0_value * (100% - distance_ratio) + vertex_1_value * distance_ratio;
 *          公式要运用在每个颜色分量(RGB)上
 *
 *   三角形中的 线性插值
 *      从片断的点到三个顶点进行连线。每个顶点相对的三角形面积越大，则该片断越接近这个顶点的颜色
 *      公式：blended_value = vertex_0_value * vertex_0_weight + vertex_1_value * vertex_1_weight
 *              + vertex_2_value * (100% - vertex_0_weight - vertex_1_weight); 权重即顶点相对三角形所占面积之比重
 */