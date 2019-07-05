/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material,
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose.
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
 ***/
package com.stone.opengl.airehockey6_touch;

import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.os.Build;

import com.stone.opengl.R;
import com.stone.opengl.airehockey6_touch.object.Mallet;
import com.stone.opengl.airehockey6_touch.object.Puck;
import com.stone.opengl.airehockey6_touch.object.Table;
import com.stone.opengl.airehockey6_touch.program.ColorShaderProgram;
import com.stone.opengl.airehockey6_touch.program.TextureShaderProgram;
import com.stone.opengl.airehockey6_touch.util.Geometry;
import com.stone.opengl.airehockey6_touch.util.MatrixHelper;
import com.stone.opengl.airehockey6_touch.util.TextureHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.invertM;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.multiplyMV;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

public class AirHockeyRenderer implements Renderer {
    private static final float[] modelMatrix = new float[16]; //模型矩阵
    private static final float[] projectionMatrix = new float[16]; //投影矩阵
    private final float[] viewMatrix = new float[16]; //视图矩阵
    private final float[] viewProjectionMatrix = new float[16]; //保存投影矩阵与视图矩阵相乘的结果
    private final float[] modelViewProjectionMatrix = new float[16]; //保存 viewProjectionMatrix * modelMatrix的结果
    private final float[] invertedViewProjectionMatrix = new float[16];

    private final Context context;

    private Table mTable;
    private Mallet mMallet;
    private Puck mPuck;
    private TextureShaderProgram mTextureShaderProgram;
    private ColorShaderProgram mColorShaderProgram;
    private int mTexture;

    private boolean mBlueMalletPressed; //木槌是否按下
    private boolean mRedMalletPressed; //木槌是否按下
    private Geometry.Point mBlueMalletPosition; //蓝色木槌的位置
    private Geometry.Point previousBlueMalletPosition; //上次蓝色木槌的位置
    private Geometry.Point mRedMalletPosition; //红色木槌的位置
    private Geometry.Point previousRedMalletPosition; //上次红色木槌的位置
    private Geometry.Point puckPosition; //冰球的位置
    private Geometry.Vector puckVector; //冰球向量: 存储速度和方向

    //边界检测：如下边界定义与 桌子的的四边对应
    private final float leftBound = -0.5f;
    private final float rightBound = 0.5f;
    private final float farBound = -0.8f;
    private final float nearBound = 0.8f;

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

        mBlueMalletPosition = new Geometry.Point(0f, mMallet.height / 2f, 0.4f);
        mRedMalletPosition = new Geometry.Point(0f, mMallet.height / 2f, -0.4f);
        puckPosition = new Geometry.Point(0f, mPuck.height / 2f, 0f);
        puckVector = new Geometry.Vector(0f, 0f, 0f);
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

        // Translate the puck by its vector
        puckPosition = puckPosition.translate(puckVector);

        // If the puck struck a side, reflect it off that side.
        if (puckPosition.x < leftBound + mPuck.radius
                || puckPosition.x > rightBound - mPuck.radius) {
            puckVector = new Geometry.Vector(-puckVector.x, puckVector.y, puckVector.z);
            puckVector = puckVector.scale(0.9f); //向量内分量缩小，即冰球速度越来小
        }
        if (puckPosition.z < farBound + mPuck.radius
                || puckPosition.z > nearBound - mPuck.radius) {
            puckVector = new Geometry.Vector(puckVector.x, puckVector.y, -puckVector.z);
            puckVector = puckVector.scale(0.9f);
        }

        puckVector = puckVector.scale(0.99f); //阻尼效果，会停止的更快一些

        // Clamp the puck position. 冰球位置边界
        puckPosition = new Geometry.Point(
                clamp(puckPosition.x, leftBound + mPuck.radius, rightBound - mPuck.radius),
                puckPosition.y,
                clamp(puckPosition.z, farBound + mPuck.radius, nearBound - mPuck.radius)
        );

        multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        //矩阵反转：取消视图矩阵和投影矩阵的效果
        invertM(invertedViewProjectionMatrix, 0, viewProjectionMatrix, 0);

        // Draw the mTable.
        positionTableInScene();
        mTextureShaderProgram.useProgram();
        mTextureShaderProgram.setUniforms(modelViewProjectionMatrix, mTexture);
        mTable.bindData(mTextureShaderProgram);
        mTable.draw();

