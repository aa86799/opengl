package com.stone.opengl.live

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.stone.opengl.R

/*
 * 自定义 GLSurfaceView -> MyGlSurfaceView
 */
class MyGlActivity : AppCompatActivity() {

    private var surfaceView: MyGlSurfaceView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mygl)
        surfaceView = findViewById(R.id.activity_mygl_sv)



    }
}
