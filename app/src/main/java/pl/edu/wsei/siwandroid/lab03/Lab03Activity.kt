package pl.edu.wsei.siwandroid.lab03

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.animation.DecelerateInterpolator
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pl.edu.wsei.siwandroid.R
import java.util.*

class Lab03Activity : AppCompatActivity() {
    private lateinit var mBoard: GridLayout
    private lateinit var mBoardModel: MemoryBoardView
    private var rows: Int = 4
    private var cols: Int = 4

    private lateinit var completionPlayer: MediaPlayer
    private lateinit var negativePlayer: MediaPlayer
    private var isSound: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lab03)

        mBoard = findViewById(R.id.memory_board)
        rows = intent.getIntExtra("rows", 4)
        cols = intent.getIntExtra("cols", 4)

        if (savedInstanceState != null) {
            val allIcons = savedInstanceState.getIntArray("all_icons")
            val currentState = savedInstanceState.getIntArray("current_state")
            mBoardModel = MemoryBoardView(mBoard, cols, rows, allIcons)
            if (currentState != null) {
                mBoardModel.setState(currentState)
            }
            isSound = savedInstanceState.getBoolean("is_sound", true)
        } else {
            mBoardModel = MemoryBoardView(mBoard, cols, rows)
        }

        setupGameLogic()
    }

    override fun onResume() {
        super.onResume()
        completionPlayer = MediaPlayer.create(applicationContext, R.raw.completion)
        negativePlayer = MediaPlayer.create(applicationContext, R.raw.negative_guitar)
    }

    override fun onPause() {
        super.onPause()
        completionPlayer.release()
        negativePlayer.release()
    }

    private fun setupGameLogic() {
        mBoardModel.setOnGameChangeListener { e ->
            runOnUiThread {
                when (e.state) {
                    GameStates.Matching -> {
                        e.tiles.forEach { it.revealed = true }
                    }
                    GameStates.Match -> {
                        if (isSound) completionPlayer.start()
                        e.tiles.forEach { tile ->
                            tile.revealed = true
                            animatePairedButton(tile.button) {
                                tile.removeOnClickListener()
                            }
                        }
                    }
                    GameStates.NoMatch -> {
                        if (isSound) negativePlayer.start()
                        e.tiles.forEach { tile ->
                            tile.revealed = true
                            animateWrongPair(tile.button) {
                                tile.revealed = false
                            }
                        }
                    }
                    GameStates.Finished -> {
                        if (isSound) completionPlayer.start()
                        e.tiles.forEach { it.revealed = true }
                        Toast.makeText(this, "Gratulacje! Gra skończona!", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun animatePairedButton(button: ImageButton, action: Runnable) {
        val set = AnimatorSet()
        val random = Random()
        button.pivotX = random.nextFloat() * 200f
        button.pivotY = random.nextFloat() * 200f

        val rotation = ObjectAnimator.ofFloat(button, "rotation", 1080f)
        val scallingX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 4f)
        val scallingY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 4f)
        val fade = ObjectAnimator.ofFloat(button, "alpha", 1f, 0f)

        set.duration = 2000
        set.interpolator = DecelerateInterpolator()
        set.playTogether(rotation, scallingX, scallingY, fade)

        set.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                button.scaleX = 1f
                button.scaleY = 1f
                button.alpha = 0.0f
                action.run()
            }
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        set.start()
    }

    private fun animateWrongPair(button: ImageButton, action: Runnable) {
        val move = ObjectAnimator.ofFloat(button, "translationX", 0f, 25f, -25f, 25f, -25f, 0f)
        move.duration = 500
        move.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                action.run()
            }
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        move.start()
    }

    // --- OBSŁUGA MENU Z DYNAMICZNĄ IKONĄ ---

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.board_activity_menu, menu)

        // Ważne: przywrócenie poprawnej ikony po obrocie ekranu
        val soundItem = menu.findItem(R.id.board_activity_sound)
        if (isSound) {
            soundItem.setIcon(R.drawable.outline_call_24)
        } else {
            soundItem.setIcon(R.drawable.baseline_call_end_24)
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.board_activity_sound -> {
                isSound = !isSound // Przełączamy stan

                if (isSound) {
                    item.setIcon(R.drawable.outline_call_24)
                    Toast.makeText(this, "Dźwięk włączony", Toast.LENGTH_SHORT).show()
                } else {
                    item.setIcon(R.drawable.baseline_call_end_24)
                    Toast.makeText(this, "Dźwięk wyłączony", Toast.LENGTH_SHORT).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putIntArray("all_icons", mBoardModel.getAllTilesResources())
        outState.putIntArray("current_state", mBoardModel.getState())
        outState.putBoolean("is_sound", isSound)
    }
}