package com.stone.opengl.airehockey4_textured.program;

import android.content.Context;
import android.opengl.GLES20;

import com.stone.opengl.R;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE1;
import static android.opengl.GLES20.GL_TEXTURE2;
import static android.opengl.GLES20.GL_TEXTURE31;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;

/**
 * desc     :
 * author   : stone
 * homepage : http://stone86.top
 * email    : aa86799@163.com
 * time     : 2018/11/10 15 24
 */
public class TextureShaderProgram extends ShaderProgram {

    private final int uMatrixLocation;
    private final int uTextureUnitLocation;
    private final int aPositionLocation;
    private final int aTextureCoordinatesLocation;

    public TextureShaderProgram(Context context) {
        super(context, R.raw.texture_vertex_shader, R.raw.texture_fragment_shader);

        uMatrixLocation = GLES20.glGetUniformLocation(mProgram, U_MATRIX);
        uTextureUnitLocation = GLES20.glGetUniformLocation(mProgram, U_TEXTURE_UNIT);
        aPositionLocation = GLES20.glGetAttribLocation(mProgram, A_POSITION);
        aTextureCoordinatesLocation = GLES20.glGetAttribLocation(mProgram, A_TEXTURE_COORDINATES);
    }

    public int getPositionAttributeLocation() {
        return aPositionLocation;
    }

    public int getTextureCoordinatesAttributeLocation() {
        return aTextureCoordinatesLocation;
    }

    public void setUniforms(float[] matrix, int textureId) {
        //Pass the matrix into the shader program.
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);

        //set the active texture unit to texture unit 0
        //设置活动的纹理单元为 0.  共有 0~31编号的纹理单元， 即共32个
        glActiveTexture(GL_TEXTURE0);
//        glActiveTexture(GL_TEXTURE1);
//        glActiveTexture(GL_TEXTURE2);
//        glActiveTexture(GL_TEXTURE31);

        //把纹理绑定到纹理单元
        glBindTexture(GL_TEXTURE_2D, textureId);

        //Tell the texture uniform sampler to use this texture in the shader by
        //telling it to read from texture unit 0.
        //把选定的纹理单元传递给 片断着色器中的 u_TextureUnit
        glUniform1i(uTextureUnitLocation, 0);
    }
}
