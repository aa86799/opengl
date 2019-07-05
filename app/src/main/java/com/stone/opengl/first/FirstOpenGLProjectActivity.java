/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
***/
package com.stone.opengl.first;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

public class FirstOpenGLProjectActivity extends Activity {
    /**
     * Hold a reference to our GLSurfaceView
     */
    private GLSurfaceView glSurfaceView;
    private boolean rendererSet = false; //记录 GLSurfaceView是否是有效状态

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        glSurfaceView = new GLSurfaceView(this);

        // Check if the system supports OpenGL ES 2.0.
        final ActivityManager activityManager =
            (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
 
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        /*
		
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;
         */
        // Even though the latest emulator supports OpenGL ES 2.0,
        // it has a bug where it doesn't set the reqGlEsVersion so
        // the above check doesn't work. The below will detect if the
        // app is running on an emulator, and assume that it supports
        // OpenGL ES 2.0.
        final boolean supportsEs2 =
            configurationInfo.reqGlEsVersion >= 0x20000
                || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
                 && (Build.FINGERPRINT.startsWith("generic")
                  || Build.FINGERPRINT.startsWith("unknown")
                  || Build.MODEL.contains("google_sdk")
                  || Build.MODEL.contains("Emulator")
                  || Build.MODEL.contains("Android SDK built for x86")));

        if (supportsEs2) {
            // Request an OpenGL ES 2.0 compatible context.
            glSurfaceView.setEGLContextClientVersion(2);

            // Assign our renderer.
            glSurfaceView.setRenderer(new FirstOpenGLProjectRenderer());
            //以设备显示刷新频率，不断的渲染  默认的
            glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
            //按请求渲染
//            glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
            rendererSet = true;
        } else {
            /*
             * This is where you could create an OpenGL ES 1.x compatible
             * renderer if you wanted to support both ES 1 and ES 2. Since we're
             * not doing anything, the app will crash if the device doesn't
             * support OpenGL ES 2.0. If we publish on the market, we should
             * also add the following to AndroidManifest.xml:
             * 
             * <uses-feature android:glEsVersion="0x00020000"
             * android:required="true" />
             * 
             * This hides our app from those devices which don't support OpenGL
             * ES 2.0.
             */
            Toast.makeText(this, "This device does not support OpenGL ES 2.0.",
                Toast.LENGTH_LONG).show();
            return;
        }

        setContentView(glSurfaceView);

        /*
        GLSurfaceView 会在后台线程中，调用渲染器 Renderer 的 渲染方法 onDrawFrame。
        glSurfaceView.queueEvent(runnable);  可在主线程中使用该方法，传递runnable给 渲染线程
        在渲染线程，可使用 activity.runOnUIThread(runnable)  或 其它方式 给主线程，
         */
    }

    @Override
    protected void onPause() {
        super.onPause();
        
        if (rendererSet) {
            glSurfaceView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        if (rendererSet) {
            glSurfaceView.onResume();
        }
    }
}