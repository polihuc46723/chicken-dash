package com.Kelzuni2.Vorqet.core.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import com.Kelzuni2.Vorqet.R
import com.Kelzuni2.Vorqet.databinding.ViewGoalProgressBinding
import com.Kelzuni2.Vorqet.domain.model.DailyGoal

class GoalProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewGoalProgressBinding =
        ViewGoalProgressBinding.inflate(LayoutInflater.from(context), this)

    private var done: Int = 0
    private var goal: Int = 1

    init {
        val typed = context.obtainStyledAttributes(attrs, R.styleable.GoalProgressView)
        done = typed.getInt(R.styleable.GoalProgressView_appDone, 0)
        goal = typed.getInt(R.styleable.GoalProgressView_appGoal, 1).coerceAtLeast(1)
        typed.recycle()
        render(false)
    }

    fun bind(progress: DailyGoal, animate: Boolean) {
        done = progress.done
        goal = progress.goal.coerceAtLeast(1)
        render(animate)
    }

    fun setDelta(blooms: Int) {
        if (blooms <= 0) {
            binding.goalDelta.visibility = View.GONE
            return
        }
        binding.goalDelta.visibility = View.VISIBLE
        binding.goalDelta.text = context.getString(R.string.goal_delta, blooms)
    }

    fun cancelAnimations() {
        binding.goalBar.animate().cancel()
    }

    override fun onDetachedFromWindow() {
        cancelAnimations()
        super.onDetachedFromWindow()
    }

    private fun render(animate: Boolean) {
        val percent = ((done.toDouble() / goal) * 100).toInt().coerceIn(0, 100)
        binding.goalValue.text = context.getString(R.string.goal_value, done, goal)
        binding.goalPercent.text = context.getString(R.string.goal_percent, percent)
        if (animate) {
            binding.goalBar.setProgressCompat(percent, true)
        } else {
            binding.goalBar.progress = percent
        }
        val state = context.getString(R.string.a11y_goal_state, done, goal)
        binding.goalBar.contentDescription = state
        ViewCompat.setStateDescription(binding.goalBar, state)
    }
}
