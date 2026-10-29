package com.Kelzuni2.Vorqet.presentation.gameover

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.Kelzuni2.Vorqet.MainActivity
import com.Kelzuni2.Vorqet.R
import com.Kelzuni2.Vorqet.core.config.GameConfig
import com.Kelzuni2.Vorqet.core.di.ViewModelFactory
import com.Kelzuni2.Vorqet.databinding.FragmentGameOverBinding
import com.Kelzuni2.Vorqet.domain.model.SessionResult
import java.util.Locale
import kotlinx.coroutines.launch

class GameOverFragment : Fragment() {

    private var _binding: FragmentGameOverBinding? = null

    private val viewModel: GameOverViewModel by viewModels { ViewModelFactory() }

    private var scoreAnimator: ValueAnimator? = null

    private var basePaddingBottom = 0

    private var headerBasePadding = 0

    private var entranceDone = false

    private val result: SessionResult by lazy { readResult() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = FragmentGameOverBinding.inflate(inflater, container, false)
        _binding = created
        return created.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = _binding ?: return
        basePaddingBottom = bound.resultRoot.paddingBottom
        headerBasePadding = resources.getDimensionPixelSize(R.dimen.header_padding_top)
        applyInsets(bound)
        bindActions(bound)
        renderStats(bound)
        observeGoal()
        observeSettings()
    }

    private fun readResult(): SessionResult {
        val args = arguments
        if (args == null) {
            return SessionResult(0, 0, 1.0, 0, 0, false, "")
        }
        return SessionResult(
            score = args.getInt(ARG_SCORE, 0),
            blooms = args.getInt(ARG_BLOOMS, 0),
            bestCombo = args.getDouble(ARG_COMBO, 1.0),
            bestPair = args.getInt(ARG_PAIR, 0),
            accuracy = args.getInt(ARG_ACCURACY, 0),
            isNewBest = args.getBoolean(ARG_NEW_BEST, false),
            plantName = args.getString(ARG_PLANT).orEmpty()
        )
    }

    private fun applyInsets(bound: FragmentGameOverBinding) {
        ViewCompat.setOnApplyWindowInsetsListener(bound.resultRoot) { target, insets ->
            try {
                val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                val current = _binding
                if (current != null) {
                    current.resultHeader.setPadding(
                        current.resultHeader.paddingLeft,
                        maxOf(bars.top, headerBasePadding),
                        current.resultHeader.paddingRight,
                        current.resultHeader.paddingBottom
                    )
                }
                target.setPadding(
                    target.paddingLeft,
                    target.paddingTop,
                    target.paddingRight,
                    basePaddingBottom + bars.bottom
                )
            } catch (e: Exception) {
            }
            insets
        }
    }

    private fun bindActions(bound: FragmentGameOverBinding) {
        bound.resultReplay.setOnClickListener {
            val host = activity as? MainActivity ?: return@setOnClickListener
            host.navigator.showGame()
        }
        bound.resultMenu.setOnClickListener {
            val host = activity as? MainActivity ?: return@setOnClickListener
            host.navigator.backToMenu()
        }
    }

    private fun renderStats(bound: FragmentGameOverBinding) {
        bound.resultScore.text = "0"
        bound.resultStatBlooms.setValue(result.blooms.toString())
        bound.resultStatCombo.setValue(
            String.format(Locale.US, "x%.1f", result.bestCombo)
        )
        bound.resultStatAccuracy.setValue(
            String.format(Locale.US, "%d%%", result.accuracy)
        )
        val visibleBlooms = result.blooms > 0
        val visibleCombo = result.bestCombo > 1.0
        val visibleAccuracy = result.accuracy > 0
        bound.resultStatBlooms.showIf(visibleBlooms)
        bound.resultStatCombo.showIf(visibleCombo)
        bound.resultStatAccuracy.showIf(visibleAccuracy)
        var shown = 0
        if (visibleBlooms) {
            shown++
        }
        if (visibleCombo) {
            shown++
        }
        if (visibleAccuracy) {
            shown++
        }
        bound.resultStatRow.visibility = if (shown >= 2) View.VISIBLE else View.GONE
    }

