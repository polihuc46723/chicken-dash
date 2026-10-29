package com.Kelzuni2.Vorqet.presentation.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.core.content.ContextCompat
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
import com.Kelzuni2.Vorqet.core.ui.NumberTileView
import com.Kelzuni2.Vorqet.databinding.FragmentGameBinding
import com.Kelzuni2.Vorqet.domain.model.GardenSession
import com.Kelzuni2.Vorqet.domain.model.TileState
import com.Kelzuni2.Vorqet.presentation.dialog.PauseDialog
import java.util.Locale
import kotlinx.coroutines.launch

class GameFragment : Fragment() {

    private var _binding: FragmentGameBinding? = null

    private val viewModel: GameViewModel by viewModels { ViewModelFactory() }

    private val tiles = mutableListOf<NumberTileView>()

    private val renderedValue = IntArray(GameConfig.GRID_SIZE) { -1 }

    private val renderedState = arrayOfNulls<TileState>(GameConfig.GRID_SIZE)

    private val renderedSprite = IntArray(GameConfig.GRID_SIZE) { -1 }

    private var lastSeconds = -1

    private var lastTarget = -1

    private var headerBasePadding = 0

    private var controlsBasePadding = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = FragmentGameBinding.inflate(inflater, container, false)
        _binding = created
        return created.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = _binding ?: return
        headerBasePadding = resources.getDimensionPixelSize(R.dimen.header_padding_top)
        controlsBasePadding = bound.gameControls.paddingBottom
        applyInsets(bound)
        buildGrid(bound)
        bindActions(bound)
        listenForPause()
        observeState()
        observeFinish()
        viewModel.start()
    }

    private fun applyInsets(bound: FragmentGameBinding) {
        ViewCompat.setOnApplyWindowInsetsListener(bound.gameRoot) { _, insets ->
            try {
                val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                val current = _binding
                if (current != null) {
                    current.gameHeader.setPadding(
                        current.gameHeader.paddingLeft,
                        maxOf(bars.top, headerBasePadding),
                        current.gameHeader.paddingRight,
                        current.gameHeader.paddingBottom
                    )
                    current.gameControls.setPadding(
                        current.gameControls.paddingLeft,
                        current.gameControls.paddingTop,
                        current.gameControls.paddingRight,
                        controlsBasePadding + bars.bottom
                    )
                }
            } catch (e: Exception) {
            }
            insets
        }
    }

    private fun buildGrid(bound: FragmentGameBinding) {
        val size = resources.getDimensionPixelSize(R.dimen.tile_size)
        val margin = resources.getDimensionPixelSize(R.dimen.tile_margin)
        bound.gameGrid.removeAllViews()
        tiles.clear()
        for (index in 0 until GameConfig.GRID_SIZE) {
            val tile = NumberTileView(requireContext())
            val params = GridLayout.LayoutParams()
            params.width = size
            params.height = size
            params.rowSpec = GridLayout.spec(index / GameConfig.GRID_COLS)
            params.columnSpec = GridLayout.spec(index % GameConfig.GRID_COLS)
            params.setMargins(margin, margin, margin, margin)
            tile.layoutParams = params
            tile.setOnClickListener {
                try {
                    viewModel.onTileTapped(index)
                } catch (e: Exception) {
                }
            }
            bound.gameGrid.addView(tile)
            tiles.add(tile)
        }
    }

    private fun bindActions(bound: FragmentGameBinding) {
        bound.gamePause.setOnClickListener {
            viewModel.pause()
            showPauseDialog()
        }
        bound.gameReshuffle.setOnClickListener {
            viewModel.reshuffle()
        }
    }

    private fun showPauseDialog() {
        try {
            if (!isAdded || parentFragmentManager.isStateSaved) {
                return
            }
            if (parentFragmentManager.findFragmentByTag(PauseDialog.TAG) != null) {
                return
            }
            PauseDialog().show(parentFragmentManager, PauseDialog.TAG)
        } catch (e: Exception) {
        }
    }

    private fun listenForPause() {
        parentFragmentManager.setFragmentResultListener(
            PauseDialog.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, result ->
            try {
                val quit = result.getBoolean(PauseDialog.KEY_QUIT, false)
                if (quit) {
                    val host = activity as? MainActivity ?: return@setFragmentResultListener
                    host.navigator.backToMenu()
                } else {
                    viewModel.resume()
                }
            } catch (e: Exception) {
            }
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    render(state)
                }
            }
        }
    }

    private fun observeFinish() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.finishEvent.collect { result ->
                    if (result == null || !isAdded) {
                        return@collect
                    }
                    val host = activity as? MainActivity ?: return@collect
                    if (!host.navigator.showResult(result)) {
                        return@collect
                    }
                    viewModel.consumeFinish()
                }
            }
        }
    }

    private fun render(state: GameUiState) {
        val bound = _binding ?: return
        val session = state.session ?: return
        renderHeader(session, state)
        renderTiles(session, state)
        renderControls(session)
        bound.gameBoard.alpha =
            if (state.phase == GameUiState.Phase.LOADING) LOADING_ALPHA else 1f
    }

    private fun renderHeader(session: GardenSession, state: GameUiState) {
        val bound = _binding ?: return
        if (lastTarget != session.targetSum) {
            lastTarget = session.targetSum
            bound.gameTargetValue.text = session.targetSum.toString()
            bound.gameTargetBlock.contentDescription =
                getString(R.string.a11y_target, session.targetSum)
        }
        val seconds = session.secondsLeft
        if (seconds == lastSeconds) {
            return
        }
        val warning = seconds <= GameConfig.WARNING_SECONDS
        bound.gameTimer.setValue(getString(R.string.game_seconds, seconds))
        bound.gameTimer.setAccent(
            ContextCompat.getColor(
                requireContext(),
                if (warning) R.color.state_coral else R.color.primary_blue
            )
        )
        if (warning && !state.reducedMotion) {
            bound.gameTimer.pulse()
        }
        lastSeconds = seconds
    }

    private fun renderTiles(session: GardenSession, state: GameUiState) {
        session.tiles.forEach { tile ->
            val view = tiles.getOrNull(tile.index) ?: return@forEach
            view.setReducedMotion(state.reducedMotion)
            val changed = renderedValue[tile.index] != tile.value ||
                renderedState[tile.index] != tile.state ||
                renderedSprite[tile.index] != tile.spriteIndex
            if (!changed) {
                return@forEach
            }
            val animate = renderedState[tile.index] != null && !state.reducedMotion
            view.bind(tile.value, tile.state, tile.spriteIndex, animate)
            renderedValue[tile.index] = tile.value
            renderedState[tile.index] = tile.state
            renderedSprite[tile.index] = tile.spriteIndex
        }
    }

    private fun renderControls(session: GardenSession) {
        val bound = _binding ?: return
        bound.gameCombo.text = getString(
            R.string.game_combo,
            String.format(Locale.US, "%.1f", session.combo)
        )
        bound.gameTimeBar.progress = session.timePercent
        bound.gameStatBlooms.setValue(session.blooms.toString())
        bound.gameStatPair.setValue(session.bestPairPoints.toString())
        bound.gameStatAccuracy.setValue(String.format(Locale.US, "%d%%", session.accuracy))
        val visibleBlooms = session.blooms > 0
        val visiblePair = session.bestPairPoints > 0
        val visibleAccuracy = session.accuracy > 0
        bound.gameStatBlooms.showIf(visibleBlooms)
        bound.gameStatPair.showIf(visiblePair)
        bound.gameStatAccuracy.showIf(visibleAccuracy)
        var shown = 0
        if (visibleBlooms) {
            shown++
        }
        if (visiblePair) {
            shown++
        }
        if (visibleAccuracy) {
            shown++
        }
        bound.gameStatRow.visibility = if (shown >= 2) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        val bound = _binding
        tiles.forEach { tile ->
            tile.setOnClickListener(null)
            tile.cancelAnimations()
        }
        tiles.clear()
        if (bound != null) {
            bound.gameTimer.cancelAnimations()
            bound.gameStatBlooms.cancelAnimations()
            bound.gameStatPair.cancelAnimations()
            bound.gameStatAccuracy.cancelAnimations()
            bound.gameBoard.animate().cancel()
            bound.gameGrid.removeAllViews()
            ViewCompat.setOnApplyWindowInsetsListener(bound.gameRoot, null)
        }
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val LOADING_ALPHA = 0.6f
    }
}
