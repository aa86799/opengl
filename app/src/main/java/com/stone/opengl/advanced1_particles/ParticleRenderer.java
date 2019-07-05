/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material,
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose.
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
 ***/
package com.stone.opengl.advanced1_particles;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.os.Build;

import com.stone.opengl.R;
import com.stone.opengl.advanced1_particles.object.ParticleShooter;
import com.stone.opengl.advanced1_particles.object.ParticleSystem;
import com.stone.opengl.advanced1_particles.program.ParticleShaderProgram;
import com.stone.opengl.advanced1_particles.util.Geometry;
import com.stone.opengl.advanced1_particles.util.MatrixHelper;
import com.stone.opengl.advanced1_particles.util.TextureHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FUNC_ADD;
import static android.opengl.GLES20.GL_ONE;
import static android.opengl.GLES20.glBlendEquation;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glViewport;

public class ParticleRenderer implements Renderer {
    private static final float[] projectionMatrix = new float[16]; //投影矩阵
    private final float[] viewMatrix = new float[16]; //视图矩阵
    private final float[] viewProjectionMatrix = new float[16]; //保存投影矩阵与视图矩阵相乘的结果

    private final Context context;
    private ParticleShaderProgram particleProgram;
    private ParticleSystem particleSystem;
    private ParticleShooter redParticleShooter;
    private ParticleShooter greenParticleShooter;
    private ParticleShooter blueParticleShooter;
    /*private ParticleFireworksExplosion particleFireworksExplosion;
    private Random random;*/
    private long globalStartTime;
    private int textureId;

    public ParticleRenderer(Context context) {
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

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);//清屏成黑色
//        glClearColor(1.0f, 0.0f, 0.0f, 0.0f);//清屏成红色
        glEnable(GL_BLEND); //开启混合模式
//        glBlendEquation(GL_FUNC_SUBTRACT);//减
        glBlendEquation(GL_FUNC_ADD);//加
        glBlendFunc(GL_ONE, GL_ONE); //指定源混合因子与目标混合因子
//        glBlendFuncSeparate(Color.CYAN, Color.parseColor("#abc777"), 50, 100);

        particleProgram = new ParticleShaderProgram(context);
        particleSystem = new ParticleSystem(1000);
        globalStartTime = System.nanoTime(); //纳秒 十亿分之一秒

        final Geometry.Vector particleDirection = new Geometry.Vector(0f, 1.5f, 0);
        //角度越大，发射器发射的角度也会范围越大，造成最绘制出来的效果更发散；角度越小，越集中
        final float angleVarianceInDegrees = 5f;
        final float speedVariance = 1f;
        redParticleShooter = new ParticleShooter(
                new Geometry.Point(-1f, 0, 0),
                particleDirection,
                Color.rgb(255, 50, 5), //偏红的颜色
                angleVarianceInDegrees,
                speedVariance
        );
        greenParticleShooter = new ParticleShooter(
                new Geometry.Point(0f, 0, 0),
                particleDirection,
                Color.rgb(25, 255, 25), //偏绿的颜色
                angleVarianceInDegrees,
                speedVariance
        );
        blueParticleShooter = new ParticleShooter(
                new Geometry.Point(1f, 0, 0),
                particleDirection,
                Color.rgb(5, 50, 255), //偏蓝的颜色
                angleVarianceInDegrees,
                speedVariance
        );

        //加载纹理
        textureId = TextureHelper.loadTexture(context, R.mipmap.particle_texture);
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
            Matrix.perspectiveM(projectionMatrix, 0, 45,
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
//        Matrix.setLookAtM(viewMatrix, 0, 0f, 1.2f, 2.2f,
//                0f, 0f, 0f, 0f, 1f, 0f);


        //设为 单位矩阵
        Matrix.setIdentityM(viewMatrix, 0);
        Matrix.translateM(viewMatrix, 0, 0, -1.5f, -5f);
        Matrix.multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
    }


    @Override
    public void onDrawFrame(GL10 glUnused) {
        // Clear the rendering surface.
        glClear(GL_COLOR_BUFFER_BIT);


        float currentTime = (System.nanoTime() - globalStartTime) / 1e9f;
        redParticleShooter.addParticles(particleSystem, currentTime, 5);
        greenParticleShooter.addParticles(particleSystem, currentTime, 5);
        blueParticleShooter.addParticles(particleSystem, currentTime, 5);

        particleProgram.useProgram();
        particleProgram.setUniforms(viewProjectionMatrix, currentTime, textureId);
        particleSystem.bindData(particleProgram);
        particleSystem.draw();


        /*
         * shooter 中，每次新增的 particle 是 drawFrame 这里定义的当前时间 currentTime；
         * 且由 currentTime 更新 vertext-shader 中的 u_Time
         * 每三组粒子间的 startTime 也等于 绘制时的时间 currentTime； 所以每三组间该值不一样；
         * 而 u_Time 会一直变化，越来越大；
         */

//        try {
//            Thread.sleep(150);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }


}