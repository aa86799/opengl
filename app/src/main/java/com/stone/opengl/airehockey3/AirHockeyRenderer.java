/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
***/
package com.stone.opengl.airehockey3;

import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.os.Build;

import com.stone.opengl.R;
import com.stone.opengl.airehockey3.util.LoggerConfig;
import com.stone.opengl.airehockey3.util.MatrixHelper;
import com.stone.opengl.airehockey3.util.ShaderHelper;
import com.stone.opengl.airehockey3.util.TextResourceReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_LINES;
import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glLineWidth;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;

public class AirHockeyRenderer implements Renderer {
    private static final float[] modelMatrix = new float[16];
    private static final String U_MATRIX = "u_Matrix";//用于在顶点着色器中查找
    private static final float[] projectionMatrix = new float[16];
    private static final String A_POSITION = "a_Position";//用于在顶点着色器中查找
    private static final int POSITION_COMPONENT_COUNT = 4;//顶点坐标由4个分量构成：x,y，z，w
    private static final String A_COLOR = "a_Color"; //用于在顶点着色器中查找
    private static final int COLOR_COMPONENT_COUNT = 3;
    private static final int BYTES_PER_FLOAT = 4;
    /*
    由于数据数组中，既有位置又有颜色；用于告诉OpenGL  在读取下个顶点时，位置间距有多少个字节
     */
    private static final int STRIDE = (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT;
    private final FloatBuffer vertexData;
    private final Context context;
    private int program;
    private int aColorLocation;
    private int aPositionLocation;
    private int uMatrixLocation;

    public AirHockeyRenderer() {
        // This constructor shouldn't be called -- only kept for showing
        // evolution of the code in the chapter.
        context = null;
        vertexData = null;
    }

    public AirHockeyRenderer(Context context) {
        this.context = context;

        float[] tableVerticesWithTriangles = {
            // Triangle Fan  三角形扇
            // 将矩形两条对角线连起，分成四个三角形，以中点和两个角落点各构成一个三角形
            // 定义6个顶点，就能绘制四个三角形，会复用前面的顶点，对应绘制函数：glDrawArrays(GL_TRIANGLE_FAN, ...)
//              0,    0,
//            -0.5f, -0.5f,
//             0.5f, -0.5f,
//             0.5f, 0.5f,
//            -0.5f, 0.5f,
//            -0.5f, -0.5f,

            // Triangle Fan。  每个顶点(x,y,z,w)后，增加三个数据，表示颜色分量
               0f,    0f,  0f, 1.5f,   1f,   1f,   1f,
            -0.5f, -0.8f,  0f, 1.0f, 0.3f, 0.3f, 0.3f,
             0.5f, -0.8f,  0f, 1.0f, 0.7f, 0.7f, 0.7f,
             0.5f,  0.8f,  0f, 2.0f, 0.3f, 0.3f, 0.3f,
            -0.5f,  0.8f,  0f, 2.0f, 0.7f, 0.7f, 0.7f,
            -0.5f, -0.8f,  0f, 1.0f, 0.3f, 0.3f, 0.3f,  //灰度色，混合; w底部为1，顶部为2

            // Line 1
            -0.5f, 0f, 0f, 1.5f, 1f, 0f, 0f,
             0.5f, 0f, 0f, 1.5f, 1f, 0f, 0f, //中线 w 1.5

            // Mallets
            0f, -0.4f, 0, 1.25f, 0, 0, 1,
            0f,  0.4f, 0, 1.75f, 1, 0, 0,

            //test: Mallets on the center of table
//            0, 0, 0, 1.5f, 1, 0, 1,

            //test: outer rect --- four line 逆时针
            -0.5f, -0.8f, 0f, 1.0f, 0, 1, 1, //左下角
             0.5f, -0.8f, 0f, 1.0f, 0, 1, 1, //右下角

             0.5f, -0.8f, 0f, 1.0f, 0, 1, 1, //右下角
             0.5f,  0.8f, 0f, 2.0f, 0, 1, 1, //右上角

             0.5f,  0.8f, 0f, 2.0f, 0, 1, 1, //右上角
            -0.5f,  0.8f, 0f, 2.0f, 0, 1, 1, //左上角

            -0.5f,  0.8f, 0f, 2.0f, 0, 1, 1, //左上角
            -0.5f, -0.8f, 0f, 1.0f, 0, 1, 1  //左下角
        };
        
        vertexData = ByteBuffer
                //分配本地内存空间  非虚拟机中的内存空间
            .allocateDirect(tableVerticesWithTriangles.length * BYTES_PER_FLOAT)
                //按  本地字节序 排序
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer();

        vertexData.put(tableVerticesWithTriangles);

        /*
        随着程序运行， 堆内存的碎片化 可能越来越严重，需要学习些本地内存的管理技术
         */
    }


    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        /*
		// Set the background clear color to red. The first component is red,
		// the second is green, the third is blue, and the last component is
		// alpha, which we don't use in this lesson.
		glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
         */

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);//清屏成黑色

