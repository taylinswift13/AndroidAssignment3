package com.example.glasteroids

import android.content.Context
import android.graphics.Color
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class Game(ctx:Context,attrs:AttributeSet?=null)
    :GLSurfaceView(ctx,attrs),GLSurfaceView.Renderer{
    private val bgColor = Color.rgb(30,50,20)
    init {
        setEGLContextClientVersion(2)
        setRenderer(this)
    }

    fun hexColorToFloat(hex:Int)=hex/255f

    override fun onSurfaceCreated(unused: GL10?, p1: EGLConfig?) {

        val red=hexColorToFloat(bgColor.red)
        val green=hexColorToFloat(bgColor.green)
        val blue=hexColorToFloat(bgColor.blue)
        val alpha =1.0f
        GLES20.glClearColor(red,green,blue,alpha)
    }

    override fun onSurfaceChanged(unused: GL10?, p1: Int, p2: Int) {

    }

    override fun onDrawFrame(unused: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
    }
}