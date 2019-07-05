/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
***/
package com.stone.opengl.advanced3_heightmap.object;

import com.stone.opengl.advanced3_heightmap.data.VertexArray;
import com.stone.opengl.advanced3_heightmap.program.SkyboxShaderProgram;

import java.nio.ByteBuffer;

import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_BYTE;
import static android.opengl.GLES20.glDrawElements;


public class Skybox {
    private static final int POSITION_COMPONENT_COUNT = 3;
    private final VertexArray vertexArray;
    private final ByteBuffer indexArray;
    
    public Skybox() {        
        // Create a unit cube.  立方体顶点， 前4个近，后4个远
        vertexArray = new VertexArray(new float[] {
            -1,  1,  1,     // (0) Top-left near
             1,  1,  1,     // (1) Top-right near
            -1, -1,  1,     // (2) Bottom-left near
             1, -1,  1,     // (3) Bottom-right near
            -1,  1, -1,     // (4) Top-left far
             1,  1, -1,     // (5) Top-right far
            -1, -1, -1,     // (6) Bottom-left far
             1, -1, -1      // (7) Bottom-right far                        
        });

        /*
         * 假设用两个三角形来绘制6面立方体，则需要12个三角形，即36个顶点(108个浮点数)，
         * 其中很多是重复的，可以用以下的索引数组来解决。
         *
         * 分析：一个正立方体，正对着的这一边，就是前面，z 轴=1； 后面 z 轴=-1；
         *      每一面，都是2个三角形构成；
         *      前面，z 轴都=-1；后面 z轴都=-1；左面涉及到前面的左边两顶点和后面的左边两点顶，所以它们只是 z 不同；
         *      同理，能得出其它三面所对应的顶点；
         * 综上，定义如下的 顶点索引数组
         */
        // 6 indices per cube side
        indexArray =  ByteBuffer.allocateDirect(6 * 6)
            .put(new byte[] {
                // Front
                1, 3, 0,
                0, 3, 2,
                
                // Back
                4, 6, 5,
                5, 6, 7,
                               
                // Left
                0, 2, 4,
                4, 2, 6,
                
                // Right
                5, 7, 1,
                1, 7, 3,
                
                // Top
                5, 1, 4,
                4, 1, 0,
                
                // Bottom
                6, 2, 7,
                7, 2, 3
            });
        indexArray.position(0);
    }
    public void bindData(SkyboxShaderProgram skyboxProgram) {
        vertexArray.setVertexAttribPointer(0,
            skyboxProgram.getPositionAttributeLocation(),
            POSITION_COMPONENT_COUNT, 0);               
    }
    
    public void draw() {
        //以前绘制顶点调用的是 glDrawArrays
        //绘制立方体，从绑定的 vertexArray中绘制indexArray 对应的36个顶点组成的三角形
        glDrawElements(GL_TRIANGLES, 36, GL_UNSIGNED_BYTE, indexArray);
    }
}