    private fun observeGoal() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.goal.collect { goal ->
                    val current = _binding ?: return@collect
                    current.resultGoal.bind(goal, true)
                    current.resultGoal.setDelta(result.blooms)
                }
            }
        }
    }

    private fun observeSettings() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.settings.collect { settings ->
                    val current = _binding ?: return@collect
                    playEntrance(current, settings.reducedMotion)
                }
            }
        }
    }

    private fun playEntrance(bound: FragmentGameOverBinding, reducedMotion: Boolean) {
        if (entranceDone) {
            return
        }
        entranceDone = true
        if (reducedMotion) {
            bound.resultSpriteA.alpha = 1f
            bound.resultSpriteB.alpha = 1f
            bound.resultSpriteC.alpha = 1f
            bound.resultScore.text = result.score.toString()
            return
        }
        bound.resultHero.scaleX = 0.9f
        bound.resultHero.scaleY = 0.9f
        bound.resultHero.alpha = 0f
        bound.resultHero.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(HERO_IN_MS)
            .setInterpolator(OvershootInterpolator(1.1f))
            .start()
        fadeSprite(bound.resultSpriteA, 0L)
        fadeSprite(bound.resultSpriteB, 120L)
        fadeSprite(bound.resultSpriteC, 240L)
        countUp(bound)
    }

    private fun fadeSprite(target: View, delay: Long) {
        target.alpha = 0f
        target.animate()
            .alpha(1f)
            .setStartDelay(delay)
            .setDuration(SPRITE_IN_MS)
            .start()
    }

    private fun countUp(bound: FragmentGameOverBinding) {
        scoreAnimator?.cancel()
        if (result.score <= 0) {
            bound.resultScore.text = "0"
            return
        }
        val animator = ValueAnimator.ofInt(0, result.score)
        animator.duration = GameConfig.COUNT_UP_DURATION_MS
        animator.addUpdateListener { frame ->
            try {
                val current = _binding ?: return@addUpdateListener
                current.resultScore.text = frame.animatedValue.toString()
            } catch (e: Exception) {
            }
        }
        scoreAnimator = animator
        animator.start()
    }

    override fun onDestroyView() {
        val bound = _binding
        scoreAnimator?.cancel()
        scoreAnimator = null
        if (bound != null) {
            bound.resultHero.animate().cancel()
            bound.resultSpriteA.animate().cancel()
            bound.resultSpriteB.animate().cancel()
            bound.resultSpriteC.animate().cancel()
            bound.resultStatBlooms.cancelAnimations()
            bound.resultStatCombo.cancelAnimations()
            bound.resultStatAccuracy.cancelAnimations()
            bound.resultGoal.cancelAnimations()
            ViewCompat.setOnApplyWindowInsetsListener(bound.resultRoot, null)
        }
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val ARG_SCORE = "arg_score"
        private const val ARG_BLOOMS = "arg_blooms"
        private const val ARG_COMBO = "arg_combo"
        private const val ARG_PAIR = "arg_pair"
        private const val ARG_ACCURACY = "arg_accuracy"
        private const val ARG_NEW_BEST = "arg_new_best"
        private const val ARG_PLANT = "arg_plant"
        private const val HERO_IN_MS = 360L
        private const val SPRITE_IN_MS = 320L

        fun newInstance(result: SessionResult): GameOverFragment {
            val fragment = GameOverFragment()
            val args = Bundle()
            args.putInt(ARG_SCORE, result.score)
            args.putInt(ARG_BLOOMS, result.blooms)
            args.putDouble(ARG_COMBO, result.bestCombo)
            args.putInt(ARG_PAIR, result.bestPair)
            args.putInt(ARG_ACCURACY, result.accuracy)
            args.putBoolean(ARG_NEW_BEST, result.isNewBest)
            args.putString(ARG_PLANT, result.plantName)
            fragment.arguments = args
            return fragment
        }
    }
}
