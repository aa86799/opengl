package com.stone.opengl.advanced4_lighting.object;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.stone.opengl.advanced4_lighting.constant.Constants;
import com.stone.opengl.advanced4_lighting.data.IndexBuffer;
import com.stone.opengl.advanced4_lighting.data.VertexBuffer;
import com.stone.opengl.advanced4_lighting.program.HeightmapShaderProgram;
import com.stone.opengl.advanced4_lighting.util.Geometry;

import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glDrawElements;

/**
 * desc     :
 * author   : stone
 * homepage : http://stone86.top
 * email    : aa86799@163.com
 * time     : 2018/11/14 10 58
 */
public class HeightMap {

    private static final int POSITION_COMPONENT_COUNT = 3; //高度图坐标 x,y,z

    private static final int NORMAL_COMPONENT_COUNT = 3; //光照位置、法线
    private static final int TOTAL_COMPONENT_COUNT =
            POSITION_COMPONENT_COUNT + NORMAL_COMPONENT_COUNT;
    private static final int STRIDE =
            (POSITION_COMPONENT_COUNT + NORMAL_COMPONENT_COUNT) * Constants.BYTES_PER_FLOAT;

    private final int width;
    private final int height;
    private final int numElements;
    private final VertexBuffer vertexBuffer;
    private final IndexBuffer indexBuffer;

    public HeightMap(Bitmap bitmap) {
        width = bitmap.getWidth();
        height = bitmap.getHeight();

        if (width * height > 65536) {
            throw new RuntimeException("Heightmap is too large for the index buffer.");
        }

        numElements = calculateNumElements();
        vertexBuffer = new VertexBuffer(loadBitmapData(bitmap));
        indexBuffer = new IndexBuffer(createIndexData());
    }

    private int calculateNumElements() {
        // There should be 2 triangles for every group of 4 vertices, so a
        // heightmap of, say, 10x10 pixels would have 9x9 groups, with 2
        // triangles per group and 3 vertices per triangle for a total of (9 x 9
        // x 2 x 3) indices.
        return (width - 1) * (height - 1) * 2 * 3; //*2*3 : 2两个三角形，每个三角形3个顶点；返回所有索引元素个数
    }

    /**
     * Copy the heightmap data into a vertex buffer object.
     */
    private float[] loadBitmapData(Bitmap bitmap) {
        final int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height); //新的空位图
        bitmap.recycle();

        final float[] heightmapVertices = new float[width * height * TOTAL_COMPONENT_COUNT];
        int offset = 0;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                // The heightmap will lie flat on the XZ plane and centered
                // around (0, 0), with the bitmap width mapped to X and the
                // bitmap height mapped to Z, and Y representing the height. We
                // assume the heightmap is grayscale, and use the value of the
                // red color to determine the height.
                final Geometry.Point point = getPoint(pixels, row, col);

                heightmapVertices[offset++] = point.x;
                heightmapVertices[offset++] = point.y;
                heightmapVertices[offset++] = point.z;

                final Geometry.Point top = getPoint(pixels, row - 1, col);
                final Geometry.Point left = getPoint(pixels, row, col - 1);
                final Geometry.Point right = getPoint(pixels, row, col + 1);
                final Geometry.Point bottom = getPoint(pixels, row + 1, col);

                final Geometry.Vector rightToLeft = Geometry.vectorBetween(right, left);
                final Geometry.Vector topToBottom = Geometry.vectorBetween(top, bottom);
                final Geometry.Vector normal = rightToLeft.crossProduct(topToBottom).normalize();

                heightmapVertices[offset++] = normal.x;
                heightmapVertices[offset++] = normal.y;
                heightmapVertices[offset++] = normal.z;
            }
        }
        return heightmapVertices;
    }

    /**
     * Returns a point at the expected position given by row and col, but if the
     * position is out of bounds, then it clamps the position and uses the
     * clamped position to read the height. For example, calling with row = -1
     * and col = 5 will set the position as if the point really was at -1 and 5,
     * but the height will be set to the heightmap height at (0, 5), since (-1,
     * 5) is out of bounds. This is useful when we're generating normals, and we
     * need to read the heights of neighbouring points.
     */
    private Geometry.Point getPoint(int[] pixels, int row, int col) {
        float x = ((float)col / (float)(width - 1)) - 0.5f;
        float z = ((float)row / (float)(height - 1)) - 0.5f;

        row = clamp(row, 0, width - 1);
        col = clamp(col, 0, height - 1);

        float y = (float)Color.red(pixels[(row * height) + col]) / (float)255;

        return new Geometry.Point(x, y, z);
    }

    private int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    /**
     * Create an index buffer object for the vertices to wrap them together into
     * triangles, creating indices based on the width and height of the
     * heightmap.
     */
    private short[] createIndexData() {
        final short[] indexData = new short[numElements];
        int offset = 0;

        for (int row = 0; row < height - 1; row++) {
            for (int col = 0; col < width - 1; col++) {
                // Note: The (short) cast will end up underflowing the number
                // into the negative range if it doesn't fit, which gives us the
                // right unsigned number for OpenGL due to two's complement.
                // This will work so long as the heightmap contains 65536 pixels
                // or less.  小于65536个像素
                short topLeftIndexNum = (short) (row * width + col);
                short topRightIndexNum = (short) (row * width + col + 1);
                short bottomLeftIndexNum = (short) ((row + 1) * width + col);
                short bottomRightIndexNum = (short) ((row + 1) * width + col + 1);

                // Write out two triangles.
                indexData[offset++] = topLeftIndexNum;
                indexData[offset++] = bottomLeftIndexNum;
                indexData[offset++] = topRightIndexNum;

                indexData[offset++] = topRightIndexNum;
                indexData[offset++] = bottomLeftIndexNum;
                indexData[offset++] = bottomRightIndexNum;
            }
        }

        return indexData;
    }
    public void bindData(HeightmapShaderProgram heightmapProgram) {
        vertexBuffer.setVertexAttribPointer(0,
                heightmapProgram.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT, STRIDE);

        vertexBuffer.setVertexAttribPointer(0,
                heightmapProgram.getNormalAttributeLocation(),
                POSITION_COMPONENT_COUNT, STRIDE);
    }

    public void draw() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer.getBufferId());
        glDrawElements(GL_TRIANGLES, numElements, GL_UNSIGNED_SHORT, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }
}
