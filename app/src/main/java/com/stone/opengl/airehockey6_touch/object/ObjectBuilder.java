package com.stone.opengl.airehockey6_touch.object;

import android.opengl.GLES20;

import com.stone.opengl.airehockey6_touch.util.Geometry;

import java.util.ArrayList;
import java.util.List;

import static android.opengl.GLES20.glDrawArrays;

/**
 * desc     : 物体构建器
 * author   : stone
 * homepage : http://stone86.top
 * email    : aa86799@163.com
 * time     : 2018/11/10 17 44
 */
public class ObjectBuilder {
    private static final int FLOATS_PER_VERTEX = 3; //顶点三分量：x，y，z
    private final float[] vertexData;
    private int offset;
    private final List<DrawCommand> drawList = new ArrayList<DrawCommand>();

    private ObjectBuilder(int sizeInVertices) {
        vertexData = new float[sizeInVertices * FLOATS_PER_VERTEX];
    }

    interface DrawCommand {
        void draw();
    }

    static class GeneratedData {
        final float[] vertexData;
        final List<DrawCommand> drawList;

        GeneratedData(float[] vertexData, List<DrawCommand> drawList) {
            this.vertexData = vertexData;
            this.drawList = drawList;
        }
    }

    /*
     * 需求：
     *  1. 调用者可以 决定物体有多少个点。点越多，图像越平滑。
     *  2. 物体将被包含在一个浮点数组中。物体被创建后，调用者将有一个绑定到OPENGL 的数组和一个绘制物体的命令。
     *  3. 物体将以调用者指定的位置为中心，并平放在x-z 平面上，即物体的顶部要指向正上方。
     *
     */

    /**
     * 计算圆柱体顶部 顶点的数量。
     * 一个圆柱体的顶部是一个和三角形扇构建的圆；它有一个顶点在圆心，围着圆的每个点都有一个顶点，
     * 围着圆的第一顶点要重复两次才能使圆闭合。
     * @param numPoints
     * @return
     */
    private static int sizeOfCircleInVertices(int numPoints) {
        return 1 + (numPoints + 1);
    }

    /**
     * 计算圆柱体侧面顶点的数量。
     * 一个圆柱体的侧面是一个卷起来的长方形，由一个三角形带构造，围着顶部圆的每个点都需要两个顶点，
     * 并且前两个顶点要重复两次，才能使这个管子闭合。
     * @param numPoints
     * @return
     */
    private static int sizeOfOpenCylinderInVertices(int numPoints) {
        return (numPoints + 1) * 2;
    }

    /**
     * 创建冰球
     * 由一个圆柱体的顶部 和 一个圆柱体的侧面构成
     * @param puck
     * @param numPoints
     * @return
     */
    static GeneratedData createPuck(Geometry.Cylinder puck, int numPoints) {
        //计算表示冰球的顶点数
        int size = sizeOfCircleInVertices(numPoints) + sizeOfOpenCylinderInVertices(numPoints);

        ObjectBuilder builder = new ObjectBuilder(size);

        //圆柱体顶部
        Geometry.Circle puckTop = new Geometry.Circle(
                puck.center.translateY(puck.height / 2f), puck.radius);

        builder.appendCircle(puckTop, numPoints);
        builder.appendOpenCylinder(puck, numPoints);

        return builder.build();
    }

    /**
     * 创建木槌
     * 由两个圆柱体构成。上面一个手柄，高度占75%；下面一个基部，高度占25%
     * @param center
     * @param radius
     * @param height
     * @param numPoints
     * @return
     */
    static GeneratedData createMallet(Geometry.Point center, float radius,
                                      float height, int numPoints) {
        int size = sizeOfCircleInVertices(numPoints) * 2 + sizeOfOpenCylinderInVertices(numPoints) * 2;

        ObjectBuilder builder = new ObjectBuilder(size);

        // First, generate the mallet base.
        float baseHeight = height * 0.25f;

        Geometry.Circle baseCircle = new Geometry.Circle(center.translateY(-baseHeight), radius);
        Geometry.Cylinder baseCylinder = new Geometry.Cylinder(
                baseCircle.center.translateY(-baseHeight / 2f), radius, baseHeight);

        builder.appendCircle(baseCircle, numPoints);
        builder.appendOpenCylinder(baseCylinder, numPoints);

        // Now generate the mallet handle.
        float handleHeight = height * 0.75f;
        float handleRadius = radius / 3f; //手柄的半径小一些

        //center.translateY(height * 0.5f ，即圆在顶部
        Geometry.Circle handleCircle = new Geometry.Circle(center.translateY(height * 0.5f), handleRadius);
        //cylinder.center 的 y 在 该柱体高度的一半
        Geometry.Cylinder handleCylinder = new Geometry.Cylinder(
                handleCircle.center.translateY(-handleHeight / 2f), handleRadius, handleHeight);

        builder.appendCircle(handleCircle, numPoints);
        builder.appendOpenCylinder(handleCylinder, numPoints);

        return builder.build();
    }

