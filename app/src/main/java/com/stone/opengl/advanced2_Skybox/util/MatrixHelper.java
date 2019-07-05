package com.stone.opengl.advanced2_Skybox.util;

/**
 * desc     :
 * author   : stone
 * homepage : http://stone86.top
 * email    : aa86799@163.com
 * time     : 15/03/2018 20 57
 */
public class MatrixHelper {

    /*
    仿 android.opengl.Matrix#perspectiveM 实现； Matrix中的实现需要sdk版本为4.0以后
     */

    /**
     *
     * @param m     矩阵数组
     * @param yFovInDegrees  y方向角度，用于计算焦距
     * @param aspect 屏幕宽高比
     * @param near   到近处平面的距离，必须是正值。如此值设为1 ，那近处平面就位于z值为-1处。
     * @param far    到远处平面的距离，必须是正值且大于到近处平面的距离
     *
     *   yFovInDegrees 角度的视野，创建一个透视投影。这个视椎体从z 值为 near的位置开始，到z 值为 far的位置结束。
     */
    public static void perspectiveM(float[] m, float yFovInDegrees, float aspect, float near, float far) {
        final float angleInRadians = (float) (yFovInDegrees * Math.PI / 180.0);//角度转弧度
        //计算焦距。视野越小，焦距变长，可以映射到归一化坐标中的范围内的x、y值的范围越小
        final float a = (float) (1.0 / Math.tan(angleInRadians / 2.0));

        //写入矩阵数据，每四个为一列
        m[0] = a / aspect;
        m[1] = 0f;
        m[2] = 0f;
        m[3] = 0f;

        m[4] = 0f;
        m[5] = a;
        m[6] = 0f;
        m[7] = 0f;

        m[8] = 0f;
        m[9] = 0f;
        m[10] = (far + near) / (far - near) * -1;
        m[11] = -1f;

        m[12] = 0f;
        m[13] = 0f;
        m[14] = (2 * far * near) / (far - near) * -1;
        m[15] = 0f;

    }
}
