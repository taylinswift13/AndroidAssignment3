package com.example.glasteroids

import android.graphics.Color
import android.opengl.GLES20
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

val TIME_TO_LIVE_PARTICE = 1f
class Particle() : GLEntity() {
    var _ttl = TIME_TO_LIVE_PARTICE
    init {
        _mesh = Dot.mesh //all Stars use the exact same Mesh instance.
        setColors(1f, 1f, 1f, 1f)
    }
    fun fireFrom(x:Float,y:Float) {
        val theta = TO_RADIANS * Random.nextInt(0,360)
        _x = x + sin(theta)
        _y = y - cos(theta)
        _velX =(Random.nextInt(2) * 2 - 1)*between(30f, 50f)
        _velY = (Random.nextInt(2) * 2 - 1)*between(30f, 50f)
        _velX += sin(theta) * SPEED
        _velY -= cos(theta) * SPEED
        _ttl = TIME_TO_LIVE_PARTICE
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
}