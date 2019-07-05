package com.stone.opengl.live

/**
 * desc:
 * author:  stone
 * email:   aa86799@163.com
 * blog:    https://stone.blog.csdn.net
 * time:    2019-07-05 12:48
 */
interface MyGlRender {

    abstract fun onSurfaceCreated()
    abstract fun onSurfaceChanged(width: Int, height: Int)
    abstract fun onDrawFrame()
}