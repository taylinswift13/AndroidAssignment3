import android.opengl.Matrix
import com.example.glasteroids.*
import com.example.glasteroids.GLManager.draw


class Text(s: String, x: Float, y: Float,scale:Float ) : GLEntity() {
    var _meshes = ArrayList<Mesh>()
    private var _spacing = GLYPH_SPACING //spacing between characters
    private var _glyphWidth = GLYPH_WIDTH.toFloat()
    private var _glyphHeight = GLYPH_HEIGHT.toFloat()
    init {
        setString(s)
        _x = x
        _y = y
        //we can't use setWidthHeight, because normalization will break
        //the layout of the pixel-font. So we resort to simply scaling the text-entity
        setScale(scale); //TO DO: magic value. scaling to 75%
    }

    fun setString(s: String) {
        _meshes = GLPixelFont.getString(s)
    }

    override fun render(viewportMatrix: FloatArray) {
        for (i in _meshes.indices) {

           if (_meshes[i] == BLANK_SPACE) {
                continue
            }
            Matrix.setIdentityM(modelMatrix, OFFSET) //reset model matrix
            Matrix.translateM(modelMatrix, OFFSET, _x + (_glyphWidth + _spacing) * i, _y, _depth)
            Matrix.scaleM(modelMatrix, OFFSET, _scale, _scale, 1f)
            Matrix.multiplyMM(
                viewportModelMatrix,
                OFFSET,
                viewportMatrix,
                OFFSET,
                modelMatrix,
                OFFSET
            )
            draw(_meshes[i], viewportModelMatrix, floatArrayOf(0.0f,0.0f,0.0f,1.0f))
        }
    }

    fun setScale(factor: Float) {
        _scale = factor
        _spacing = GLYPH_SPACING * _scale
        _glyphWidth = GLYPH_WIDTH * _scale
        _glyphHeight = GLYPH_HEIGHT * _scale
        _height = _glyphHeight
        _width = (_glyphWidth + _spacing) * _meshes.size
    }
}