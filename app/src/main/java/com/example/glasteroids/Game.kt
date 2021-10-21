package com.example.glasteroids

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.AttributeSet
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.random.Random

lateinit var engine: Game
val WORLD_WIDTH =  160f //all dimensions are in meters
val WORLD_HEIGHT = 90f
val METERS_TO_SHOW_X = 160f //160m x 90m, the entire game world in view
val METERS_TO_SHOW_Y = 90f //TO DO: calculate to match screen aspect ratio
val STAR_COUNT = 100
var SECOND_IN_NANOSECONDS: Long = 1000000000
var MILLISECOND_IN_NANOSECONDS: Long = 1000000
var NANOSECONDS_TO_MILLISECONDS = 1.0f / MILLISECOND_IN_NANOSECONDS
var NANOSECONDS_TO_SECONDS = 1.0f / SECOND_IN_NANOSECONDS
class Game(ctx:Context,attrs:AttributeSet?=null)
    :GLSurfaceView(ctx,attrs),GLSurfaceView.Renderer{

    private val TAG = "Game"
    private val BG_COLOR = floatArrayOf(135f / 255f, 206f / 255f, 235f / 255f, 1f) //RGBA


    private val _stars = ArrayList<Star>()
    private val _asteroids=ArrayList<Asteroid>()
    // Create the projection Matrix. This is used to project the scene onto a 2D viewport.
    private val _viewportMatrix = FloatArray(5 * 4) //In essence, it is our our Camera
    //center the player in the world
    val _player = Player(WORLD_WIDTH/2f, 10f)
    val _border = Border(WORLD_WIDTH/2f, WORLD_HEIGHT/2f, WORLD_WIDTH, WORLD_HEIGHT)
    init {
        engine = this
        for (i in 0 until STAR_COUNT) {
            val x = Random.nextInt(WORLD_WIDTH.toInt()).toFloat()
            val y = Random.nextInt(WORLD_HEIGHT.toInt()).toFloat()
            _stars.add(Star(x, y))
        }
        for(i in 1 until 20){
            _asteroids.add(Asteroid(i*20.toFloat(),50f,Random.nextInt(3,8)))
        }
        setEGLContextClientVersion(2)
        setRenderer(this)
    }

    private fun hexColorToFloat(hex:Int)=hex/255f

    override fun onSurfaceCreated(unused: GL10?, p1: EGLConfig?) {

        GLES20.glClearColor(BG_COLOR[0], BG_COLOR[1], BG_COLOR[2], BG_COLOR[3])
        GLManager.buildProgram()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        // Set the OpenGL viewport to the same size as the surface.
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        //setup a projection matrix by passing in the range of the game world that will be mapped by OpenGL to the screen.
        //TO DO: encapsulate this in a Camera-class
        val offset = 0
        val left = 0f
        val right = METERS_TO_SHOW_X
        val bottom = METERS_TO_SHOW_Y
        val top = 0f
        val near = 0f
        val far = 1f
        Matrix.orthoM(_viewportMatrix, offset, left, right, bottom, top, near, far)
        for (s in _stars) {
            s.render(_viewportMatrix)
        }
        for (s in _asteroids) {
            s.render(_viewportMatrix)
        }
        _player.render(_viewportMatrix)
        _border.render(_viewportMatrix)
        update()
        render()

    }
    val dt = 0.01f
    var accumulator = 0.0f
    var currentTime = (System.nanoTime() * NANOSECONDS_TO_SECONDS).toFloat()
    private fun update() {

        val newTime = (System.nanoTime() * NANOSECONDS_TO_SECONDS).toFloat()
        val frameTime = newTime - currentTime
        currentTime = newTime
        accumulator += frameTime
        while (accumulator >= dt) {
            for (a in _asteroids) {
                a.update(dt)
            }
            _player.update(dt)
            accumulator -= dt
        }
    }

    private fun render() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT) //clear buffer to background color
        val offset = 0
        val left = 0f
        val right = METERS_TO_SHOW_X
        val bottom = METERS_TO_SHOW_Y
        val top = 0f
        val near = 0f
        val far = 1f
        Matrix.orthoM(_viewportMatrix, offset, left, right, bottom, top, near, far)
        _border.render(_viewportMatrix)
        for (s in _stars) {
            s.render(_viewportMatrix)
        }
        for (a in _asteroids) {
            a.render(_viewportMatrix)
        }
        _player.render(_viewportMatrix)
    }
}