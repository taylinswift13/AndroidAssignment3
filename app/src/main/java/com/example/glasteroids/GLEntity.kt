package com.example.glasteroids

import android.graphics.PointF
import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log
import com.example.glasteroids.GLManager.draw

//re-usable singleton TriangleMesh
object Triangle{
    val mesh = Mesh(floatArrayOf( // in counterclockwise order:
        0.0f, 1f, 0.0f,  // top
        -0.5f, -0.5f, 0.0f,  // bottom left
        0.5f, -0.5f, 0.0f // bottom right
    ), GLES20.GL_TRIANGLES)
}
//re-usable matrices
val modelMatrix = FloatArray(4 * 4)
val viewportModelMatrix = FloatArray(4 * 4)
val viewportModelMatrix_1 = FloatArray(4 * 4)
val rotationViewportModelMatrix = FloatArray(4 * 4)

open class GLEntity {
    lateinit var _mesh: Mesh
    var _color = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f) //RGBA, default white
    var _x = 0.0f
    var _y = 0.0f
    var _depth = 0.0f //we'll use _depth for z-axis
    var _scale = 1f
    var _rotation = 0f
    var _velX = 0f
    var _velY = 0f
    var _width = 0.0f
    var _height = 0.0f


    var _isAlive = true
    var CollisionObject = 0
    open fun isDead(): Boolean {
        return !_isAlive
    }
    open fun radius(): Float {
        //use the longest side to calculate radius
        return if (_width > _height) _width * 0.5f else _height * 0.5f
    }

    open fun onCollision(that: GLEntity) {
        _isAlive = false
    }
//    open fun isColliding(that: GLEntity): Boolean {
//        return areBoundingSpheresOverlapping(this, that)
//        setColors(1.0f,0f,0f,1f)
//    }

    open fun isColliding(that: GLEntity): Boolean {
        if (this === that) {
            throw AssertionError("isColliding: You shouldn't test Entities against themselves!")
        }
        return isAABBOverlapping(this, that)
    }

    open fun centerX(): Float {
        return _x //assumes our mesh has been centered on [0,0] (normalized)
    }

    open fun centerY(): Float {
        return _y //assumes our mesh has been centered on [0,0] (normalized)
    }
    open fun update(dt: Float) {
        _x += _velX * dt;
        _y += _velY * dt;
        if(left() > WORLD_WIDTH){
            setRight(0f);
        }else if(right() < 0f){
            setLeft(WORLD_WIDTH);
        }
        if(top() > WORLD_HEIGHT){
            setBottom(0f);
        }else if(bottom() < 0f){
            setTop(WORLD_HEIGHT);
        }
       // _rotation++
    }
    open fun getPointList(): ArrayList<PointF> {
        return _mesh.getPointList(_x, _y, _rotation)
    }

    fun left() =  _x + _mesh.left()
    fun right()=  _x + _mesh.right()
    fun setLeft(leftEdgePosition: Float) {
        _x = leftEdgePosition - _mesh.left()
    }
    fun setRight(rightEdgePosition: Float) {
        _x = rightEdgePosition - _mesh.right()
    }
    fun top() =  _y + _mesh.top()
    fun bottom() = _y + _mesh.bottom()
    fun setTop(topEdgePosition: Float) {
        _y = topEdgePosition - _mesh.top()
    }
    fun setBottom(bottomEdgePosition: Float) {
        _y = bottomEdgePosition - _mesh.bottom()
    }


    open fun render(viewportMatrix: FloatArray) {
        //reset the model matrix and then translate (move) it into world space
        Matrix.setIdentityM(modelMatrix, OFFSET) //reset model matrix
        Matrix.translateM(modelMatrix, OFFSET, _x, _y, _depth)
        //viewportMatrix * modelMatrix combines into the viewportModelMatrix
        //NOTE: projection matrix on the left side and the model matrix on the right side.
        Matrix.multiplyMM(viewportModelMatrix, OFFSET, viewportMatrix, OFFSET, modelMatrix, OFFSET)
        //apply a rotation around the Z-axis to our modelMatrix. Rotation is in degrees.
        Matrix.setRotateM(modelMatrix, OFFSET, _rotation, 0f, 0f, 1.0f)
        //apply scaling to our modelMatrix, on the x and y axis only.
        Matrix.scaleM(modelMatrix, OFFSET, _scale, _scale, 1f)
        //finally, multiply the rotated & scaled model matrix into the model-viewport matrix
        //creating the final rotationViewportModelMatrix that we pass on to OpenGL
        Matrix.multiplyMM(
            rotationViewportModelMatrix,
            OFFSET,
            viewportModelMatrix,
            OFFSET,
            modelMatrix,
            OFFSET
        )
        draw(_mesh, rotationViewportModelMatrix, _color)
    }

    fun setColors(colors: FloatArray) {
        assert(colors.size == 4)
        setColors(colors[0], colors[1], colors[2], colors[3])
    }

    fun setColors(r: Float, g: Float, b: Float, a: Float) {
        _color[0] = r //red
        _color[1] = g //green
        _color[2] = b //blue
        _color[3] = a //alpha (transparency)
    }

}

