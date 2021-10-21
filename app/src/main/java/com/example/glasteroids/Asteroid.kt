package com.example.glasteroids

import android.opengl.GLES20
import kotlin.random.Random

private const val MAX_VEL = 25f
private const val MIN_VEL = -25f

fun between(min: Float, max: Float): Float = min + Random.nextFloat() * (max - min)

class Asteroid (x: Float, y: Float, points: Int) : GLEntity(){
    init{
        assert(points >= 3, {"triangles or more, please. :)"})
        _x = x
        _y = y
        _width = 12f; //TO DO: gameplay settings
        _height = _width;
        _velX = between(MIN_VEL, MAX_VEL)
        _velY = between(MIN_VEL, MAX_VEL)
        val radius =_width*0.5f
        _mesh = Mesh(
            generateLinePolygon(points, radius),
            GLES20.GL_LINES
        )
        //_mesh.setWidthHeight(_width, _height);
    }
}