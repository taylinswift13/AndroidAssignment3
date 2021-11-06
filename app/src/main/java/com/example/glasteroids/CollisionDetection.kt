package com.example.glasteroids

//CollisionDetection.kt

import android.graphics.PointF
import kotlin.math.PI
import kotlin.math.abs

const val TO_DEGREES = (180.0f / PI).toFloat()
const val TO_RADIAS = (PI / 180.0f).toFloat()

fun triangleVsPoint(triVerts: ArrayList<PointF>, px: Float, py: Float): Boolean {
    assert(
        triVerts.size == 3,
        { "triangleVsPoints expects 3 vertices. For more complex shapes, use polygonVsPoint!" }
    )
    val p1 = triVerts[0]
    val p2 = triVerts[1]
    val p3 = triVerts[2]

    //calculate the area of the original triangle using Cramers Rule
    // https://web.archive.org/web/20070912110121/http://mcraefamily.com:80/MathHelp/GeometryTriangleAreaDeterminant.htm
    val triangleArea = abs((p2.x - p1.x) * (p3.y - p1.y) - (p3.x - p1.x) * (p2.y - p1.y))

    // get the area of 3 triangles made between the point, and each corner of the triangle
    val area1 = abs((p1.x - px) * (p2.y - py) - (p2.x - px) * (p1.y - py))
    val area2 = abs((p2.x - px) * (p3.y - py) - (p3.x - px) * (p2.y - py))
    val area3 = abs((p3.x - px) * (p1.y - py) - (p1.x - px) * (p3.y - py))

    // if the sum of the three areas equals the original we're inside the triangle.
    // we avoid equality comparisons on float by checking "larger than".
    if (area1 + area2 + area3 > triangleArea) {
        return false
    }
    return true
}