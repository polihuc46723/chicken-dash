package com.Kelzuni2.Vorqet.presentation.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.Kelzuni2.Vorqet.MainActivity
import com.Kelzuni2.Vorqet.core.di.ServiceLocator
import com.Kelzuni2.Vorqet.core.di.ViewModelFactory
import com.Kelzuni2.Vorqet.databinding.FragmentSplashBinding
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null

    private val viewModel: SplashViewModel by viewModels { ViewModelFactory() }

    private val animator = SplashAnimator()

    private var startRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = FragmentSplashBinding.inflate(inflater, container, false)
        _binding = created
        return created.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = _binding ?: return
        applyInsets(bound)
        val reducedMotion = readReducedMotion()
        val runnable = Runnable {
            try {
                if (!isAdded) {
                    return@Runnable
                }
                val current = _binding ?: return@Runnable
                animator.play(current, reducedMotion)
            } catch (e: Exception) {
            }
        }
        startRunnable = runnable
        bound.splashRoot.post(runnable)
        observeHandOff()
    }

    private fun readReducedMotion(): Boolean = try {
        if (ServiceLocator.isReady) {
            ServiceLocator.settingsRepository.load().reducedMotion
        } else {
            false
        }
    } catch (e: Exception) {
        false
    }

    private fun applyInsets(bound: FragmentSplashBinding) {
        ViewCompat.setOnApplyWindowInsetsListener(bound.splashRoot) { target, insets ->
            try {
                val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                target.setPadding(0, bars.top, 0, bars.bottom)
            } catch (e: Exception) {
            }
            insets
        }
    }

    private fun observeHandOff() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.handOffReady.collect { ready ->
                    if (ready) {
                        handOff()
                    }
                }
            }
        }
    }

    private fun handOff() {
        try {
            if (!isAdded) {
                return
            }
            val host = activity as? MainActivity ?: return
            if (!host.navigator.showMenu()) {
                return
            }
            viewModel.consumeHandOff()
        } catch (e: Exception) {
        }
    }

    override fun onDestroyView() {
        val bound = _binding
        if (bound != null) {
            val pending = startRunnable
            if (pending != null) {
                bound.splashRoot.removeCallbacks(pending)
            }
            animator.detach(bound)
            ViewCompat.setOnApplyWindowInsetsListener(bound.splashRoot, null)
        }
        startRunnable = null
        animator.cancel()
        _binding = null
        super.onDestroyView()
    }
}
