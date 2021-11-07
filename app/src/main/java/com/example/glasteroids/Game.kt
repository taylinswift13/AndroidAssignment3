package com.example.glasteroids

import Text
import android.content.Context
import android.content.SharedPreferences
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
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
const val TIME_BETWEEN_SHOTS = 0.35F //seconds. TO DO: game play setting!
const val BULLET_COUNT = (TIME_TO_LIVE / TIME_BETWEEN_SHOTS).toInt()+1
val NORMAL_TEXT_SCALE=0.6f
private val PLAYER_START_HP = 5
class Game(ctx:Context,attrs:AttributeSet?=null)
    :GLSurfaceView(ctx,attrs),GLSurfaceView.Renderer{
    private val PREFS= "com.example.glasteroids"
    private val jukebox = Jukebox(this)
    var _inputs = InputManager()

    private var _bullets = ArrayList<Bullet>(BULLET_COUNT)
    private var _particles = ArrayList<Particle>(PARTICLE_COUNT)
    private val _stars = ArrayList<Star>()
    private val _asteroids=ArrayList<Asteroid>()
    private val _texts = ArrayList<Text>()
    private val _viewportMatrix = FloatArray(5 * 4)
    private val _player = Player(WORLD_WIDTH/2f, 10f)
    private val _border = Border(0f, 0f, WORLD_WIDTH, WORLD_HEIGHT)

    private val TAG = "Game"
    private val BG_COLOR = floatArrayOf(135f / 255f, 206f / 255f, 235f / 255f, 1f) //RGBA
    private var score=0

    private var FPS_text = "FPS:"
    private var score_text = "0"
    private var asteroids_text = "asteroids:0/0"
    private var hp_text = "HP:5"
    private val gameOver_text=Text("GAME OVER",40f, 30f, 1.5f)
    private val gameWin_text = Text("YOU WIN", 40f, 30f, 1.5f)

    init {
        engine = this
        engine.onGameEvent(GameEvent.Start)
        for (i in 0 until STAR_COUNT) {
            val x = Random.nextInt(WORLD_WIDTH.toInt()).toFloat()
            val y = Random.nextInt(WORLD_HEIGHT.toInt()).toFloat()
            _stars.add(Star(x, y))
        }

        for(i in 0 until PARTICLE_COUNT){
            _particles.add(Particle())
        }
        initializeBullets()
        initializeAsteroids()
        initializeText()

        setEGLContextClientVersion(2)
        setRenderer(this)
    }
    fun initializeText(){
        _texts.add(Text(FPS_text, 5f, 5f, NORMAL_TEXT_SCALE))
        _texts.add(Text(score_text, 80f, 5f,1f))
        _texts.add(Text(asteroids_text, 5f, 10f, NORMAL_TEXT_SCALE))
        _texts.add(Text(hp_text, 5f, 15f, NORMAL_TEXT_SCALE))
        _texts.add(Text("LEVEL:1", 5f, 20f, NORMAL_TEXT_SCALE))
    }

    fun initializeAsteroids(){
        for(i in 0 until 2){
            //small
            _asteroids.add(Asteroid(i*5.toFloat(),50f,Random.nextInt(3,6),1,30f))
            //medium
            _asteroids.add(Asteroid(i*10.toFloat(),10f,Random.nextInt(5,9),2,15f))
            //large
            _asteroids.add(Asteroid(i*15.toFloat(),70f,Random.nextInt(8,12),3,8f))
        }
    }

    private fun initializeBullets() {
        for (i in 0 until BULLET_COUNT) {
            _bullets.add(Bullet())
        }
    }
    fun onGameEvent(event: GameEvent) {
        //TODO: really should schedule these by adding to an list, avoiding duplicates, and then start all unique sounds once per frame.
        jukebox.playEventSound(event)
    }

    var isRunning = false
    fun pause() {
        Log.d(TAG, "pause")
        isRunning = false
        jukebox.pauseBgMusic()
    }
    fun resume() {
        Log.d(TAG, "resume")
        jukebox.resumeBgMusic()
        isRunning = true
    }
    fun restart(){
        _bullets.clear()
        score=0
        GameOverSoundPlayed = false
        initializeAsteroids()
        initializeBullets()
        _player.health=PLAYER_START_HP
        engine.onGameEvent(GameEvent.Start)
    }


    fun getActivity() = context as MainActivity
    fun getAssets() = context.assets
    fun getPreferences(): SharedPreferences? {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)}
    fun getPreferencesEditor() = getPreferences()?.edit()
    fun savePreference(key: String, v: Boolean) = getPreferencesEditor()?.putBoolean(key, v)?.commit()

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
        _border.render(_viewportMatrix)
        for (s in _stars) {
            s.render(_viewportMatrix)
        }
        for (a in _asteroids) {
            a.render(_viewportMatrix)
        }
        _player.render(_viewportMatrix)
        for (b in _bullets) {
            if (b.isDead()) {
                continue
            }
            b.render(_viewportMatrix)
        }
        for (p in _particles) {
            p.render(_viewportMatrix)
        }
        for (t in _texts) {
            t.render(_viewportMatrix)
        }
        update()
        if(_player.health<=0){
            gameOver_text.render(_viewportMatrix)
        }
        if(_asteroids.size==0&&_player.health>0){
            gameWin_text.render(_viewportMatrix)
        }
    }
    private val dt = 0.01f
    private var accumulator = 0.0f
    private var currentTime = (System.nanoTime() * NANOSECONDS_TO_SECONDS).toFloat()
    private var frames=0
    private var startTime = System.currentTimeMillis()
    private var WinSoundPlayed=false
    private var GameOverSoundPlayed = false
    private var isGameOver = false

    private fun update() {
        onGameStateCheck()
        if(!isGameOver) {
            val newTime = (System.nanoTime() * NANOSECONDS_TO_SECONDS)
            val frameTime = newTime - currentTime
            currentTime = newTime
            accumulator += frameTime
            frames++
            while (accumulator >= dt) {
                getFPSandRenderHUD()
                for (a in _asteroids) {
                    a.update(dt)
                }
                for (b in _bullets) {
                    if (b.isDead()) {
                        continue
                    }
                    b.update(dt)
                }
                for (p in _particles) {
                    p.update(dt)
                }
                _player.update(dt)
                collisionDetection()
                renderScoreHUD()
                addNewEntities()
                removeDeadEntities()
                renderAstreoidsHUD()
                renderHPHUD()
            }
            gameWinSoundPlay()
        }
        else{
            gameOverSoundPlay()
        }

    }
    fun onGameStateCheck()
    {
        isGameOver = _player.health <= 0
    }
    fun gameWinSoundPlay()
    {
        if(_asteroids.size==0&&_player.health>0){
            if(!WinSoundPlayed)
            {
                engine.onGameEvent(GameEvent.Win)
                WinSoundPlayed=true
            }
        }
    }
    fun gameOverSoundPlay()
        {
            if(!GameOverSoundPlayed)
            {
                _asteroids.clear()
                engine.onGameEvent(GameEvent.GameOver)
                GameOverSoundPlayed=true
            }
        }


    fun getFPSandRenderHUD(){
        val nowTime = System.currentTimeMillis()
        val deltaTime: Long = nowTime - startTime

        if (deltaTime > 1000) {
            val secs = deltaTime.toFloat() / 1000
            val fps =  (frames / secs ).toInt()
            this.FPS_text ="FPS:$fps"
            _texts[0]=Text(this.FPS_text,5f, 5f, NORMAL_TEXT_SCALE)
            _player.setColors(0.0f,1.0f,0.0f,1.0f)
            startTime = nowTime
            frames = 0
        }
    }

    fun renderScoreHUD(){
        if(_texts.size!=0)
        {this.score_text ="$score"
            _texts[1]=Text(score_text,80f, 5f,1f)}
    }
    fun renderAstreoidsHUD(){
        if(_texts.size!=0)
        { this.asteroids_text="ASTEROIDS:${_asteroids.size}"
            _texts[2]=Text(asteroids_text,5f, 10f, NORMAL_TEXT_SCALE)}
        accumulator -= dt
    }
    fun renderHPHUD(){
        hp_text ="HP:${_player.health}"
        _texts[3]=Text(hp_text,5f, 15f, NORMAL_TEXT_SCALE)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action and MotionEvent.ACTION_MASK)
        {
            MotionEvent.ACTION_DOWN -> {
            if(_player.health<=0||_asteroids.size==0&&_player.health>0)
                restart()
            }
        }
        return true
    }
    fun maybeFireBullet(source: GLEntity): Boolean {
        for (b in _bullets) {
            if (b.isDead()){
                engine.onGameEvent(GameEvent.Bullut)
                b.fireFrom(source)
                return true
            }
        }
        return false
    }
    fun ParticleEffectPlay(x:Float,y:Float){
        for (p in _particles) {
            p.fireFrom(x,y)
        }
    }

    private fun collisionDetection() {
        for (b in _bullets)
        {
            if (b.isDead())
            { continue } //skip dead bullets
            for (a in _asteroids)
            {
                if (a.isDead())
                { continue } //skip dead asteroids
                if (b.isColliding(a)) {

                    ParticleEffectPlay(a._x,a._y)
                    score += a._score
                    engine.onGameEvent(GameEvent.Asteroid_crash)
                    a.CollisionObject = 1//BULLET
                    a.onCollision(b)
                    b.onCollision(a)
                }
            }

        }
        for (a in _asteroids) {
            if (a.isDead()) { continue }
            if (_player.isColliding(a)) {
                engine.onGameEvent(GameEvent.PlayerHurt)
                _player.health--
                a.onCollision(_player)
                _player.onCollision(a)
                a.CollisionObject = 2//PLAYER
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
//        for (i in _bullets.size - 1 downTo 0) {
//                if (!_bullets[i]._isAlive)
//                {
//                    _bullets.removeAt(i)
//                }
//            }
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
                                Random.nextInt(5,9),
                                2,
                                15f
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
                                    Random.nextInt(3,6 ),1,30f
                                )
                            )
                        }
                    }
                }
            }
        }
    }



}