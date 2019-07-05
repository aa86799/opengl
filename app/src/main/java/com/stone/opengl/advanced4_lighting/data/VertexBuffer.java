package com.stone.opengl.advanced4_lighting.data;

import com.stone.opengl.advanced4_lighting.constant.Constants;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGenBuffers;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * desc     : 高度图顶点缓冲区
 *              高度图：使用灰度图，亮的区域表示高地，暗的区域表示低地；描述地形
 * author   : stone
 * homepage : http://stone86.top
 * email    : aa86799@163.com
 * time     : 2018/11/14 10 20
 */
public class VertexBuffer {

    private final int bufferId;

    public VertexBuffer(float[] vertexData) {
        // Allocate a buffer.
        final int buffers[] = new int[1];
        glGenBuffers(buffers.length, buffers, 0);
        if (buffers[0] == 0) {
            throw new RuntimeException("Could not create a new vertex buffer object.");
        }
        bufferId = buffers[0];

        // Bind to the buffer.
        glBindBuffer(GL_ARRAY_BUFFER, bufferId);

        // Transfer data to native memory.
        FloatBuffer vertexArray = ByteBuffer
                .allocateDirect(vertexData.length * Constants.BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
        vertexArray.position(0);

        // Transfer data from native memory to the GPU buffer.  将数据传递到缓冲区对象
        /*
         * 参数 useage：
         *  GL_STREAM_DRAW: 对象被修改一次，不会经常使用
         *  GL_STATIC_DRAW: 对象被修改一次，会经常使用
         *  GL_DYNAMIC_DRAW: 对象被修改和使用多次
         *  这参数只是提示，OpenGL可以根据需要自行变化。多数情况下使用 GL_STATIC_DRAW
         */
        glBufferData(GL_ARRAY_BUFFER, vertexArray.capacity() * Constants.BYTES_PER_FLOAT,
                vertexArray, GL_STATIC_DRAW);

        //解绑 缓冲区对象
        // IMPORTANT: Unbind from the buffer when we're done with it.
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        // We let vertexArray go out of scope, but it won't be released
        // until the next time the garbage collector is run.
    }

    public void setVertexAttribPointer(int dataOffset, int attributeLocation,
                                       int componentCount, int stride) {
        glBindBuffer(GL_ARRAY_BUFFER, bufferId);
        // This call is slightly different than the glVertexAttribPointer we've
        // used in the past: the last parameter is set to dataOffset, to tell OpenGL
        // to begin reading data at this position of the currently bound buffer.
        glVertexAttribPointer(attributeLocation, componentCount, GL_FLOAT,
                false, stride, dataOffset);
        glEnableVertexAttribArray(attributeLocation);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
}
