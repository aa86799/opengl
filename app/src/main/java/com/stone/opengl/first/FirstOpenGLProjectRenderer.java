/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
***/
package com.stone.opengl.first;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glViewport;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView.Renderer;

public class FirstOpenGLProjectRenderer implements Renderer {
    @Override //可能被多次调用
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        // Set the background clear color to red. The first component is
        // red, the second is green, the third is blue, and the last
        // component is alpha, which we don't use in this lesson.
        // RGBA通道 范围0~1； 可看成是  (0~1)*255
        glClearColor(0.0f, 0.0f, 0.5f, 0.0f);
    }

    /**
     * onSurfaceChanged is called whenever the surface has changed. This is
     * called at least once when the surface is initialized. Keep in mind that
     * Android normally restarts an Activity on rotation, and in that case, the
     * renderer will be destroyed and a new one created.
     * 
     * @param width
     *            The new width, in pixels.
     * @param height
     *            The new height, in pixels.
     */
    @Override //尺寸变化时调用  如横竖屏切换
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        // Set the OpenGL viewport to fill the entire surface.
        // 设置 视口
        glViewport(0, 0, width, height);
    }

    /**
     * OnDrawFrame is called whenever a new frame needs to be drawn. Normally,
     * this is done at the refresh rate of the screen.
     *  绘制一帧。 必须要绘制一些东西，即使只是清空屏幕，否则会有闪烁效果
     *
     *
     */
    @Override
    public void onDrawFrame(GL10 glUnused) {
        // Clear the rendering surface.  清空屏幕，并使用之前glClearColor 调用的颜色 填充视口
        glClear(GL_COLOR_BUFFER_BIT);
    }
}