    /**
     * 添加圆
     * @param circle
     * @param numPoints
     */
    private void appendCircle(Geometry.Circle circle, int numPoints) {
        final int startVertex = offset / FLOATS_PER_VERTEX;
        final int numVertices = sizeOfCircleInVertices(numPoints);

        // Center point of fan
        vertexData[offset++] = circle.center.x;
        vertexData[offset++] = circle.center.y;
        vertexData[offset++] = circle.center.z;
        //这里相当于第一次， 下面循环中，当 i=0时，重复了一次

        // Fan around center point. <= is used because we want to generate
        // the point at the starting angle twice to complete the fan.
        for (int i = 0; i <= numPoints; i++) {
            //循环中，弧度范围 [0, 2pi]，即角度范围从 [0, 360]
            float angleInRadians = ((float) i / (float) numPoints) * ((float) Math.PI * 2f);

            //已知弧度和半径还有中心点 x，求顶圆环上顶点的 x
            vertexData[offset++] = (float) (circle.center.x + circle.radius * Math.cos(angleInRadians));
            vertexData[offset++] = circle.center.y;
            //一个柱体的顶部，是 xz 构成的平面；所以这里求 z 坐标
            vertexData[offset++] = (float) (circle.center.z + circle.radius * Math.sin(angleInRadians));
        }

        drawList.add(new DrawCommand() {
            @Override
            public void draw() {
                //绘制三角形扇
                glDrawArrays(GLES20.GL_TRIANGLE_FAN, startVertex, numVertices);
            }
        });

        //3 + 1 + N=32 = 36
    }

    /**
     * 添加圆柱
     * @param cylinder
     * @param numPoints
     */
    private void appendOpenCylinder(Geometry.Cylinder cylinder, int numPoints) {
        final int startVertex = offset / FLOATS_PER_VERTEX;
        final int numVertices = sizeOfOpenCylinderInVertices(numPoints);
        final float yStart = cylinder.center.y - (cylinder.height / 2f); //圆柱底部
        final float yEnd = cylinder.center.y + (cylinder.height / 2f); //圆柱顶部

        // Generate strip around center point. <= is used because we want to
        // generate the points at the starting angle twice, to complete the
        // strip.
        for (int i = 0; i <= numPoints; i++) {
            float angleInRadians = ((float) i / (float) numPoints) * ((float) Math.PI * 2f);

            float xPosition = (float) (cylinder.center.x + cylinder.radius * Math.cos(angleInRadians));

            float zPosition = (float) (cylinder.center.z + cylinder.radius * Math.sin(angleInRadians));

            vertexData[offset++] = xPosition;
            vertexData[offset++] = yStart;
            vertexData[offset++] = zPosition;

            vertexData[offset++] = xPosition;
            vertexData[offset++] = yEnd;
            vertexData[offset++] = zPosition;
            //从代码上来理解 前面说，侧面使用三角形带的 要重复两次前面两个顶点的说法：
            //x、z 位置会重复，y 一个为 start 底部，一个为 end 顶部
        }
        drawList.add(new DrawCommand() {
            @Override
            public void draw() {
                //绘制三角形带
                glDrawArrays(GLES20.GL_TRIANGLE_STRIP, startVertex, numVertices);
            }
        });
        //32*6=192 192+36=228
        //
    }

    private GeneratedData build() {
        return new GeneratedData(vertexData, drawList);
    }
}
