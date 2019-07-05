/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material,
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose.
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
 ***/
package com.stone.opengl.airehockey5_withimprovedmallets;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.os.Build;

import com.stone.opengl.R;
import com.stone.opengl.airehockey5_withimprovedmallets.object.Mallet;
import com.stone.opengl.airehockey5_withimprovedmallets.object.Puck;
import com.stone.opengl.airehockey5_withimprovedmallets.object.Table;
import com.stone.opengl.airehockey5_withimprovedmallets.program.ColorShaderProgram;
import com.stone.opengl.airehockey5_withimprovedmallets.program.TextureShaderProgram;
import com.stone.opengl.airehockey5_withimprovedmallets.util.MatrixHelper;
import com.stone.opengl.airehockey5_withimprovedmallets.util.TextureHelper;

public class AirHockeyRenderer implements Renderer {
    private static final float[] modelMatrix = new float[16]; //模型矩阵
    private static final float[] projectionMatrix = new float[16]; //投影矩阵
    private final float[] viewMatrix = new float[16]; //视图矩阵
    private final float[] viewProjectionMatrix = new float[16]; //保存投影矩阵与视图矩阵相乘的结果
    private final float[] modelViewProjectionMatrix = new float[16]; //保存 viewProjectionMatrix * modelMatrix的结果

    private final Context context;

    private Table mTable;
    private Mallet mMallet;
    private Puck mPuck;
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
//        mMallet = new Mallet(0.05f, 0.03f, 32);
        mMallet = new Mallet(0.08f, 0.15f, 32);
        mPuck = new Puck(0.06f, 0.02f, 32);

        mTextureShaderProgram = new TextureShaderProgram(context);
        mColorShaderProgram = new ColorShaderProgram(context);

        mTexture = TextureHelper.loadTexture(context, R.mipmap.air_hockey_surface);
    }

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

        //创建视图矩阵
        /*
         * 参数意义：
         *  float[] rm, 这是目标数组，长度至少为16个，以便能存储视图矩阵
         *  int rmOffset, 从这个偏移值开始存储到 rm 数组中
         *  float eyeX, float eyeY, float eyeZ, 眼睛/相机镜头所在的位置，即从这个点进行观察
         *  float centerX, float centerY, float centerZ, 眼睛正在看的地方；这个位置出现在整体场景的中心
         *  float upX, float upY, float upZ, 眼睛所属的头，指向的位置。如 upY=1，则头指向正上方
         *
         *  把眼睛(eye)设成(0,1.2,2.2)，意味着眼睛的位置在 x-z 平面上方1.2个单位，并向后2.2个单位。
         *  换句话说，场景中所有东西都出现在你眼睛下面1.2个单位及前面2.2个单位。把中心(center)设成(0,0,0)，
         *  意味着你将向下看你前面的原点；并把指向(up)设成(0,1,0)，意味着你的头是我笔直朝上的，这个场景不会旋转到任何一边。
         */
        Matrix.setLookAtM(viewMatrix, 0, 0f, 1.2f, 2.2f,
                0f, 0f, 0f, 0f, 1f, 0f);

//
////        //设为 单位矩阵
//        Matrix.setIdentityM(modelMatrix, 0);
//        //z轴 负方向 平移 2个单位  (与透视函数不同，可传负数)。 视觉效果即z向里
//        /*
//         * 在投影矩阵中，定义的视椎体，从-1的位置开始；而默认 原z 值是0；需要进行平移。
//         * 投影中是 (-1,-10) ， 这里的 z 值可以从 (-0.5,-10.5)，越来越小。
//         *               -1.5f 开始才能看到全貌。
//         */
//        Matrix.translateM(modelMatrix, 0, 0, 0, -2f);
//
//        //绕某个轴 进行旋转
//        /*
//         *  对x 轴旋转，即 x=1时：
//         *      a 值 [0~90]，越大时离中心越近，图像越小；
//         *          [-90~0], 越大时，顶部越向上抬
//         *  对 y轴旋转，即 y=1时；
//         *      a 值 为正至90度内，左侧倾斜；为负 右侧倾斜；
//         *  以上为角度 范围当 绝对值超过90度时， 可以替代 正负值效果
//         */
//        Matrix.rotateM(modelMatrix, 0, -40, 1, 0, 0);
//
//        float[] temp = new float[16];
//        //投影矩阵 乘以 模型矩阵，结果写入 temp
//        Matrix.multiplyMM(temp, 0, projectionMatrix, 0, modelMatrix, 0);
//        System.arraycopy(temp, 0, projectionMatrix, 0, temp.length);
    }


    @Override
    public void onDrawFrame(GL10 glUnused) {
        // Clear the rendering surface.
        glClear(GL_COLOR_BUFFER_BIT);

        multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        // Draw the mTable.
        positionTableInScene();
        mTextureShaderProgram.useProgram();
        mTextureShaderProgram.setUniforms(modelViewProjectionMatrix, mTexture);
        mTable.bindData(mTextureShaderProgram);
        mTable.draw();

        // Draw the mallets.
        positionObjectInScene(0, mMallet.height / 2f, -0.4f);
        mColorShaderProgram.useProgram();
        mColorShaderProgram.setUniforms(modelViewProjectionMatrix, 1f, 0.0f, 0f);
        mMallet.bindData(mColorShaderProgram);
        mMallet.draw();

        positionObjectInScene(0f, mMallet.height / 2f, 0.4f);
        mColorShaderProgram.setUniforms(modelViewProjectionMatrix, 0f, 0f, 1f);
        // Note that we don't have to define the object data twice -- we just
        // draw the same mMallet again but in a different position and with a
        // different color.
        mMallet.draw();

        // Draw the mPuck.
        positionObjectInScene(0f, mPuck.height / 2f, 0f);
        mColorShaderProgram.setUniforms(modelViewProjectionMatrix, 0.5f, 0.8f, 1f);
        mPuck.bindData(mColorShaderProgram);
        mPuck.draw();
    }

    private void positionTableInScene() {
        // The mTable is defined in terms of X & Y coordinates, so we rotate it
        // 90 degrees to lie flat on the XZ plane.
        setIdentityM(modelMatrix, 0);
        //桌子以 xy 坐标定义的，平铺有屏幕平面上；绕 x轴 转90度，即平放在地上的感觉（立体感）
        rotateM(modelMatrix, 0, -90f, 1f, 0f, 0f);
        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix,
                0, modelMatrix, 0);
    }

    // The mallets and the mPuck are positioned on the same plane as the mTable.
    private void positionObjectInScene(float x, float y, float z) {
        setIdentityM(modelMatrix, 0);
        translateM(modelMatrix, 0, x, y, z);
        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix,
                0, modelMatrix, 0);
    }
}