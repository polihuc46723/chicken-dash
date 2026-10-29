package com.Kelzuni2.Vorqet.presentation.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.Kelzuni2.Vorqet.MainActivity
import com.Kelzuni2.Vorqet.R
import com.Kelzuni2.Vorqet.core.di.ViewModelFactory
import com.Kelzuni2.Vorqet.databinding.FragmentMenuBinding
import com.Kelzuni2.Vorqet.presentation.dialog.HistoryDialog
import com.Kelzuni2.Vorqet.presentation.dialog.RulesDialog
import com.Kelzuni2.Vorqet.presentation.dialog.SettingsDialog
import kotlinx.coroutines.launch

class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null

    private val viewModel: MenuViewModel by viewModels { ViewModelFactory() }

    private var basePaddingBottom = 0

    private var entranceDone = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = FragmentMenuBinding.inflate(inflater, container, false)
        _binding = created
        return created.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = _binding ?: return
        basePaddingBottom = bound.menuRoot.paddingBottom
        bound.menuStatRow.visibility = View.GONE
        applyInsets(bound)
        bindActions(bound)
        listenForSettings()
        observeState()
    }

    override fun onResume() {
        super.onResume()
        try {
            viewModel.refresh()
        } catch (e: Exception) {
        }
    }

    private fun applyInsets(bound: FragmentMenuBinding) {
        val headerBase = resources.getDimensionPixelSize(R.dimen.header_padding_top)
        ViewCompat.setOnApplyWindowInsetsListener(bound.menuRoot) { target, insets ->
            try {
                val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                val current = _binding
                if (current != null) {
                    current.menuHeader.setPadding(
                        current.menuHeader.paddingLeft,
                        maxOf(bars.top, headerBase),
                        current.menuHeader.paddingRight,
                        current.menuHeader.paddingBottom
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

    private fun bindActions(bound: FragmentMenuBinding) {
        bound.menuPlay.setOnClickListener {
            val host = activity as? MainActivity ?: return@setOnClickListener
            host.navigator.showGame()
        }
        bound.menuChipHistory.setOnClickListener {
            showDialog(HistoryDialog(), HistoryDialog.TAG)
        }
        bound.menuChipSettings.setOnClickListener {
            showDialog(SettingsDialog(), SettingsDialog.TAG)
        }
        bound.menuChipRules.setOnClickListener {
            showDialog(RulesDialog(), RulesDialog.TAG)
        }
    }

    private fun showDialog(dialog: DialogFragment, tag: String) {
        try {
            if (!isAdded || parentFragmentManager.isStateSaved) {
                return
            }
            if (parentFragmentManager.findFragmentByTag(tag) != null) {
                return
            }
            dialog.show(parentFragmentManager, tag)
        } catch (e: Exception) {
        }
    }

    private fun listenForSettings() {
        parentFragmentManager.setFragmentResultListener(
            SettingsDialog.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, _ ->
            try {
                viewModel.refresh()
            } catch (e: Exception) {
            }
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.goal.collect { goal ->
                    val current = _binding ?: return@collect
                    current.menuGoal.bind(goal, true)
                    current.menuStreak.setValue(goal.streakDays, false)
                    current.menuStreak.showIf(goal.streakDays > 0)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.bestScore.collect { best ->
                    val current = _binding ?: return@collect
                    current.menuStatBest.setValue(best, false)
                    refreshStatRow(current)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.totalBlooms.collect { blooms ->
                    val current = _binding ?: return@collect
                    current.menuStatBlooms.setValue(blooms, false)
                    refreshStatRow(current)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.settings.collect { settings ->
                    val current = _binding ?: return@collect
                    current.menuGreeting.text = if (settings.gardenerName.isBlank()) {
                        getString(R.string.menu_greeting)
                    } else {
                        getString(R.string.menu_greeting_named, settings.gardenerName)
                    }
                    playEntrance(current, settings.reducedMotion)
                }
            }
        }
    }

    private fun refreshStatRow(bound: FragmentMenuBinding) {
        val best = viewModel.bestScore.value
        val blooms = viewModel.totalBlooms.value
        bound.menuStatBest.showIf(best > 0)
        bound.menuStatBlooms.showIf(blooms > 0)
        val visible = best > 0 && blooms > 0
        bound.menuStatRow.visibility = if (visible) View.VISIBLE else View.GONE
        if (visible) {
            bound.menuStatRow.alpha = 1f
            bound.menuStatRow.translationY = 0f
        }
    }

    private fun playEntrance(bound: FragmentMenuBinding, reducedMotion: Boolean) {
        if (entranceDone) {
            return
        }
        entranceDone = true
        if (reducedMotion) {
            return
        }
        val density = resources.displayMetrics.density
        bound.menuHeader.alpha = 0f
        bound.menuHeader.animate().alpha(1f).setDuration(240L).start()
        slideIn(bound.menuGoal, density, 0L)
        slideIn(bound.menuGreetingBlock, density, 60L)
        bound.menuPlay.scaleX = 0.94f
        bound.menuPlay.scaleY = 0.94f
        bound.menuPlay.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(300L)
            .setInterpolator(OvershootInterpolator(1.2f))
            .start()
    }

    private fun slideIn(target: View, density: Float, delay: Long) {
        target.alpha = 0f
        target.translationY = 24f * density
        target.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(delay)
            .setDuration(320L)
            .start()
    }

    override fun onDestroyView() {
        val bound = _binding
        if (bound != null) {
            bound.menuHeader.animate().cancel()
            bound.menuGoal.animate().cancel()
            bound.menuGreetingBlock.animate().cancel()
            bound.menuPlay.animate().cancel()
            bound.menuStreak.cancelAnimations()
            bound.menuStatBest.cancelAnimations()
            bound.menuStatBlooms.cancelAnimations()
            bound.menuGoal.cancelAnimations()
            ViewCompat.setOnApplyWindowInsetsListener(bound.menuRoot, null)
        }
        _binding = null
        super.onDestroyView()
    }
}
