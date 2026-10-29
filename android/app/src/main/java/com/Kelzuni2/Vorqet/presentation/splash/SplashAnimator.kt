package com.Kelzuni2.Vorqet.presentation.splash

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import com.Kelzuni2.Vorqet.databinding.FragmentSplashBinding

class SplashAnimator {

    private val animators = mutableListOf<ObjectAnimator>()

    fun play(binding: FragmentSplashBinding, reducedMotion: Boolean) {
        cancel()
        val density = binding.splashRoot.resources.displayMetrics.density
        if (reducedMotion) {
            settle(binding)
            return
        }
        introduceLogo(binding, density)
        introduceTitle(binding, density)
        driftGrid(binding, density)
        revealAccents(binding)
    }

    private fun settle(binding: FragmentSplashBinding) {
        binding.splashLogo.scaleX = 1f
        binding.splashLogo.scaleY = 1f
        binding.splashLogo.translationY = 0f
        binding.splashTitle.alpha = 1f
        binding.splashTitle.translationY = 0f
        binding.splashTagline.alpha = 1f
        binding.splashAccentTop.alpha = ACCENT_ALPHA
        binding.splashAccentRight.alpha = ACCENT_ALPHA
        binding.splashAccentBottom.alpha = ACCENT_ALPHA
    }

    private fun introduceLogo(binding: FragmentSplashBinding, density: Float) {
        val logo = binding.splashLogo
        logo.scaleX = 0.86f
        logo.scaleY = 0.86f
        logo.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(LOGO_IN_MS)
            .setInterpolator(OvershootInterpolator(1.4f))
            .withEndAction {
                try {
                    breathe(logo, density)
                } catch (e: Exception) {
                }
            }
            .start()
    }

    private fun breathe(logo: View, density: Float) {
        val travel = 6f * density
        val breathing = ObjectAnimator.ofFloat(logo, View.TRANSLATION_Y, -travel, travel)
        breathing.duration = BREATHE_MS
        breathing.repeatCount = ValueAnimator.INFINITE
        breathing.repeatMode = ValueAnimator.REVERSE
        breathing.interpolator = AccelerateDecelerateInterpolator()
        animators.add(breathing)
        breathing.start()
    }

    private fun introduceTitle(binding: FragmentSplashBinding, density: Float) {
        val title = binding.splashTitle
        title.alpha = 0f
        title.translationY = 18f * density
        title.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(TITLE_DELAY_MS)
            .setDuration(TITLE_IN_MS)
            .start()

        val tagline = binding.splashTagline
        tagline.alpha = 0f
        tagline.animate()
            .alpha(1f)
            .setStartDelay(TITLE_DELAY_MS + 160L)
            .setDuration(TITLE_IN_MS)
            .start()
    }

    private fun driftGrid(binding: FragmentSplashBinding, density: Float) {
        val travel = 16f * density
        val drift = ObjectAnimator.ofFloat(binding.splashGrid, View.TRANSLATION_X, 0f, travel)
        drift.duration = DRIFT_MS
        drift.repeatCount = ValueAnimator.INFINITE
        drift.repeatMode = ValueAnimator.REVERSE
        drift.interpolator = LinearInterpolator()
        animators.add(drift)
        drift.start()
    }

    private fun revealAccents(binding: FragmentSplashBinding) {
        fadeIn(binding.splashAccentTop, 900L)
        fadeIn(binding.splashAccentRight, 1100L)
        fadeIn(binding.splashAccentBottom, 1300L)
    }

    private fun fadeIn(target: View, duration: Long) {
        target.alpha = 0f
        target.animate()
            .alpha(ACCENT_ALPHA)
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    fun cancel() {
        animators.forEach { animator ->
            try {
                animator.cancel()
            } catch (e: Exception) {
            }
        }
        animators.clear()
    }

    fun detach(binding: FragmentSplashBinding) {
        cancel()
        binding.splashLogo.animate().cancel()
        binding.splashTitle.animate().cancel()
        binding.splashTagline.animate().cancel()
        binding.splashGrid.animate().cancel()
        binding.splashAccentTop.animate().cancel()
        binding.splashAccentRight.animate().cancel()
        binding.splashAccentBottom.animate().cancel()
    }

    companion object {
        private const val LOGO_IN_MS = 700L
        private const val BREATHE_MS = 2600L
        private const val TITLE_IN_MS = 520L
        private const val TITLE_DELAY_MS = 260L
        private const val DRIFT_MS = 9000L
        private const val ACCENT_ALPHA = 0.3f
    }
}
