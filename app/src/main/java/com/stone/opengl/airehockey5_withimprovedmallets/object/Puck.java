package com.stone.opengl.airehockey5_withimprovedmallets.object;

import com.stone.opengl.airehockey5_withimprovedmallets.data.VertexArray;
import com.stone.opengl.airehockey5_withimprovedmallets.program.ColorShaderProgram;
import com.stone.opengl.airehockey5_withimprovedmallets.util.Geometry;

import java.util.List;

/**
 * desc     : 冰球
 * author   : stone
 * homepage : http://stone86.top
 * email    : aa86799@163.com
 * time     : 2018/11/10 22 34
 */
public class Puck {
    private static final int POSITION_COMPONENT_COUNT = 3; //这里表示颜色分量数量
    public final float radius, height;
    private final VertexArray vertexArray;
    private final List<ObjectBuilder.DrawCommand> drawList;

    public Puck(float radius, float height, int numPointsAroundPuck) {
        this.radius = radius;
        this.height = height;
        ObjectBuilder.GeneratedData generatedData = ObjectBuilder.createPuck(
                //点(0,0,0) 正中心
                new Geometry.Cylinder(new Geometry.Point(0, 0, 0), radius, height),
                numPointsAroundPuck
        );
        vertexArray = new VertexArray(generatedData.vertexData);
        drawList = generatedData.drawList;
    }

    //将顶点数据绑定到着色器
    public void bindData(ColorShaderProgram colorShaderProgram) {
        vertexArray.setVertexAttribPointer(0,
                colorShaderProgram.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT, 0);
    }

    public void draw() {
        for (ObjectBuilder.DrawCommand command : drawList) {
            command.draw();
        }
    }
}
