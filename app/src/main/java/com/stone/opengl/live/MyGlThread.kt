package com.stone.opengl.live

import android.opengl.GLSurfaceView.RENDERMODE_CONTINUOUSLY
import android.opengl.GLSurfaceView.RENDERMODE_WHEN_DIRTY
import java.lang.ref.WeakReference
import javax.microedition.khronos.egl.EGLContext

/**
 * desc:
 * author:  stone
 * email:   aa86799@163.com
 * blog:    https://stone.blog.csdn.net
 * time:    2019-07-05 12:32
 */
class MyGlThread() : Thread() {
    private var eglSvReference: WeakReference<MyGlSurfaceView>? = null
    private var eglHelper: EglHelper? = null
//    private var `object`: Any = Any() //`` 与关键字重名需要加上； Any没有 并发相关函数 wait()、notify()、notifyAll()
    private var `object`:java.lang.Object? = java.lang.Object() //`` 与关键字重名需要加上;

    private var isExit = false
    var isCreate = false
    var isChange = false
    private var isStart = false

    var width: Int = 0
    var height: Int = 0

    constructor(sv: MyGlSurfaceView) : this() {
        this.eglSvReference = WeakReference(sv)
    }

    override fun run() {
        super.run()
        isExit = false
        isStart = false
        eglHelper = EglHelper()
        eglHelper!!.initEgl(eglSvReference?.get()?.surface!!, eglSvReference?.get()?.eglContext)

        while (true) {
            if (isExit) {
                //释放资源
                release()
                break
            }

            if (isStart) {
                if (eglSvReference!!.get()!!.renderMode == RENDERMODE_WHEN_DIRTY) {
                    synchronized(`object`!!) {
                        try {

                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }

                    }
                } else if (eglSvReference!!.get()!!.renderMode == RENDERMODE_CONTINUOUSLY) {
                    try {
                        Thread.sleep((1000 / 60).toLong())
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                } else {
                    throw RuntimeException("mRenderMode is wrong value")
                }
            }


            onCreate()
            onChange(width, height)
            onDraw()

            isStart = true


        }


    }

    private fun onCreate() {
        if (isCreate && eglSvReference!!.get()!!.render != null) {
            isCreate = false
            eglSvReference!!.get()!!.render?.onSurfaceCreated()
        }
    }

    private fun onChange(width: Int, height: Int) {
        if (isChange && eglSvReference!!.get()!!.render != null) {
            isChange = false
            eglSvReference!!.get()!!.render?.onSurfaceChanged(width, height)
        }
    }

    private fun onDraw() {
        if (eglSvReference!!.get()!!.render != null && eglHelper != null) {
            eglSvReference!!.get()!!.render?.onDrawFrame()
            if (!isStart) {
                eglSvReference!!.get()!!.render?.onDrawFrame()
            }
            eglHelper!!.swapBuffers()

        }
    }

    fun requestRender() {
        if (`object` != null) {
            synchronized(`object`!!) {
                `object`!!.notifyAll()
            }
        }
    }

    fun onDestory() {
        isExit = true
        requestRender()
    }


    private fun release() {
        if (eglHelper != null) {
            eglHelper!!.destoryEgl()
            eglHelper = null
            `object` = null
            eglSvReference = null
        }
    }

    fun getEglContext(): EGLContext? {
        return if (eglHelper != null) {
            eglHelper!!.getmEglContext()
        } else null
    }


}