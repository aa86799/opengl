package com.stone.opengl.live

import android.view.Surface

import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay
import javax.microedition.khronos.egl.EGLSurface

/*
 * egl 连接底层窗口系统
 */
class EglHelper {

    private var mEgl: EGL10? = null
    private var mEglDisplay: EGLDisplay? = null
    private var mEglContext: EGLContext? = null
    private var mEglSurface: EGLSurface? = null

    fun initEgl(surface: Surface, eglContext: EGLContext?) {

        //1、
        mEgl = EGLContext.getEGL() as EGL10

        //2、默认显示设备
        mEglDisplay = mEgl!!.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)
        if (mEglDisplay === EGL10.EGL_NO_DISPLAY) {
            throw RuntimeException("eglGetDisplay failed")
        }

        //3、初始化 egl
        val version = IntArray(2)
        if (!mEgl!!.eglInitialize(mEglDisplay, version)) {
            throw RuntimeException("eglInitialize failed")
        }

        //4、创建显示属性
        val attributes = intArrayOf(
            EGL10.EGL_RED_SIZE,
            8,
            EGL10.EGL_GREEN_SIZE,
            8,
            EGL10.EGL_BLUE_SIZE,
            8,
            EGL10.EGL_ALPHA_SIZE,
            8, //RGBA 8888
            EGL10.EGL_DEPTH_SIZE,
            8,
            EGL10.EGL_STENCIL_SIZE,
            8,
            EGL10.EGL_RENDERABLE_TYPE,
            4,
            EGL10.EGL_NONE
        )

        val numConfig = IntArray(1)
        //https://www.khronos.org/registry/EGL/sdk/docs/man/html/eglChooseConfig.xhtml
        if (!mEgl!!.eglChooseConfig(mEglDisplay, attributes, null, 1, numConfig)) {
            throw IllegalArgumentException("eglChooseConfig failed")
        }

        val numConfigs = numConfig[0]
        if (numConfigs <= 0) {
            throw IllegalArgumentException(
                "No configs match configSpec"
            )
        }

        //5、选择配置，初始化属性
        val configs = arrayOfNulls<EGLConfig>(numConfigs)
        if (!mEgl!!.eglChooseConfig(mEglDisplay, attributes, configs, numConfigs, numConfig)) {
            throw IllegalArgumentException("eglChooseConfig#2 failed")
        }

        //6、创建 eglContext
        if (eglContext != null) {
            mEglContext = mEgl!!.eglCreateContext(mEglDisplay, configs[0], eglContext, null)
        } else {
            mEglContext = mEgl!!.eglCreateContext(mEglDisplay, configs[0], EGL10.EGL_NO_CONTEXT, null)
        }

        //7、创建渲染的 surface
        mEglSurface = mEgl!!.eglCreateWindowSurface(mEglDisplay, configs[0], surface, null)

        //8、绑定 eglSurface 和 eglContext 到 eglDisplay
        if (!mEgl!!.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
            throw RuntimeException("eglMakeCurrent fail")
        }
    }

    fun swapBuffers(): Boolean {
        return if (mEgl != null) {
            //双缓冲
            mEgl!!.eglSwapBuffers(mEglDisplay, mEglSurface)
        } else {
            throw RuntimeException("egl is null")
        }
    }

    fun getmEglContext(): EGLContext? {
        return mEglContext
    }

    fun destoryEgl() {
        if (mEgl != null) {
            mEgl!!.eglMakeCurrent(
                mEglDisplay, EGL10.EGL_NO_SURFACE,
                EGL10.EGL_NO_SURFACE,
                EGL10.EGL_NO_CONTEXT
            )

            mEgl!!.eglDestroySurface(mEglDisplay, mEglSurface)
            mEglSurface = null

            mEgl!!.eglDestroyContext(mEglDisplay, mEglContext)
            mEglContext = null

            mEgl!!.eglTerminate(mEglDisplay)
            mEglDisplay = null
            mEgl = null
        }
    }

}
