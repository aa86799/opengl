package com.stone.opengl.live

import android.content.Context
import android.util.AttributeSet
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import javax.microedition.khronos.egl.EGLContext

/**
 * desc:
 * author:  stone
 * email:   aa86799@163.com
 * blog:    https://stone.blog.csdn.net
 * time:    2019-07-05 12:24
 */
class MyGlSurfaceView constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback {

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0) {

    }

    constructor(context: Context): this(context, null, 0) {

    }

    var surface: Surface? = null
    var eglContext: EGLContext? = null

    var glThread: MyGlThread? = null
    var render: MyGlRender? = null

    val RENDERMODE_WHEN_DIRTY = 0
    val RENDERMODE_CONTINUOUSLY = 1

    var renderMode = RENDERMODE_CONTINUOUSLY

    init {
        holder.addCallback(this)
    }

    fun requestRender() {
        glThread?.requestRender()
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        if (surface == null) surface = holder?.surface
        glThread = MyGlThread(this)
        glThread?.isCreate = true
        glThread?.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        glThread?.width = width
        glThread?.height = height
        glThread?.isChange = true
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        glThread?.onDestory()
        glThread = null
        surface = null
        eglContext = null
    }
}