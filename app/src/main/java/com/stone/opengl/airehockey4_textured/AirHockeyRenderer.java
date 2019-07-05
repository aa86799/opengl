/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
***/
package com.stone.opengl.airehockey4_textured;

import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.os.Build;

import com.stone.opengl.R;
import com.stone.opengl.airehockey4_textured.object.Mallet;
import com.stone.opengl.airehockey4_textured.object.Table;
import com.stone.opengl.airehockey4_textured.program.ColorShaderProgram;
import com.stone.opengl.airehockey4_textured.program.TextureShaderProgram;
import com.stone.opengl.airehockey4_textured.util.LoggerConfig;
import com.stone.opengl.airehockey4_textured.util.MatrixHelper;
import com.stone.opengl.airehockey4_textured.util.ShaderHelper;
import com.stone.opengl.airehockey4_textured.util.TextResourceReader;
import com.stone.opengl.airehockey4_textured.util.TextureHelper;

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
    private static final float[] projectionMatrix = new float[16];

    private final Context context;

    private Table mTable;
    private Mallet mMallet;
    private TextureShaderProgram mTextureShaderProgram;
    private ColorShaderProgram mColorShaderProgram;
    private int mTexture;

    public AirHockeyRenderer(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        /*
		// Set the background clear color to red. The first component is red,
		// the second is green, the third is blue, and the last component is
		// alpha, which we don't use in this lesson.
		glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
         */

//        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);//清屏成黑色
        glClearColor(1.0f, 0.0f, 0.0f, 0.0f);//清屏成红色

        mTable = new Table();
        mMallet = new Mallet();

        mTextureShaderProgram = new TextureShaderProgram(context);
        mColorShaderProgram = new ColorShaderProgram(context);

        mTexture = TextureHelper.loadTexture(context, R.mipmap.air_hockey_surface);


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
        生成 正交投影矩阵.
        产生一个二维的图像。
         */
//        final float aspectRatio = width > height ? (float) width / height : (float) height / width;
//        if (width > height) {//landscape
//            Matrix.orthoM(projectionMatrix, 0, -aspectRatio/2, aspectRatio/2,
//                    -0.5f, 0.5f, -1.0f/2, 1.0f/2);
//        } else {//portrait
//            Matrix.orthoM(projectionMatrix, 0, -1, 1,
//                    -aspectRatio, aspectRatio, -1, 1);
//        }

        /*
        透视投影 (三维的)
        fovy: 角度值[0,90] ，越大时，看到的范围越小，感觉在高空中在看一样； 图像是底部大，顶部小
                [-90,0], 越大时，看到的范围越大；图像是底部小，顶部大
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            Matrix.perspectiveM(projectionMatrix, 0, 60,
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
        Matrix.translateM(modelMatrix, 0, 0, 0, -2f);

        //绕某个轴 进行旋转
        /*
         *  对x 轴旋转，即 x=1时：
         *      a 值 [0~90]，越大时离中心越近，图像越小；
         *          [-90~0], 越大时，顶部越向上抬
         *  对 y轴旋转，即 y=1时；
         *      a 值 为正至90度内，左侧倾斜；为负 右侧倾斜；
         *  以上为角度 范围当 绝对值超过90度时， 可以替代 正负值效果
         */
        Matrix.rotateM(modelMatrix, 0, -40, 1, 0, 0);

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

        mTextureShaderProgram.userProgram();
        mTextureShaderProgram.setUniforms(projectionMatrix, mTexture);
        mTable.bindData(mTextureShaderProgram);
        mTable.draw();

        mColorShaderProgram.userProgram();
        mColorShaderProgram.setUniforms(projectionMatrix);
        mMallet.bindData(mColorShaderProgram);
        mMallet.draw();
    }
}
