package com.stone.opengl.airehockey5_withimprovedmallets.object;

import android.opengl.GLES20;

import com.stone.opengl.airehockey5_withimprovedmallets.constant.Constants;
import com.stone.opengl.airehockey5_withimprovedmallets.data.VertexArray;
import com.stone.opengl.airehockey5_withimprovedmallets.program.TextureShaderProgram;

/**
 * desc     : 球桌
 * author   : stone
 * homepage : http://stone86.top
 * email    : aa86799@163.com
 * time     : 2018/11/10 14 34
 */
public class Table {
    private static final int POSITION_COMPONENT_COUNT = 2; //顶点坐标由2个分量构成：x,y
    private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2; //纹理坐标由2个分量构成：S、T
    /*
    由于数据数组中，既有位置又有颜色；用于告诉OpenGL  在读取下个顶点时，位置间距有多少个字节
     */
    private static final int STRIDE = (POSITION_COMPONENT_COUNT
            + TEXTURE_COORDINATES_COMPONENT_COUNT) * Constants.BYTES_PER_FLOAT;

    private static final float[] VERTEX_DATA = {
            /* X,Y,S,T
             * 注意：Y 和 T 分量是相反的。y 取下边的时候，传递的 T 是上边的
             *
             * 裁剪纹理
             *   这里使用了0.1f 和0.9f 作为T 坐标。因为桌子是一个单位这，1.6个单位高。
             *   而纹理图像是512x1024像素，因此纹理的宽对应一个单位，高对应两个单位。
             *   为了避免把纹理压扁，使用范围0.1到0.9裁剪它的边缘，即只画了中间(纵向上)0.8的部分。
             */
            /*0, 0, 0.5f, 0.5f,
            -0.5f, -0.8f, 0, 0.9f,
            0.5f, -0.8f,  1, 0.9f,
            0.5f,  0.8f,  1, 0.1f,
            -0.5f,  0.8f, 0, 0.1f,
            -0.5f, -0.8f, 0, 0.9f*/

            /*实际图像是 512x512的 */
            0, 0, 0.5f, 0.5f,
            -0.5f, -0.8f, 0, 1f,
            0.5f, -0.8f,  1, 1f,
            0.5f,  0.8f,  1, 0f,
            -0.5f,  0.8f, 0, 0f,
            -0.5f, -0.8f, 0, 1f
    };

    private final VertexArray mVertexArray;

    public Table() {
        mVertexArray = new VertexArray(VERTEX_DATA);
    }

    public void bindData(TextureShaderProgram textureShaderProgram) {
        mVertexArray.setVertexAttribPointer(
                0,
                textureShaderProgram.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT,
                STRIDE);
        mVertexArray.setVertexAttribPointer(
                POSITION_COMPONENT_COUNT,
                textureShaderProgram.getTextureCoordinatesAttributeLocation(),
                TEXTURE_COORDINATES_COMPONENT_COUNT,
                STRIDE);

    }

    public void draw() {
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 6);
    }

}
