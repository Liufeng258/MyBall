package com.liufeng.ballfight

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import android.os.SystemClock
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import kotlin.math.sqrt
import kotlin.random.Random

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    // region 成员变量
    internal var gameThread: GameThread
    internal var score = 0
    private var isInitialized = false
    private var lastUpdateTime = 0L
    
    // 游戏对象
    private val balls = mutableListOf<Ball>()
    private var playerBall: Ball? = null
    private val paint = Paint().apply { 
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    // 输入控制
    private var targetX = 0f
    private var targetY = 0f
    
    // 图形效果
    private val particleSystem = ParticleSystem()
    private var ballTexture: Bitmap? = null
    
    // 音频系统
    internal var backgroundMusic: MediaPlayer? = null
    private var soundPool: SoundPool? = null
    private var eatSoundId = 0
    
    // 界面相关
    private val layoutInflater by lazy { 
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater 
    }
    private var pauseOverlay: View? = null

    var currentVolume: Float = 1.0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            backgroundMusic?.setVolume(field, field)
        }
    // endregion

    // region 初始化
    init {
        holder.addCallback(this)
        gameThread = GameThread(holder, this)
        initializeGame()
        initResources()
    }

    private fun initResources() {
        loadBallTexture()
        initAudioSystem()
    }

    private fun loadBallTexture() {
        try {
            ballTexture = BitmapFactory.decodeResource(resources, R.drawable.ball_texture)
        } catch (e: Exception) {
            showToast("无法加载皮肤纹理")
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun initAudioSystem() {
        backgroundMusic = MediaPlayer.create(context, R.raw.bg_music).apply {
            isLooping = true
            setVolume(currentVolume, currentVolume)
        }

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        eatSoundId = soundPool?.load(context, R.raw.eat_sound, 1) ?: 0
    }
    // endregion

    // region 游戏逻辑
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (!isInitialized) initializeGame()
    }

    private fun initializeGame() {
        synchronized(balls) {
            score = 0
            balls.clear()
            
            playerBall = Ball(
                x = width / 2f,
                y = height / 2f,
                radius = 50f,
                color = Color.BLUE
            ).apply { 
                texture = ballTexture?.let { 
                    Bitmap.createScaledBitmap(it, 
                        (radius * 2).toInt(), 
                        (radius * 2).toInt(), 
                        true
                    )
                }
            }
            
            balls.add(playerBall!!)
            repeat(20) { generateFood() }
            isInitialized = true
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (gameThread.paused) return false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                targetX = event.x
                targetY = event.y
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun update() {
        synchronized(balls) {
            val currentTime = SystemClock.elapsedRealtime()
            val deltaTime = (currentTime - lastUpdateTime).coerceAtMost(50L)
            lastUpdateTime = currentTime

            updatePlayerPosition(deltaTime)
            checkCollisions()
            particleSystem.update()
            generateRandomFood()
        }
    }

    private fun updatePlayerPosition(deltaTime: Long) {
        playerBall?.let { player ->
            val dx = targetX - player.x
            val dy = targetY - player.y
            val distance = sqrt(dx * dx + dy * dy)

            if (distance > 0) {
                val speed = 15f * (50f / player.radius) * (deltaTime / 16.67f)
                player.x += dx / distance * speed
                player.y += dy / distance * speed

                // 边界限制
                player.x = player.x.coerceIn(player.radius, width - player.radius)
                player.y = player.y.coerceIn(player.radius, height - player.radius)
            }
        }
    }

    private fun checkCollisions() {
        val iterator = balls.iterator()
        while (iterator.hasNext()) {
            val ball = iterator.next()
            if (ball == playerBall) continue
            
            playerBall?.let { player ->
                if (checkCollision(player, ball)) {
                    handleCollision(player, ball, iterator)
                }
            }
        }
    }

    private fun checkCollision(a: Ball, b: Ball): Boolean {
        val dx = a.x - b.x
        val dy = a.y - b.y
        return sqrt(dx * dx + dy * dy) < (a.radius + b.radius)
    }

    private fun handleCollision(player: Ball, other: Ball, iterator: MutableIterator<Ball>) {
        if (player.radius > other.radius) {
            score += (other.radius * 10).toInt()
            player.radius += other.radius / 10
            player.updateTexture()
            particleSystem.addExplosion(other.x, other.y)
            playEatSound()
            iterator.remove()
        } else {
            gameThread.running = false
            showGameOver()
        }
    }
    // endregion

    // region 辅助方法
    private fun playEatSound() {
        soundPool?.play(eatSoundId, currentVolume, currentVolume, 1, 0, 1f)
    }

    private fun generateRandomFood() {
        if (Random.nextInt(100) < 5) {
            generateFood()
        }
    }

    private fun generateFood() {
        balls.add(
            Ball(
                x = Random.nextFloat() * width,
                y = Random.nextFloat() * height,
                radius = 15f,
                color = Color.GREEN
            )
        )
    }

    private fun showToast(text: String) {
        (context as Activity).runOnUiThread {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showGameOver() {
        showToast("游戏结束！得分：$score")
    }
    // endregion

    // region 渲染系统
    override fun draw(canvas: Canvas?) {
        canvas?.let {
            drawBackground(it)
            drawGameObjects(it)
            drawUI(it)
        }
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.drawColor(Color.WHITE)
    }

    private fun drawGameObjects(canvas: Canvas) {
        synchronized(balls) {
            balls.forEach { ball ->
                ball.texture?.let { texture ->
                    canvas.drawBitmap(
                        texture,
                        ball.x - ball.radius,
                        ball.y - ball.radius,
                        paint
                    )
                } ?: run {
                    paint.color = ball.color
                    canvas.drawCircle(ball.x, ball.y, ball.radius, paint)
                }
            }
            particleSystem.draw(canvas)
        }
    }

    private fun drawUI(canvas: Canvas) {
        paint.color = Color.BLACK
        paint.textSize = 48f
        canvas.drawText("得分: $score", 50f, 100f, paint)
        
        // 绘制最高分
        val highScore = context.getSharedPreferences("game", Context.MODE_PRIVATE)
            .getInt("high_score", 0)
        canvas.drawText("最高: $highScore", width - 250f, 100f, paint)
    }
    // endregion

    // region 生命周期管理
    override fun surfaceCreated(holder: SurfaceHolder) {
        if (!isInitialized) initializeGame()
        gameThread.running = true
        gameThread.start()
        backgroundMusic?.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        gameThread.running = false
        gameThread.join()
        releaseResources()
    }

    private fun releaseResources() {
        backgroundMusic?.release()
        soundPool?.release()
        ballTexture?.recycle()
    }
    // endregion

    // region 暂停系统
    fun pauseGame() {
        gameThread.paused = true
        backgroundMusic?.pause()
        showPauseOverlay()
    }

    fun resumeGame() {
        gameThread.paused = false
        backgroundMusic?.start()
        hidePauseOverlay()
    }

    private fun showPauseOverlay() {
        (context as Activity).runOnUiThread {
            pauseOverlay = layoutInflater.inflate(R.layout.pause_overlay, null).apply {
                findViewById<Button>(R.id.btnResume).setOnClickListener { resumeGame() }
                findViewById<Button>(R.id.btnMainMenu).setOnClickListener { 
                    (context as Activity).finish()
                }
                (parent as? ViewGroup)?.addView(this)
            }
        }
    }

    private fun hidePauseOverlay() {
        (context as Activity).runOnUiThread {
            (parent as? ViewGroup)?.removeView(pauseOverlay)
            pauseOverlay = null
        }
    }
    // endregion

    // region 内部类
    inner class GameThread(
        private val holder: SurfaceHolder,
        private val gameView: GameView
    ) : Thread() {
        var running = false
        var paused = false

        override fun run() {
            var canvas: Canvas? = null
            while (running) {
                if (!paused) {
                    try {
                        canvas = holder.lockCanvas()
                        canvas?.let {
                            synchronized(holder) {
                                gameView.update()
                                gameView.draw(it)
                            }
                        }
                    } finally {
                        canvas?.let { holder.unlockCanvasAndPost(it) }
                    }
                    sleep(16)
                } else {
                    sleep(100)
                }
            }
        }
    }

    data class Ball(
        var x: Float,
        var y: Float,
        var radius: Float,
        val color: Int,
        private var originalTexture: Bitmap? = null
    ) {
        var texture: Bitmap? = null

        init {
            updateTexture()
        }

        fun updateTexture() {
            originalTexture?.let { orig ->
                texture = Bitmap.createScaledBitmap(
                    orig,
                    (radius * 2).toInt(),
                    (radius * 2).toInt(),
                    true
                )
            }
        }
    }

    private inner class ParticleSystem {
        private val particles = mutableListOf<Particle>()

        fun addExplosion(x: Float, y: Float) {
            repeat(30) {
                particles.add(
                    Particle(
                        x = x + Random.nextFloat() * 20 - 10,
                        y = y + Random.nextFloat() * 20 - 10
                    )
                )
            }
        }

        fun update() {
            particles.removeAll { it.life <= 0 }
            particles.forEach { it.update() }
        }

        fun draw(canvas: Canvas?) {
            particles.forEach { it.draw(canvas, paint) }
        }

        private inner class Particle(
            var x: Float,
            var y: Float,
            val velocityX: Float = Random.nextFloat() * 10 - 5,
            val velocityY: Float = Random.nextFloat() * 10 - 5,
            var life: Float = 1.0f
        ) {
            private val color = Color.argb(128, 
                Random.nextInt(256), 
                Random.nextInt(256), 
                Random.nextInt(256)
            )

            fun update() {
                x += velocityX
                y += velocityY
                life -= 0.02f
            }

            fun draw(canvas: Canvas?, paint: Paint) {
                paint.color = color
                paint.alpha = (life * 255).toInt()
                canvas?.drawCircle(x, y, 8f * life, paint)
            }
        }
    }
    // endregion
}