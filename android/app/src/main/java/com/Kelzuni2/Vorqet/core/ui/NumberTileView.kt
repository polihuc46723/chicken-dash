package com.Kelzuni2.Vorqet.core.ui

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import com.Kelzuni2.Vorqet.R
import com.Kelzuni2.Vorqet.core.config.GameConfig
import com.Kelzuni2.Vorqet.databinding.ViewNumberTileBinding
import com.Kelzuni2.Vorqet.domain.model.TileState

class NumberTileView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewNumberTileBinding =
        ViewNumberTileBinding.inflate(LayoutInflater.from(context), this)

    private var shaker: ObjectAnimator? = null
    private var currentState: TileState = TileState.IDLE
    private var currentNumber: Int = 0
    private var reducedMotion: Boolean = false

    init {
        val typed = context.obtainStyledAttributes(attrs, R.styleable.NumberTileView)
        currentNumber = typed.getInt(R.styleable.NumberTileView_appNumber, 0)
        typed.recycle()
        isClickable = true
        isFocusable = true
        binding.tileNumber.text = currentNumber.toString()
        applyState(TileState.IDLE, 0, false)
    }

    fun setReducedMotion(value: Boolean) {
        reducedMotion = value
    }

    fun setNumber(value: Int) {
        if (currentNumber == value) {
            return
        }
        currentNumber = value
        binding.tileNumber.text = value.toString()
        contentDescription = context.getString(R.string.cd_tile, value)
    }

    fun bind(value: Int, state: TileState, spriteIndex: Int, animate: Boolean) {
        setNumber(value)
        if (state != currentState || state == TileState.BLOOMED) {
            applyState(state, spriteIndex, animate)
        }
    }

    fun applyState(state: TileState, spriteIndex: Int, animate: Boolean) {
        currentState = state
        when (state) {
            TileState.IDLE -> renderIdle()
            TileState.SELECTED -> renderSelected(animate)
            TileState.BLOOMED -> renderBloomed(spriteIndex, animate)
            TileState.WRONG -> renderWrong(animate)
        }
        ViewCompat.setStateDescription(this, describe(state))
        contentDescription = context.getString(R.string.cd_tile, currentNumber)
    }

    fun shake() {
        if (reducedMotion) {
            return
        }
        shaker?.cancel()
        val step = resources.displayMetrics.density * 8f
        val animator = ObjectAnimator.ofFloat(
            this,
            View.TRANSLATION_X,
            0f,
            -step,
            step,
            -step * 0.6f,
            step * 0.6f,
            0f
        )
        animator.duration = GameConfig.SHAKE_DURATION_MS
        shaker = animator
        animator.start()
    }

    fun cancelAnimations() {
        shaker?.cancel()
        shaker = null
        animate().cancel()
        binding.tileSprite.animate().cancel()
        binding.tileNumber.animate().cancel()
        translationX = 0f
    }

    override fun onDetachedFromWindow() {
        cancelAnimations()
        super.onDetachedFromWindow()
    }

    private fun renderIdle() {
        binding.tileFace.setBackgroundResource(R.drawable.tile_face_idle)
        binding.tileNumber.alpha = 1f
        binding.tileNumber.visibility = View.VISIBLE
        binding.tileSprite.visibility = View.GONE
        binding.tileSprite.alpha = 0f
        scaleX = 1f
        scaleY = 1f
        translationX = 0f
    }

    private fun renderSelected(animate: Boolean) {
        binding.tileFace.setBackgroundResource(R.drawable.tile_face_selected)
        binding.tileNumber.alpha = 1f
        binding.tileNumber.visibility = View.VISIBLE
        binding.tileSprite.visibility = View.GONE
        if (animate && !reducedMotion) {
            animate().scaleX(1.06f).scaleY(1.06f).setDuration(120L).start()
        } else {
            scaleX = 1.06f
            scaleY = 1.06f
        }
    }

    private fun renderBloomed(spriteIndex: Int, animate: Boolean) {
        binding.tileFace.setBackgroundResource(R.drawable.tile_face_bloom)
        binding.tileSprite.setImageResource(SpriteCatalog.drawableAt(spriteIndex))
        binding.tileSprite.visibility = View.VISIBLE
        scaleX = 1f
        scaleY = 1f
        if (animate && !reducedMotion) {
            binding.tileNumber.animate()
                .alpha(0f)
                .setDuration(180L)
                .start()
            binding.tileSprite.alpha = 0f
            binding.tileSprite.scaleX = 0.6f
            binding.tileSprite.scaleY = 0.6f
            binding.tileSprite.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(GameConfig.BLOOM_DURATION_MS)
                .setInterpolator(OvershootInterpolator(1.6f))
                .start()
        } else {
            binding.tileNumber.alpha = 0f
            binding.tileSprite.alpha = 1f
            binding.tileSprite.scaleX = 1f
            binding.tileSprite.scaleY = 1f
        }
    }

    private fun renderWrong(animate: Boolean) {
        binding.tileFace.setBackgroundResource(R.drawable.tile_face_wrong)
        binding.tileNumber.alpha = 1f
        binding.tileNumber.visibility = View.VISIBLE
        binding.tileSprite.visibility = View.GONE
        scaleX = 1f
        scaleY = 1f
        if (animate) {
            shake()
        }
    }

    private fun describe(state: TileState): String = when (state) {
        TileState.SELECTED -> context.getString(R.string.a11y_tile_selected)
        TileState.BLOOMED -> context.getString(R.string.a11y_tile_bloomed)
        else -> context.getString(R.string.a11y_tile_empty)
    }
}
