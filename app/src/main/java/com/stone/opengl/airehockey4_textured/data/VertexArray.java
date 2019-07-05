package com.stone.opengl.airehockey4_textured.data;

import android.opengl.GLES20;

import com.stone.opengl.airehockey4_textured.constant.Constants;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.glEnableVertexAttribArray;

/**
 * desc     : 顶点
 * author   : stone
 * homepage : http://stone86.top
 * email    : aa86799@163.com
 * time     : 2018/11/10 14 25
 */
public class VertexArray {
    private final FloatBuffer mFloatBuffer;


    public VertexArray(float[] vertexData) {
        mFloatBuffer = ByteBuffer
                .allocateDirect(vertexData.length * Constants.BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
    }

    public void setVertexAttribPointer(int dataOffset, int attributeLocation,
                                       int componentCount, int stride) {
        //指针移动到开始位置：表示将从开始位置读取
        mFloatBuffer.position(dataOffset);
        //绑定顶点数据到 位置对象.
        GLES20.glVertexAttribPointer(attributeLocation, componentCount, GLES20.GL_FLOAT,
                false, stride, mFloatBuffer);
        //启用顶点数据
        glEnableVertexAttribArray(attributeLocation);

//        mFloatBuffer.position(0);
    }
}
