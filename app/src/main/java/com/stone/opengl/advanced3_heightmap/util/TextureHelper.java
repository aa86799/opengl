package com.stone.opengl.advanced3_heightmap.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_LINEAR_MIPMAP_LINEAR;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_CUBE_MAP;
import static android.opengl.GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X;
import static android.opengl.GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y;
import static android.opengl.GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z;
import static android.opengl.GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
import static android.opengl.GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y;
import static android.opengl.GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glTexParameteri;

/**
 * desc     : 纹理帮助类
 * author   : stone
 * homepage : http://stone86.top
 * email    : aa86799@163.com
 * time     : 2018/11/10 13 13
 */
public class TextureHelper {

    private static String TAG = "TextureHelper";

    /**
     * 返回 加载图像后的 纹理 ID
     * @param context
     * @param resourceId
     */
    public static int loadTexture(Context context, int resourceId) {
        /*创建一个纹理对象*/
        final int[] textureObjectIds = new int[1];
        GLES20.glGenTextures(1, textureObjectIds, 0);
        if (textureObjectIds[0] == 0) {
            if (LoggerConfig.ON) {
                Log.w(TAG,"could not generate a new OpenGL texture object.");
            }
            return 0;
        }

        /* 将png、jpg 等图像，压缩为一个 Android 的位图 */
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        if (bitmap == null) {
            if (LoggerConfig.ON) {
                Log.w(TAG, "Resource ID " + resourceId + " could not be decoded");
            }
            GLES20.glDeleteTextures(1, textureObjectIds, 0);
            return 0;
        }

        //绑定纹理对象
        glBindTexture(GL_TEXTURE_2D, textureObjectIds[0]);

        /*
         * 纹理过滤
         * GL_TEXTURE_MIN_FILTER 缩小.  GL_TEXTURE_MAG_FILTER 放大。
         * 过滤模式
         *  GL_NEAREST                  最近邻过滤
         *  GL_NEAREST_MIPMAP_NEAREST   使用 MIP 贴图的最近邻过滤
         *  GL_NEAREST_MIPMAP_LINEAR    使用 MIP 贴图级别之间插值的最近邻过滤
         *  GL_LINEAR                   双线性过滤
         *  GL_LINEAR_MIPMAP_NEAREST    使用 MIP 贴图的双线性过滤
         *  GL_LINEAR_MIPMAP_LINEAR     三线性过滤(使用 MIP 贴图级别之间插值的双线性过滤)
         *
         *  在放大时，仅允许 GL_NEAREST、GL_NEAREST的过滤模式；
         *  在缩小时，都允许
         *
         */
        GLES20.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR_MIPMAP_LINEAR);

        //读入位图数据，并复制到绑定的纹理对象
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
//        GLES20.glTexImage2D();

        bitmap.recycle(); //手动释放

        //生成 所有必要级别的 MIP 贴图
        GLES20.glGenerateMipmap(GL_TEXTURE_2D);

        //完成了纹理加载后，解绑
        glBindTexture(GL_TEXTURE_2D, 0);

        return textureObjectIds[0];
    }

    /**
     * 加载立体贴图
     * @param context
     * @param cubeResources  顺序为 左右下上前后的资源图 id
     * @return
     */
    public static int loadCubeMap(Context context, int[] cubeResources) {
        final int[] textureObjectIds = new int[1];
        glGenTextures(1, textureObjectIds, 0); //创建纹理

        if (textureObjectIds[0] == 0) {
            if (LoggerConfig.ON) {
                Log.w(TAG, "Could not generate a new OpenGL texture object.");
            }
            return 0;
        }
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;//不缩放
        final Bitmap[] cubeBitmaps = new Bitmap[cubeResources.length];
        for (int i = 0; i < cubeBitmaps.length; i++) {
            cubeBitmaps[i] =
                    BitmapFactory.decodeResource(context.getResources(), cubeResources[i], options);

            if (cubeBitmaps[i] == null) {
                if (LoggerConfig.ON) {
                    Log.w(TAG, "Resource ID " + cubeResources[i]
                            + " could not be decoded.");
                }
                glDeleteTextures(1, textureObjectIds, 0);
                return 0;
            }
        }
        // Linear filtering for minification and magnification
        glBindTexture(GL_TEXTURE_CUBE_MAP, textureObjectIds[0]);

        //mip贴图之纹理过滤，缩小模式，双线性算法
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        //放大，双线性
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        /*
         * 把每图图像与立方体贴图面关联起来。
         *
         * 立方体贴图：内存使用左手坐标系统，外部使用右手坐标系统
         */
        GLUtils.texImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, cubeBitmaps[0], 0);
        GLUtils.texImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, cubeBitmaps[1], 0);
        GLUtils.texImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, cubeBitmaps[2], 0);
        GLUtils.texImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, cubeBitmaps[3], 0);
        GLUtils.texImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, cubeBitmaps[4], 0);
        GLUtils.texImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, cubeBitmaps[5], 0);

        glBindTexture(GL_TEXTURE_2D, 0);

        for (Bitmap bitmap : cubeBitmaps) {
            bitmap.recycle();
        }

        return textureObjectIds[0];
    }
}