        /* 读取glsl文件 */
        String vertexShaderSource = TextResourceReader.readTextFileFromResource(context, R.raw.simple_vertex_shader);
        String fragmentShaderSource = TextResourceReader.readTextFileFromResource(context, R.raw.simple_fragment_shader);

        //编译着色器
        int vertexShader = ShaderHelper.compileVertexShader(vertexShaderSource);
        int fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderSource);

        //链接program
        program = ShaderHelper.linkProgram(vertexShader, fragmentShader);

        if (LoggerConfig.ON) {
            //验证 program
            ShaderHelper.validateProgram(program);
        }

        //告诉OpenGL 要绘制任何东西屏幕上时，要使用的程序
        glUseProgram(program);

        //获取 uniform的 矩阵变量在程序中的位置
        uMatrixLocation = glGetUniformLocation(program, U_MATRIX);

        //获取attribute变量在程序中的位置； 这里是取的 颜色
        aColorLocation = glGetAttribLocation(program, A_COLOR);
        //获取attribute变量在程序中的位置； 这里取的是 位置坐标
        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        
        // Bind our data, specified by the variable vertexData, to the vertex attribute at location A_POSITION_LOCATION.
        vertexData.position(0);//指针移动到开始位置：表示将从开始位置读取
        /*
         * 绑定顶点数据到 位置对象. 参数：
         * 位置对象；顶点由几个分量表示；数据类型；false参数暂忽略; 跨过多少字节查找下一顶点位置；顶点buffer
         * size: 指定每个顶点属性的组件数量; 必须为1、2、3或者4。初始值为4。（如position是由3个（x,y,z）组成，而颜色是4个（r,g,b,a））
         *      我们这里的顶点位置用2个数量表示；颜色用3个
         */
        glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT, 
            false, STRIDE, vertexData);
        //启用顶点数据
        glEnableVertexAttribArray(aPositionLocation);

        /*
         * 从第2个位置开始读取(绑定)颜色； 即与 着色器中的 a_color关联起来了
         * OpenGL的一种颜色有四个分量，我们不必指定所有颜色分量。前三个未指定的分量，会被默认设为0，最后一个被设为1
         */
        vertexData.position(POSITION_COMPONENT_COUNT);
        glVertexAttribPointer(aColorLocation, COLOR_COMPONENT_COUNT, GL_FLOAT,
                false, STRIDE, vertexData);
        //启用颜色数据
        glEnableVertexAttribArray(aColorLocation);


    }

    /**
     * onSurfaceChanged is called whenever the surface has changed. This is
     * called at least once when the surface is initialized. Keep in mind that
     * Android normally restarts an Activity on rotation, and in that case, the
     * renderer will be destroyed and a new one created.
     * 
     * @param width
     *            The new width, in pixels.
     * @param height
     *            The new height, in pixels.
     */
    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        // Set the OpenGL viewport to fill the entire surface.
        glViewport(0, 0, width, height);

        /*
        判断宽高谁大；扩展大的取值范围， 用大的除以小的，得出比例；小的范围还是[-1,1]
        生成 正交投影矩阵
         */
        /*final float aspectRatio = width > height ? (float) width / height : (float) height / width;
        if (width > height) {//landscape
            Matrix.orthoM(projectionMatrix, 0, -aspectRatio/2, aspectRatio/2,
                    -0.5f, 0.5f, -1.0f/2, 1.0f/2);
        } else {//portrait
            Matrix.orthoM(projectionMatrix, 0, -1, 1,
                    -aspectRatio, aspectRatio, -1, 1);
        }*/

        /*
        透视投影
        fovy: 角度值[0,90] ，越大时，看到的范围越小，感觉在高空中在看一样； 图像是底部大，顶部小
                [-90,0], 越大时，看到的范围越大；图像是底部小，顶部大
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            Matrix.perspectiveM(projectionMatrix, 0, 45,
                    (float) width / height, 1f, 10f);

//            MatrixHelper.perspectiveM(projectionMatrix, 60,
//                    (float) width / height, 1f, 10f);
        } else {
            MatrixHelper.perspectiveM(projectionMatrix, 45,
                    (float) width / height, 1f, 10f);
        }

//        //设为 单位矩阵
        Matrix.setIdentityM(modelMatrix, 0);
        //z轴 负方向 平移 2个单位  (与透视函数不同，可传负数)。 视觉效果即z向里
        /*
         * 在投影矩阵中，定义的视椎体，从-1的位置开始；而默认 原z 值是0；需要进行平移。
         * 投影中是 (-1,-10) ， 这里的 z 值可以从 (-0.5,-10.5)，越来越小。
         *               -1.5f 开始才能看到全貌。
         */
        Matrix.translateM(modelMatrix, 0, 0, 0, -1.5f);

        //绕某个轴 进行旋转
        /*
         *  对x 轴旋转，即 x=1时：
         *      a 值 [0~90]，越大时离中心越近，图像越小；
         *          [-90~0], 越大时，顶部越向上抬
         *  对 y轴旋转，即 y=1时；
         *      a 值 为正至90度内，左侧倾斜；为负 右侧倾斜；
         *  以上为角度 范围当 绝对值超过90度时， 可以替代 正负值效果
         */
        Matrix.rotateM(modelMatrix, 0, 45, 1, 0, 0);

        float[] temp = new float[16];
        //投影矩阵 乘以 模型矩阵，结果写入 temp
        Matrix.multiplyMM(temp, 0, projectionMatrix, 0, modelMatrix, 0);
        System.arraycopy(temp, 0, projectionMatrix, 0, temp.length);
    }

    /**
     * OnDrawFrame is called whenever a new frame needs to be drawn. Normally,
     * this is done at the refresh rate of the screen.
     */
    @Override
    public void onDrawFrame(GL10 glUnused) {
        // Clear the rendering surface.
        glClear(GL_COLOR_BUFFER_BIT);

        /*
            将正交投影矩阵传递给着色器
         */
        glUniformMatrix4fv(uMatrixLocation, 1, false, projectionMatrix, 0);

        // Draw the table.
        glDrawArrays(GL_TRIANGLE_FAN, 0, 6);//绘制数组中数据(形状，开始读的顶点，要读的顶点的数量)

        // Draw the center dividing line.
        glDrawArrays(GL_LINES, 6, 2);
        
        // Draw the first mallet blue.        
        glDrawArrays(GL_POINTS, 8, 1);

        // Draw the second mallet red.
        glDrawArrays(GL_POINTS, 9, 1);

        //test 绘制一个中心点
//        glDrawArrays(GL_POINTS, 10, 1);

        //设置线宽. 影响全局
        glLineWidth(20);
        //test 四周绘制一个边框
        glDrawArrays(GL_LINES, 10, 8);


    }
}
