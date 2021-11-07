package com.example.glasteroids

import android.opengl.GLES20
import kotlin.random.Random

fun between(min: Float, max: Float): Float = min + Random.nextFloat() * (max - min)

open class Asteroid (x: Float, y: Float,points:Int,type:Int,speed:Float) : GLEntity(){
    var _score=0
    var points =3
    init{
        _x = x
        _y = y
        if(type==1)//small
        {
           _width = 8f //TO DO: gameplay settings
           _height = 8f;
           _velX =(Random.nextInt(2) * 2 - 1)*between(speed-5f,speed+5f)
           _velY = (Random.nextInt(2) * 2 - 1)*between(speed-5f,speed+5f)
           _score = 50
           _color = floatArrayOf(0.0f,0.0f,0.0f,1.0f)
        }
        if(type==2)//medium
        {
            _width = 15f //TO DO: gameplay settings
            _height = 15f;
            _velX =(Random.nextInt(2) * 2 - 1)*between(speed-5f,speed+5f)
            _velY = (Random.nextInt(2) * 2 - 1)*between(speed-5f,speed+5f)
            _score = 30
            _color = floatArrayOf(1.0f,1.0f,0.0f,1.0f)
        }
        if(type==3)//large
        {
            _width = 25f //TO DO: gameplay settings
            _height = 25f;
            _velX =(Random.nextInt(2) * 2 - 1)*between(speed-5f,speed+5f)
            _velY = (Random.nextInt(2) * 2 - 1)*between(speed-5f,speed+5f)
            _score = 10
            _color = floatArrayOf(1.0f,1.0f,1.0f,1.0f)
        }
        assert(points >= 3, {"triangles or more, please. :)"})
        val radius =_width*0.5f
        _mesh = Mesh(
            generateLinePolygon(points, radius),
            GLES20.GL_LINES
        )
        //_mesh.setWidthHeight(_width, _height);
    }

}

