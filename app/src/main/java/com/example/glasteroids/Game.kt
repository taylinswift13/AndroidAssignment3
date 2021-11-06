package com.example.glasteroids

import Text
import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.AttributeSet
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.random.Random


lateinit var engine: Game
val WORLD_WIDTH =  160f //all dimensions are in meters
val WORLD_HEIGHT = 90f
val METERS_TO_SHOW_X = 160f //160m x 90m, the entire game world in view
val METERS_TO_SHOW_Y = 90f //TO DO: calculate to match screen aspect ratio
val STAR_COUNT = 100
val PARTICLE_COUNT = 30
var SECOND_IN_NANOSECONDS: Long = 1000000000
var MILLISECOND_IN_NANOSECONDS: Long = 1000000
var NANOSECONDS_TO_MILLISECONDS = 1.0f / MILLISECOND_IN_NANOSECONDS
var NANOSECONDS_TO_SECONDS = 1.0f / SECOND_IN_NANOSECONDS
const val TIME_BETWEEN_SHOTS = 0.25F //seconds. TO DO: game play setting!
const val BULLET_COUNT = (TIME_TO_LIVE / TIME_BETWEEN_SHOTS).toInt()+1
val NORMAL_TEXT_SCALE=0.6f
class Game(ctx:Context,attrs:AttributeSet?=null)
    :GLSurfaceView(ctx,attrs),GLSurfaceView.Renderer{
    private val jukebox = Jukebox(context.assets)
    private val _texts = ArrayList<Text>()
    private val _flames = ArrayList<Flame>()
    private val TAG = "Game"
    private val BG_COLOR = floatArrayOf(135f / 255f, 206f / 255f, 235f / 255f, 1f) //RGBA

    var _bullets = ArrayList<Bullet>(BULLET_COUNT)
    var _particles = ArrayList<Particle>(PARTICLE_COUNT)
    private val _stars = ArrayList<Star>()
    private val _asteroids=ArrayList<Asteroid>()
    // Create the projection Matrix. This is used to project the scene onto a 2D viewport.
    private val _viewportMatrix = FloatArray(5 * 4) //In essence, it is our our Camera
    //center the player in the world
    val _player = Player(WORLD_WIDTH/2f, 10f)

    val _border = Border(0f, 0f, WORLD_WIDTH, WORLD_HEIGHT)
    var _inputs = InputManager() //empty but valid default
    var FPS_text = "FPS:"
    var score_text = "0"
    var asteroids_text = "asteroids:0/0"
    var hp_text = "HP:5"
    var score=0

    init {
        engine = this

        for (i in 0 until STAR_COUNT) {
            val x = Random.nextInt(WORLD_WIDTH.toInt()).toFloat()
            val y = Random.nextInt(WORLD_HEIGHT.toInt()).toFloat()
            _stars.add(Star(x, y))
        }

        for (i in 0 until BULLET_COUNT) {
            _bullets.add(Bullet())
        }
        for(i in 0 until PARTICLE_COUNT){
            _particles.add(Particle())
        }
        for(i in 0 until 2){
            //small
            _asteroids.add(Asteroid(i*5.toFloat(),50f,8f,8f,30f,Random.nextInt(3,7),50, floatArrayOf(0.0f,0.0f,0.0f,1.0f)))
            //medium
            _asteroids.add(Asteroid(i*10.toFloat(),10f,15f,15f,20f,Random.nextInt(5,10),30, floatArrayOf(1.0f,1.0f,0.0f,1.0f)))
            //large
            _asteroids.add(Asteroid(i*15.toFloat(),70f,20f,20f,5f,Random.nextInt(8,12),10, floatArrayOf(1.0f,1.0f,1.0f,1.0f)))
        }

        _texts.add(Text(FPS_text, 5f, 5f, NORMAL_TEXT_SCALE))
        _texts.add(Text(score_text, 80f, 5f,1f))
        _texts.add(Text(asteroids_text, 5f, 10f, NORMAL_TEXT_SCALE))
        _texts.add(Text(hp_text, 5f, 15f, NORMAL_TEXT_SCALE))
        _texts.add(Text("LEVEL:1", 5f, 20f, NORMAL_TEXT_SCALE))


        _flames.add(Flame("+",_player._x,_player._y,0f,1.0f))
        setEGLContextClientVersion(2)
        setRenderer(this)
    }

    private fun hexColorToFloat(hex:Int)=hex/255f
    fun setControls(input: InputManager) {
        _inputs.onPause()
        _inputs.onStop()
        _inputs = input
        _inputs.onResume()
        _inputs.onStart()
    }
    override fun onSurfaceCreated(unused: GL10?, p1: EGLConfig?) {

        GLES20.glClearColor(BG_COLOR[0], BG_COLOR[1], BG_COLOR[2], BG_COLOR[3])
        GLManager.buildProgram()

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        // Set the OpenGL viewport to the same size as the surface.
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        //setup a projection matrix by passing in the range of the game world that will be mapped by OpenGL to the screen.
        //TO DO: encapsulate this in a Camera-class
        val offset = 0
        val left = 0f
        val right = METERS_TO_SHOW_X
        val bottom = METERS_TO_SHOW_Y
        val top = 0f
        val near = 0f
        val far = 1f
        Matrix.orthoM(_viewportMatrix, offset, left, right, bottom, top, near, far)
        for (s in _stars) {
            s.render(_viewportMatrix)
        }
        for (a in _asteroids) {
            a.render(_viewportMatrix)
        }
        _player.render(_viewportMatrix)
        _border.render(_viewportMatrix)
        for (t in _texts) {
            t.render(_viewportMatrix)
        }
        for (b in _bullets) {
            if (b.isDead()) {
                continue
            }
            b.render(_viewportMatrix)
        }
        for (p in _particles) {
            p.render(_viewportMatrix)
        }
        update()
    }
    val dt = 0.01f
    var accumulator = 0.0f
    var currentTime = (System.nanoTime() * NANOSECONDS_TO_SECONDS).toFloat()
    var frames=0;
    var startTime = System.currentTimeMillis()
    var isGameWin=false
    var isGameOver = false
    private fun update() {
        if(_player.health>0){
            val newTime = (System.nanoTime() * NANOSECONDS_TO_SECONDS).toFloat()
            val frameTime = newTime - currentTime
            currentTime = newTime
            frames++

            val nowTime = System.currentTimeMillis()
            val deltaTime: Long = nowTime - startTime

            if (deltaTime > 1000) {
                val secs = deltaTime.toFloat() / 1000
                val fps =  (frames / secs ).toInt()
                this.FPS_text ="FPS:$fps"
                _texts[0]=Text(this.FPS_text,5f, 5f, NORMAL_TEXT_SCALE)
                _player.setColors(0.0f,1.0f,1.0f,1.0f)
                startTime = nowTime
                frames = 0
            }
            accumulator += frameTime
            while (accumulator >= dt) {
                Log.d(TAG,"Particles:${_particles.size}")
                for (a in _asteroids) {
                    a.update(dt)
                }
                for (b in _bullets) {
                    if (b.isDead()) { continue }
                    b.update(dt)
                }
                for(p in _particles){
                    p.update(dt)
                }
                _player.update(dt)

                collisionDetection();

                _texts[1]=Text(score_text,80f, 5f,1f)
                removeDeadEntities();
                addNewEntities()
                this.asteroids_text="ASTEROIDS:${_asteroids.size}"
                _texts[2]=Text(asteroids_text,5f, 10f, NORMAL_TEXT_SCALE)
                accumulator -= dt
            }

            if(_asteroids.size==0){
                _texts.add(Text("YOU WIN", 50f, 40f, 1.5f))
                if(!isGameWin)
                {
                    jukebox.play(SFX.win)
                    isGameWin=true
                }
            }
        }
        else{
            _texts.add(Text("GAME OVER", 50f, 40f, 1.5f))
            if(!isGameOver)
            {
                jukebox.play(SFX.lose)
                isGameOver=true
            }
        }

    }

    fun maybeFireBullet(source: GLEntity): Boolean {
        for (b in _bullets) {
            if (b.isDead()){
                jukebox.play(SFX.bullet)
                b.fireFrom(source)
                return true
            }
        }
        return false
    }
    private fun collisionDetection() {
        for (b in _bullets)
        {
            if (b.isDead()||!b._isAlive)
            { continue } //skip dead bullets
            for (a in _asteroids)
            {
                if (a.isDead())
                { continue } //skip dead asteroids
                if (b.isColliding(a)) {
                    for (p in _particles) {
                        p.fireFrom(a._x,a._y)
                    }
                    score += a._score
                    this.score_text ="$score"
                    jukebox.play(SFX.asteroid_crash)
                    a.CollisionObject = 1//BULLET
                    a.onCollision(b)
                    b.onCollision(a)
                }
            }

        }
        for (a in _asteroids) {
            if (a.isDead()) { continue }
            if (_player.isColliding(a)) {
                jukebox.play(SFX.player_hurt)
                _player.health--
                a.onCollision(_player)
                _player.onCollision(a)
                a.CollisionObject = 2//PLAYER
                hp_text ="HP:${_player.health}"
                _texts[3]=Text(hp_text,5f, 15f, NORMAL_TEXT_SCALE)
            }
        }
    }

    fun removeDeadEntities() {
        for (i in _asteroids.size - 1 downTo 0)
        {
            if (_asteroids[i].isDead())
            {
                _asteroids.removeAt(i)
            }
        }
        for (i in _bullets.size - 1 downTo 0) {
                if (!_bullets[i]._isAlive)
                {
                    _bullets.removeAt(i)
                }
            }
       }
    fun addNewEntities(){
        for (i in _asteroids.size - 1 downTo 0)
        {
            if (_asteroids[i].isDead())
            {
                if (_asteroids[i].CollisionObject == 1)
                {
                    //large type breaks into 2 medium parts
                    if (_asteroids[i]._score == 10) {
                        for (j in 0 until 2)
                        { _asteroids.add(
                            Asteroid(
                                _asteroids[i]._x,
                                _asteroids[i]._y,
                                15f,
                                15f,
                                20f,
                                Random.nextInt(5, 10),
                                30,
                                floatArrayOf(1.0f,1.0f,0.0f,1.0f)
                            )
                        )
                        }
                    }
                    //medium one breaks into 2 small parts
                    else if (_asteroids[i]._score == 30) {
                        for (j in 0 until 2){
                            _asteroids.add(
                                Asteroid(
                                    _asteroids[i]._x,
                                    _asteroids[i]._y,
                                    8f,
                                    8f,
                                    30f,
                                    Random.nextInt(3, 7),
                                    50,
                                    floatArrayOf(0.0f,0.0f,0.0f,1.0f)
                                )
                            )
                        }
                    }
                }
            }
        }
    }



}