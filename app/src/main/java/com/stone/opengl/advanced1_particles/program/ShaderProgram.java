package com.stone.opengl.advanced1_particles.program;

import android.content.Context;
import android.opengl.GLES20;

import com.stone.opengl.advanced1_particles.util.ShaderHelper;
import com.stone.opengl.advanced1_particles.util.TextResourceReader;

/**
 * desc     :
 * author   : stone
 * homepage : http://stone86.top
 * email    : aa86799@163.com
 * time     : 2018/11/10 15 46
 */
public class ShaderProgram {
    protected static final String U_MATRIX = "u_Matrix";
    protected static final String U_TIME = "u_Time";
    protected static final String U_TEXTURE_UNIT = "u_TextureUnit";

    protected static final String A_POSITION = "a_Position";
    protected static final String A_COLOR = "a_Color";
    protected static final String A_DIRECTION_VECTOR = "a_DirectionVector";
    protected static final String A_PARTICLE_START_TIME = "a_ParticleStartTime";

    protected final int mProgram;

    public ShaderProgram(Context context, int vertexShaderResourceId, int fragmentShaderResourceId) {
        this.mProgram = ShaderHelper.buildProgram(
                TextResourceReader.readTextFileFromResource(context, vertexShaderResourceId),
                TextResourceReader.readTextFileFromResource(context, fragmentShaderResourceId)
        );
    }

    public void useProgram() {
        GLES20.glUseProgram(mProgram);
    }
}
