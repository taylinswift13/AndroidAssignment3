package com.example.glasteroids

import android.opengl.GLES20
import kotlin.random.Random

//private const val MAX_VEL = 25f
//private const val MIN_VEL = -25f

fun between(min: Float, max: Float): Float = min + Random.nextFloat() * (max - min)

open class Asteroid (x: Float, y: Float, width:Float, height:Float, speed:Float, points: Int,score:Int,color:FloatArray) : GLEntity(){
    var _score=0
    init{
        assert(points >= 3, {"triangles or more, please. :)"})
        _x = x
        _y = y
        _width = width //TO DO: gameplay settings
        _height = height;
        _velX =(Random.nextInt(2) * 2 - 1)*between(speed-5, speed+5)
        _velY = (Random.nextInt(2) * 2 - 1)*between(speed-5, speed+5)
        _score = score
        _color = color
        val radius =_width*0.5f
        _mesh = Mesh(
            generateLinePolygon(points, radius),
            GLES20.GL_LINES
        )
        //_mesh.setWidthHeight(_width, _height);
    }

}

