package com.stone.opengl.live

import android.opengl.GLES20
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import com.stone.opengl.R

/*
 * 自定义 EglHelper
 * 仿 opengl/java/android/opengl/GLSurfaceView 中的私有的静态内部类 EglHelper
 */
class MainActivity : AppCompatActivity() {

    private var surfaceView: SurfaceView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        surfaceView = findViewById(R.id.activity_main_sv)

        surfaceView!!.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {

            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

                object : Thread() {
                    override fun run() {
                        val eglHelper = EglHelper()
                        eglHelper.initEgl(holder.surface, null)

                        while (true) {
                            GLES20.glViewport(0, 0, width, height)

                            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT) //每帧都要清理
                            GLES20.glClearColor(1.0f, 1.0f, 0.0f, 1.0f)
                            eglHelper.swapBuffers()

                            try {
                                sleep(16)
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            }

                        }
                    }
                }.start()


            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                //egl.destroy
            }
        })


    }
}
