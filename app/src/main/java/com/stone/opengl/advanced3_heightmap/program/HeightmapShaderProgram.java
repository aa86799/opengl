package com.stone.opengl.advanced3_heightmap.program;

import android.content.Context;
import android.opengl.GLES20;

import com.stone.opengl.R;


/**
 * desc     :
 * author   : stone
 * homepage : http://stone86.top
 * email    : aa86799@163.com
 * time     : 2018/11/14 11 01
 */
public class HeightmapShaderProgram extends ShaderProgram {

    private final int uMatrixLocation;
    private final int aPositionLocation;

    public HeightmapShaderProgram(Context context) {
        super(context, R.raw.heightmap_vertex_shader, R.raw.heightmap_fragment_shader);

        this.uMatrixLocation = GLES20.glGetUniformLocation(mProgram, U_MATRIX);

        this.aPositionLocation = GLES20.glGetAttribLocation(mProgram, A_POSITION);

    }

    public void setUniforms(float[] matrix) {
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);
    }

    public int getPositionAttributeLocation() {
        return aPositionLocation;
    }
}
