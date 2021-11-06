package com.example.glasteroids


import android.graphics.PointF
import android.os.SystemClock
import android.util.Log
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
const val TO_RADIANS = PI.toFloat() / 180.0f
const val ROTATION_VELOCITY = 360f //TODO: game play values!
const val THRUST = 1f
const val DRAG = 0.99f
class Player (x: Float, y: Float) : GLEntity() {
    private  val TAG = "Player"
    private var _bulletCooldown = 0f
    var health =5
    init {
        _x = x
        _y = y
        _width = 10f; //TO DO: gameplay values! move to configs
        _height = 10f;
        _mesh = Triangle.mesh
        //_mesh.setWidthHeight(_width, _height);
        _mesh.flipY();
        setColors(0.0f,1.0f,1.0f,1.0f)
    }
    override fun update(dt: Float) {
        _rotation += dt * ROTATION_VELOCITY * engine._inputs._horizontalFactor

        if (engine._inputs._pressingB) {
            val theta = _rotation * TO_RADIANS
            _velX += sin(theta) * THRUST
            _velY -= cos(theta) * THRUST
        }
        _velX *= DRAG
        _velY *= DRAG
        _bulletCooldown -= dt;
        if(engine._inputs._pressingA && _bulletCooldown <= 0f){
            if(engine.maybeFireBullet(this)){
                _bulletCooldown = TIME_BETWEEN_SHOTS;
            }
        }
        super.update(dt)
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
    override fun isColliding(that: GLEntity): Boolean {
        if (!areBoundingSpheresOverlapping(this, that)) {
            return false
        }
        val shipHull = getPointList()
        val asteroidHull = that.getPointList()
        if (polygonVsPolygon(shipHull, asteroidHull)) {
            return true
        }
        return polygonVsPoint(asteroidHull, _x, _y) //finally, check if we're inside the asteroid
    }

    override fun onCollision(that: GLEntity) {
        this.setColors(1.0f,0.0f,0.0f,1.0f)
    }
}