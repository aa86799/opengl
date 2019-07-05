package com.stone.opengl.airehockey6_touch.object;

import com.stone.opengl.airehockey6_touch.data.VertexArray;
import com.stone.opengl.airehockey6_touch.program.ColorShaderProgram;
import com.stone.opengl.airehockey6_touch.util.Geometry;

import java.util.List;

/**
 * desc     : 木槌
 * author   : stone
 * homepage : http://stone86.top
 * email    : aa86799@163.com
 * time     : 2018/11/10 15 31
 */
public class Mallet {
    private static final int POSITION_COMPONENT_COUNT = 3; //顶点坐标分量
    private final List<ObjectBuilder.DrawCommand> drawList;
    private final VertexArray vertexArray;
    public final float radius;
    public final float height;

    /**
     *
     * @param radius
     * @param height
     * @param numPointsAroundMallet 围绕着圆的点的个数
     */
    public Mallet(float radius, float height, int numPointsAroundMallet) {
        this.radius = radius;
        this.height = height;
        ObjectBuilder.GeneratedData generatedData = ObjectBuilder.createMallet(new Geometry.Point(0, 0, 0),
                radius, height, numPointsAroundMallet);
        vertexArray = new VertexArray(generatedData.vertexData);
        drawList = generatedData.drawList;
    }

    public void bindData(ColorShaderProgram colorShaderProgram) {
        vertexArray.setVertexAttribPointer(
                0,
                colorShaderProgram.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT,
                0);
    }

    public void draw() {
//        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 2); //绘制点

        for (ObjectBuilder.DrawCommand drawCommand : drawList) {
            drawCommand.draw();
        }
    }
}