        // Draw the mallets.
        positionObjectInScene(mRedMalletPosition.x, mRedMalletPosition.y, mRedMalletPosition.z);
        mColorShaderProgram.useProgram();
        mColorShaderProgram.setUniforms(modelViewProjectionMatrix, 1f, 0.0f, 0f);
        mMallet.bindData(mColorShaderProgram);
        mMallet.draw();

        //绘制蓝槌
        positionObjectInScene(mBlueMalletPosition.x, mBlueMalletPosition.y, mBlueMalletPosition.z);
        mColorShaderProgram.setUniforms(modelViewProjectionMatrix, 0f, 0f, 1f);
        // Note that we don't have to define the object data twice -- we just
        // draw the same mMallet again but in a different position and with a
        // different color.
        mMallet.draw();

        // Draw the mPuck.
        positionObjectInScene(puckPosition.x, puckPosition.y, puckPosition.z);
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

    public void handleTouchPress(float normalizedX, float normalizedY) {
        mBlueMalletPressed = mRedMalletPressed = false;
        /*
         * 相交测试：
         *  首先，把二维空间坐标转换到三维空间；要知道触碰了什么，需要把被触碰点投射到一条射线上，这条射线从
         *    我们的视点跨越那个三维场景。
         *  然后，需要检查这条射线是否与木槌相交。假定木槌周围是一个同样大小的包围球，我们就测试这个球是否与射线相交。
         */

        //创建射线
        Geometry.Ray ray = convertNormalized2DPointToRay(normalizedX, normalizedY);

        //y在[-1,0] 蓝槌区域
        if (normalizedY >= -1 && normalizedY <= 0) {
            // Now test if this ray intersects with the mallet by creating a
            // bounding sphere that wraps the mallet.
            //球体
            Geometry.Sphere malletBoundingSphere = new Geometry.Sphere(new Geometry.Point(
                    mBlueMalletPosition.x,
                    mBlueMalletPosition.y,
                    mBlueMalletPosition.z),
                    mMallet.height / 2f);

            // If the ray intersects (if the user touched a part of the screen that
            // intersects the mallet's bounding sphere), then set malletPressed =
            // true.  //球体与射线是否相交
            mBlueMalletPressed = Geometry.intersects(malletBoundingSphere, ray);
        } else {//红槌
            Geometry.Sphere malletBoundingSphere = new Geometry.Sphere(new Geometry.Point(
                    mRedMalletPosition.x,
                    mRedMalletPosition.y,
                    mRedMalletPosition.z),
                    mMallet.height / 2f);

            mRedMalletPressed = Geometry.intersects(malletBoundingSphere, ray);
        }
    }

    /**
     * 拖动， 射线与桌子平面相交，就移动木槌
     * @param normalizedX
     * @param normalizedY
     */
    public void handleTouchDrag(float normalizedX, float normalizedY) {
        if (mBlueMalletPressed || mRedMalletPressed) {
            Geometry.Ray ray = convertNormalized2DPointToRay(normalizedX, normalizedY);
            // Define a plane representing our air hockey table. 定义一个代表空气球桌的平面
            //new Geometry.Vector(0, 1, 0) 该向量 垂直于平面上的一点
            Geometry.Plane plane = new Geometry.Plane(new Geometry.Point(0, 0, 0),
                    new Geometry.Vector(0, 1, 0));
            // Find out where the touched point intersects the plane
            // representing our table. We'll move the mallet along this plane.
            Geometry.Point touchedPoint = Geometry.intersectionPoint(ray, plane);
            // Clamp to bounds

            if (mBlueMalletPressed) {
                previousBlueMalletPosition = mBlueMalletPosition;

                /*
            blueMalletPosition =
                new Point(touchedPoint.x, mallet.height / 2f, touchedPoint.z);
            */
                // Clamp to bounds
                mBlueMalletPosition = new Geometry.Point(
                        //三个参数间中的最大值坐为 x 边界
                        clamp(touchedPoint.x,
                                leftBound + mMallet.radius,rightBound - mMallet.radius),
                        mMallet.height / 2f,
                        clamp(touchedPoint.z,
                                0f + mMallet.radius,nearBound - mMallet.radius)
                );

                // Now test if mallet has struck the puck.
                float distance =
                        Geometry.vectorBetween(mBlueMalletPosition, puckPosition).length();

                //若 true：冰球与蓝木槌相交
                if (distance < (mPuck.radius + mMallet.radius)) {
                    // The mallet has struck the puck. Now send the puck flying
                    // based on the mallet velocity.
                    puckVector = Geometry.vectorBetween(
                            previousBlueMalletPosition, mBlueMalletPosition);
                }
            } else if (mRedMalletPressed) {
                previousRedMalletPosition = mRedMalletPosition;

                // Clamp to bounds
                mRedMalletPosition = new Geometry.Point(
                        //三个参数间中的最大值坐为 x 边界
                        clamp(touchedPoint.x,
                                leftBound + mMallet.radius,rightBound - mMallet.radius),
                        mMallet.height / 2f,
                        clamp(touchedPoint.z,
                                farBound + mMallet.radius, 0f - mMallet.radius)
                );

                // Now test if mallet has struck the puck.
                float distance =
                        Geometry.vectorBetween(mRedMalletPosition, puckPosition).length();

                //若 true：冰球与红木槌相交
                if (distance < (mPuck.radius + mMallet.radius)) {
                    // The mallet has struck the puck. Now send the puck flying
                    // based on the mallet velocity.
                    puckVector = Geometry.vectorBetween(
                            previousRedMalletPosition, mRedMalletPosition);
                }
            }

        }
    }

