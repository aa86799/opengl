package com.stone.opengl.airehockey4_textured.object;

import android.opengl.GLES20;

import com.stone.opengl.airehockey4_textured.constant.Constants;
import com.stone.opengl.airehockey4_textured.data.VertexArray;
import com.stone.opengl.airehockey4_textured.program.ColorShaderProgram;

/**
 * desc     : 木槌
 * author   : stone
 * homepage : http://stone86.top
 * email    : aa86799@163.com
 * time     : 2018/11/10 15 31
 */
public class Mallet {
    private static final int POSITION_COMPONENT_COUNT = 2; //顶点坐标分量
    private static final int COLOR_COMPONENT_COUNT = 3; //颜色分量
    //间隔步幅
    private static final int STRIDE = (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * Constants.BYTES_PER_FLOAT;

    private static final float[] VERTEX_DATA = {
            //x,y,r,g,b
            0f, -0.4f, 0, 0, 1,
            0f,  0.4f, 1, 0, 0,
    };
    private final VertexArray mVertexArray;

    public Mallet() {
        mVertexArray = new VertexArray(VERTEX_DATA);
    }

    public void bindData(ColorShaderProgram colorShaderProgram) {
        mVertexArray.setVertexAttribPointer(
                0,
                colorShaderProgram.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT,
                STRIDE);
        mVertexArray.setVertexAttribPointer(
                POSITION_COMPONENT_COUNT,
                colorShaderProgram.getColorAttributeLocation(),
                COLOR_COMPONENT_COUNT,
                STRIDE);
    }

    public void draw() {
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 2);
    }
}