//a basic axis-aligned bounding box intersection test.
//https://gamedev.stackexchange.com/questions/586/what-is-the-fastest-way-to-work-out-2d-bounding-box-intersection
fun isAABBOverlapping(a: GLEntity, b: GLEntity): Boolean {
    return !(a.right() <= b.left() || b.right() <= a.left() || a.bottom() <= b.top() || b.bottom() <= a.top())
}
fun polygonVsPolygon(polyA: ArrayList<PointF>, polyB: ArrayList<PointF>): Boolean {
    val count = polyA.size
    var next = 0
    for (current in 0 until count) {
        next = current + 1
        if (next == count) {
            next = 0
        }
        val segmentStart = polyA[current] //get a line segment from polyA
        val segmentEnd = polyA[next]
        if (polygonVsSegment(
                polyB,
                segmentStart,
                segmentEnd
            )
        ) { //compare the segment to all segments in polyB
            return true
        }
    }
    return false
}

fun polygonVsSegment(vertices: ArrayList<PointF>, segmentStart: PointF, segmentEnd: PointF): Boolean {
    val count = vertices.size
    var next = 0
    for (current in 0 until count) {
        next = current + 1
        if (next == count) {
            next = 0
        }
        val lineBStart = vertices[current]
        val lineBEnd = vertices[next]
        if (segmentVsSegment(segmentStart, segmentEnd, lineBStart, lineBEnd)) {
            return true
        }
    }
    return false
}

// takes 4 vertices, the begin and end for two line segments.
fun segmentVsSegment(
    lineAStart: PointF,
    lineAEnd: PointF,
    lineBStart: PointF,
    lineBEnd: PointF
): Boolean {
    val x1 = lineAStart.x
    val y1 = lineAStart.y //create some local names to make the typing easier further down
    val x2 = lineAEnd.x
    val y2 = lineAEnd.y
    val x3 = lineBStart.x
    val y3 = lineBStart.y
    val x4 = lineBEnd.x
    val y4 = lineBEnd.y
    //pre-calculate any value that's needed twice or more
    val dx1 = x2 - x1
    val dy1 = y2 - y1
    val dx2 = x4 - x3
    val dy2 = y4 - y3
    val cInv = 1f / (dy2 * dx1 - dx2 * dy1)
    // calculate the direction of the lines
    val uA = (dx2 * (y1 - y3) - dy2 * (x1 - x3)) * cInv
    val uB = (dx1 * (y1 - y3) - dy1 * (x1 - x3)) * cInv
    // if uA and uB are between 0-1, lines are colliding
    return uA >= 0 && uA <= 1 && uB >= 0 && uB <= 1
}
fun polygonVsPoint(vertices: ArrayList<PointF>, px: Float, py: Float): Boolean {
    val count = vertices.size
    var collision = false
    var next = 0
    for (current in 0 until count) {
        next = current + 1
        if (next == count) next = 0
        val segmentStart = vertices[current]
        val segmentEnd = vertices[next]
        // compare position, flip 'collision' variable back and forth
        // Look up "Crossing Number Algorithm" for details.
        // If our variable is odd after testing the vertex against every line we have a hit. If it is even, no collision has occurred.
        if ((segmentStart.y > py && segmentEnd.y < py || segmentStart.y < py && segmentEnd.y > py) &&  //Is the point's Y coordinate within the lines' Y-range?
            px < (segmentEnd.x - segmentStart.x) * (py - segmentStart.y) / (segmentEnd.y - segmentStart.y) + segmentStart.x
        ) { //look up the "jordan curve theorem"
            collision = !collision
        }
    }
    return collision
}

//a more refined AABB intersection test
//returns true on intersection, and sets the least intersecting axis in overlap
val overlap = PointF(0f, 0f); //re-usable PointF for collision detection. Assumes single threading.

@SuppressWarnings("UnusedReturnValue")
fun getOverlap(a: GLEntity, b: GLEntity, overlap: PointF): Boolean {
    overlap.x = 0.0f;
    overlap.y = 0.0f;
    val centerDeltaX = a.centerX() - b.centerX();
    val halfWidths = (a._width + b._width) * 0.5f;
    var dx = Math.abs(centerDeltaX); //cache the abs, we need it twice

    if (dx > halfWidths) return false; //no overlap on x == no collision

    val centerDeltaY = a.centerY() - b.centerY();
    val halfHeights = (a._height + b._height) * 0.5f;
    var dy = Math.abs(centerDeltaY);

    if (dy > halfHeights) return false; //no overlap on y == no collision

    dx = halfWidths - dx; //overlap on x
    dy = halfHeights - dy; //overlap on y
    if (dy < dx) {
        overlap.y = if (centerDeltaY < 0f) -dy else dy;
    } else if (dy > dx) {
        overlap.x = if (centerDeltaX < 0) -dx else dx;
    } else {
        overlap.x = if (centerDeltaX < 0) -dx else dx;
        overlap.y = if (centerDeltaY < 0) -dy else dy;
    }
    return true;
}
fun areBoundingSpheresOverlapping(a: GLEntity, b: GLEntity): Boolean {
    val dx = a.centerX() - b.centerX() //delta x
    val dy = a.centerY() - b.centerY()
    val distanceSq = dx * dx + dy * dy
    val minDistance = a.radius() + b.radius()
    val minDistanceSq = minDistance * minDistance
    return distanceSq < minDistanceSq
}
