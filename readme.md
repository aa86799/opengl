public class GLSurfaceView extends SurfaceView implements SurfaceHolder.Callback2


public interface Renderer
    GLSurfaceView 中的内部类

public interface SurfaceHolder

public interface Callback
    SurfaceHolder 中的内部类

public interface Callback2 extends Callback
    SurfaceHolder 中的内部类

airehockey2
    OPENGL ES 应用开发实践指南， 1~5章
airehockey3
    OPENGL ES 应用开发实践指南， 6~章

# 基本概念
    1. 顶点 vertex
    属性：位置，表示了顶点在空间的定位。
    OPENGL 中，只能绘制点、直线、三角形。
    逆时针顺序(从下向上)，卷曲顺序，优化性能。
    顶点2分量：x、y
    顶点4分量：x、y、z、w
    绘制三角形：glDrawArrays(type, startIndex, count);
        type分：
            GL_TRIANGLE: 每三个顶点绘制一个三角形
            GL_TRIANGLE_STRIP: 条带
                设顶定个数为 n；前三个顶点构成第一个三角形。
                之后，第 n 个顶点，
                当 n 为偶数：
                    (n-1, n-2, n)  如n 为4，表示第4个顶点，这时就是用第3、第2和第4个顶点逆时针构成一个三角形
                当 n 为奇数：
                    (n-2, n-1, n)
            GL_TRIANGLE_FAN: 扇形
                初始顶点需要是扇形的圆心，前三个顶点构成第一个三角形。
                从第三个顶点开始就满足，就是 (n, 圆心, n-1)
       GL_TRIANGLE_FAN 适合在绘制近似扇形或圆形时，GL_TRIANGLE_STRIP 适合在绘制类似 一条水平带的图形时；
            起始顶点的选取也很重要，要使后续顶点满足规则，逆时针绘制出满足要求的图形
    三角形扇：将矩形两条对角线连起，分成四个三角形，以中点和两个角落点各构成一个三角形；
            从中点开始定义，最后需要闭合图形时，需要重复第一个角落顶点
    2. 顶点着色器 vertex shader
        生成每个顶点的位置，将顶点集合，组装成点、直线、三角形。
    3. 片断着色器 fragment shader
        生成颜色。一个片断就是一个小的、单一颜色的长方形区域，类似屏幕上的一个像素
     当最后的颜色生成后，OPENGL 就会把它们写到一块称为 帧缓冲区的内存中，然后android 会把这个帧缓冲区显示到屏幕上。
    4.  管道
        读取顶点数据->执行顶点着色器->组装图元->光栅化图元->执行片断着色器->写入帧缓冲区->显示帧缓冲区在屏幕上。
     程序中的流程：
        编译两种 shader->生成program链接shader

# 创建 顶点着色器
    在 res/raw 下，建立 后缀为 ".glsl"的文件。
# 颜色
    颜色值范围[0,1]，从未点亮到全亮

# 归一化坐标
    [-1, 1]，方向为 从左向右、从下向上