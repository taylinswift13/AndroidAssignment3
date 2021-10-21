package com.example.glasteroids

import android.os.SystemClock
import kotlin.math.PI
import kotlin.math.sin
const val TO_RADIANS = PI.toFloat() / 180.0f

class Player (x: Float, y: Float) : GLEntity() {
    private  val TAG = "Player"
    init {
        _x = x
        _y = y
        _width = 8f; //TO DO: gameplay values! move to configs
        _height = 12f;
        _mesh = Triangle.mesh
        //_mesh.setWidthHeight(_width, _height);
        _mesh.flipY();
    }
    override fun update(dt: Float) {

    }
    override fun render(viewportMatrix: FloatArray) {
        val uptime = SystemClock.uptimeMillis() //get an (ever-increasing) timestamp to use as a counter
        val startPosition = WORLD_WIDTH / 2f
        val range = METERS_TO_SHOW_X / 2f //amplitude of our sine wave (how far to travel, in each direction)
        val speed = 360f / 1000f //I want the sine wave to complete a full revolution (360 degrees) in 2 seconds (2000 milliseconds).
        var angle = (uptime * speed) % 360f //use modulus (%) to turn linear, ever growing, timestamp into 0-359 range
        val five_seconds = uptime % 5000 //turn a timestamp into 0-4999 ms

        //sin() returns a numeric value between [-1.0, 1.0], the sine of the angle given in radians.
        //perfect for moving smoothly up-and-down some range!
        //remember than sin expects the angle in radians, not in degrees.
       // _x = startPosition + (sin(angle * TO_RADIANS) * range)
       // _rotation = (360.0f / 5000.0f) * five_seconds // Do a complete rotation every 5 seconds.
        _scale = 5f //render at 5x the size

        //ask the super class (GLEntity) to render us
        super.render(viewportMatrix)
    }

}