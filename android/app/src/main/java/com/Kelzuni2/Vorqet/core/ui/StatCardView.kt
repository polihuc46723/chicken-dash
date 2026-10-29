package com.Kelzuni2.Vorqet.core.ui

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.Kelzuni2.Vorqet.R
import com.Kelzuni2.Vorqet.databinding.ViewStatCardBinding

class StatCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewStatCardBinding =
        ViewStatCardBinding.inflate(LayoutInflater.from(context), this)

    private var accentColor: Int = ContextCompat.getColor(context, R.color.primary_blue)
    private var labelText: String = ""
    private var counter: ValueAnimator? = null

    init {
        val typed = context.obtainStyledAttributes(attrs, R.styleable.StatCardView)
        accentColor = typed.getColor(
            R.styleable.StatCardView_appAccent,
            ContextCompat.getColor(context, R.color.primary_blue)
        )
        labelText = typed.getString(R.styleable.StatCardView_appLabel).orEmpty()
        val value = typed.getString(R.styleable.StatCardView_appValue).orEmpty()
        val compact = typed.getBoolean(R.styleable.StatCardView_appCompact, false)
        typed.recycle()

        binding.statLabel.text = labelText
        binding.statValue.text = value
        applyAccent(accentColor)
        if (compact) {
            applyCompact()
        }
        refreshDescription()
    }

    fun setAccent(color: Int) {
        accentColor = color
        applyAccent(color)
    }

    fun setLabel(text: String) {
        labelText = text
        binding.statLabel.text = text
        refreshDescription()
    }

    fun setValue(text: String) {
        counter?.cancel()
        counter = null
        binding.statValue.text = text
        refreshDescription()
    }

    fun setValue(value: Int, animate: Boolean) {
        counter?.cancel()
        counter = null
        if (!animate) {
            binding.statValue.text = value.toString()
            refreshDescription()
            return
        }
        val animator = ValueAnimator.ofInt(0, value)
        animator.duration = COUNT_UP_MS
        animator.addUpdateListener { frame ->
            try {
                binding.statValue.text = frame.animatedValue.toString()
            } catch (e: Exception) {
            }
        }
        counter = animator
        animator.start()
        refreshDescription()
    }

    fun showIf(visible: Boolean) {
        visibility = if (visible) View.VISIBLE else View.GONE
    }

    fun pulse() {
        binding.statValue.animate().cancel()
        binding.statValue.scaleX = 1f
        binding.statValue.scaleY = 1f
        binding.statValue.animate()
            .scaleX(1.08f)
            .scaleY(1.08f)
            .setDuration(140L)
            .withEndAction {
                try {
                    binding.statValue.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(140L)
                        .start()
                } catch (e: Exception) {
                }
            }
            .start()
    }

    fun cancelAnimations() {
        counter?.cancel()
        counter = null
        binding.statValue.animate().cancel()
    }

    override fun onDetachedFromWindow() {
        cancelAnimations()
        super.onDetachedFromWindow()
    }

    private fun applyAccent(color: Int) {
        binding.statValue.setTextColor(color)
        binding.statDot.backgroundTintList = ColorStateList.valueOf(color)
        binding.statCard.setStrokeColor((color and 0x00FFFFFF) or ALPHA_STROKE)
    }

    private fun applyCompact() {
        binding.statValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        binding.statLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9f)
    }

    private fun refreshDescription() {
        val description = context.getString(
            R.string.a11y_stat,
            binding.statValue.text.toString(),
            labelText
        )
        contentDescription = description
        ViewCompat.setStateDescription(this, binding.statValue.text.toString())
    }

    companion object {
        private const val COUNT_UP_MS = 420L
        private const val ALPHA_STROKE = 0x55000000
    }
}