    /*
     * 被触碰的点，转换为一个三维的射线
     *
     */
    private Geometry.Ray convertNormalized2DPointToRay(float normalizedX, float normalizedY) {
        // We'll convert these normalized device coordinates into world-space
        // coordinates. We'll pick a point on the near and far planes, and draw a
        // line between them. To do this transform, we need to first multiply by
        // the inverse matrix, and then we need to undo the perspective divide.
        /*
         * 通常把一个三维场景投递到二维屏幕时，使用透视投影和透视除法将顶点坐标转化为归一设备坐标。
         * 现在要反向操作：已知归一坐标，要计算出在三维世界中那个被触碰的点与哪里相对应，即为了把被触碰点转换为一个
         *      三维射线，实质上就是要取消透视投影和透视除法
         * 把被触碰点映射以三维空间的一条直线：
         *      直线的近端映射到在投影矩阵中定义的视椎体的近平面；
         *      直线的远端映射到在投影矩阵中定义的视椎体的远平面；
         */
        final float[] nearPointNdc = {normalizedX, normalizedY, -1, 1};
        final float[] farPointNdc =  {normalizedX, normalizedY,  1, 1};

        final float[] nearPointWorld = new float[4];//世界空间中的近点坐标
        final float[] farPointWorld = new float[4];

        //反转矩阵 乘 顶点，获取的结果，w 值也是反转的；
        multiplyMV(//矩阵乘顶点
                nearPointWorld, 0, invertedViewProjectionMatrix, 0, nearPointNdc, 0);
        multiplyMV(
                farPointWorld, 0, invertedViewProjectionMatrix, 0, farPointNdc, 0);

        // Why are we dividing by W? We multiplied our vector by an inverse
        // matrix, so the W value that we end up is actually the *inverse* of
        // what the projection matrix would create. By dividing all 3 components
        // by W, we effectively undo the hardware perspective divide.
        // 因 w 值是反转的，需要除以 w，以消除透视除法的影响
        divideByW(nearPointWorld);
        divideByW(farPointWorld);

        // We don't care about the W value anymore, because our points are now
        // in world coordinates.
        Geometry.Point nearPointRay =
                new Geometry.Point(nearPointWorld[0], nearPointWorld[1], nearPointWorld[2]);

        Geometry.Point farPointRay =
                new Geometry.Point(farPointWorld[0], farPointWorld[1], farPointWorld[2]);

        /*
         * 基于上面的世界空间的近、远点，构造了两个 Geometry.Point；
         * 下面，就可以构成跨越那个三维场景的射线了
         * 射线(进点，远点-近点的 向量)
         */
        return new Geometry.Ray(nearPointRay, Geometry.vectorBetween(nearPointRay, farPointRay));
    }

    private void divideByW(float[] vector) {
        vector[0] /= vector[3];
        vector[1] /= vector[3];
        vector[2] /= vector[3];
    }

    //三个参数间的最大值
    private float clamp(float value, float min, float max) {
        return Math.min(max, Math.max(value, min));
    }

}