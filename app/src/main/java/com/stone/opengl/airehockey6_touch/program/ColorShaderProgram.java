package com.stone.opengl.airehockey6_touch.program;

import android.content.Context;
import android.opengl.GLES20;

import com.stone.opengl.R;

import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;

/**
 * desc     :
 * author   : stone
 * homepage : http://stone86.top
 * email    : aa86799@163.com
 * time     : 2018/11/10 15 36
 */
public class ColorShaderProgram extends ShaderProgram {
    private final int uMatrixLocation;
    private final int aPositionLocation;
    private final int uColorLocation;

    public ColorShaderProgram(Context context) {
        super(context, R.raw.simple_vertex_shader_5, R.raw.simple_fragment_shader_5);

        uMatrixLocation = GLES20.glGetUniformLocation(mProgram, U_MATRIX);
        aPositionLocation = GLES20.glGetAttribLocation(mProgram, A_POSITION);
        uColorLocation = GLES20.glGetUniformLocation(mProgram, U_COLOR);
    }

    public void setUniforms(float[] matrix, float r, float g, float b) {
        //Pass the matrix into the shader program.
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);
        glUniform4f(uColorLocation, r, g, b, 1f);
    }

    public int getPositionAttributeLocation() {
        return aPositionLocation;
    }

}
