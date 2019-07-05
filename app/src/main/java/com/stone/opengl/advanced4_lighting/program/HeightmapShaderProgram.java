package com.stone.opengl.advanced4_lighting.program;

import android.content.Context;
import android.opengl.GLES20;

import com.stone.opengl.R;

import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform3fv;
import static android.opengl.GLES20.glUniform4fv;
import static android.opengl.GLES20.glUniformMatrix4fv;


/**
 * desc     :
 * author   : stone
 * homepage : http://stone86.top
 * email    : aa86799@163.com
 * time     : 2018/11/14 11 01
 */
public class HeightmapShaderProgram extends ShaderProgram {

    private final int uVectorToLightLocation;
    private final int uMVMatrixLocation;
    private final int uIT_MVMatrixLocation;
    private final int uMVPMatrixLocation;
    private final int uPointLightPositionsLocation;
    private final int uPointLightColorsLocation;

    private final int aPositionLocation;
    private final int aNormalLocation;

    public HeightmapShaderProgram(Context context) {
        super(context, R.raw.lighting_vertex_shader, R.raw.lighting_fragment_shader);


        uVectorToLightLocation = glGetUniformLocation(mProgram, U_VECTOR_TO_LIGHT);
        uMVMatrixLocation = glGetUniformLocation(mProgram, U_MV_MATRIX);
        uIT_MVMatrixLocation = glGetUniformLocation(mProgram, U_IT_MV_MATRIX);
        uMVPMatrixLocation = glGetUniformLocation(mProgram, U_MVP_MATRIX);
        uPointLightPositionsLocation =
                glGetUniformLocation(mProgram, U_POINT_LIGHT_POSITIONS);
        uPointLightColorsLocation =
                glGetUniformLocation(mProgram, U_POINT_LIGHT_COLORS);

        aPositionLocation = glGetAttribLocation(mProgram, A_POSITION);
        aNormalLocation = glGetAttribLocation(mProgram, A_NORMAL);

    }


    public void setUniforms(float[] mvMatrix,
                            float[] it_mvMatrix,
                            float[] mvpMatrix,
                            float[] vectorToDirectionalLight,
                            float[] pointLightPositions,
                            float[] pointLightColors) {
        glUniformMatrix4fv(uMVMatrixLocation, 1, false, mvMatrix, 0);
        glUniformMatrix4fv(uIT_MVMatrixLocation, 1, false, it_mvMatrix, 0);
        glUniformMatrix4fv(uMVPMatrixLocation, 1, false, mvpMatrix, 0);
        glUniform3fv(uVectorToLightLocation, 1, vectorToDirectionalLight, 0);

        glUniform4fv(uPointLightPositionsLocation, 3, pointLightPositions, 0);
        glUniform3fv(uPointLightColorsLocation, 3, pointLightColors, 0);
    }

    public int getPositionAttributeLocation() {
        return aPositionLocation;
    }

    public int getNormalAttributeLocation() {
        return aNormalLocation;
    }
}
