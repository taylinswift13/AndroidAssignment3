package com.example.glasteroids

import android.graphics.PointF
import kotlin.math.cos
import kotlin.math.sin

private val BULLET_MESH = Dot.mesh //reusing the Dot (defined in Star.kt, but available throughout the package)
const val SPEED = 60f //TO DO: game play settings
const val TIME_TO_LIVE = 3f //seconds

class Bullet : GLEntity() {
    var _ttl = TIME_TO_LIVE
    init {
        setColors(1f, 0f, 1f, 1f)
        _mesh = BULLET_MESH //all bullets use the exact same mesh
    }

    fun fireFrom(source: GLEntity) {
        val theta = source._rotation * TO_RADIANS
        _x = source._x + sin(theta) * (source._width*0.5f);
        _y = source._y - cos(theta) * (source._height*0.5f);
        _velX = source._velX
        _velY = source._velY
        _velX += sin(theta) * SPEED
        _velY -= cos(theta) * SPEED
        _ttl = TIME_TO_LIVE
    }

    val isAlive: Boolean
        get() = _ttl > 0
    override fun isDead(): Boolean {
        return _ttl < 1
    }

    override fun update(dt: Float) {
        if (_ttl > 0)
        {
            _ttl -= dt
        }
        _x += _velX * dt;
        _y += _velY * dt;
    }

    override fun render(viewportMatrix: FloatArray) {
        if (_ttl > 0) {
            super.render(viewportMatrix)
        }
    }

    override fun isColliding(that: GLEntity): Boolean {
        if (!areBoundingSpheresOverlapping(this, that)) { //quick rejection
            return false
        }
        val asteroidVerts: ArrayList<PointF> = that.getPointList()
        return polygonVsPoint(asteroidVerts, _x, _y)

    }
}