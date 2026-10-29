package com.Kelzuni2.Vorqet.presentation.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.Kelzuni2.Vorqet.R
import com.Kelzuni2.Vorqet.core.config.GameConfig
import com.Kelzuni2.Vorqet.core.di.ServiceLocator
import com.Kelzuni2.Vorqet.databinding.DialogSettingsBinding
import com.Kelzuni2.Vorqet.domain.model.GardenSettings

class SettingsDialog : DialogFragment() {

    private var _binding: DialogSettingsBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_ChickenDash_Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = DialogSettingsBinding.inflate(inflater, container, false)
        _binding = created
        return created.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = _binding ?: return
        applyCurrent(bound, loadSettings())
        bound.settingsTargetGroup.addOnButtonCheckedListener { _, _, _ ->
            describeGroups()
        }
        bound.settingsLengthGroup.addOnButtonCheckedListener { _, _, _ ->
            describeGroups()
        }
        bound.settingsGoalGroup.addOnButtonCheckedListener { _, _, _ ->
            describeGroups()
        }
        bound.settingsSave.setOnClickListener {
            persist()
        }
        describeGroups()
    }

    override fun onStart() {
        super.onStart()
        try {
            dialog?.window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        } catch (e: Exception) {
        }
    }

    private fun loadSettings(): GardenSettings = try {
        if (ServiceLocator.isReady) {
            ServiceLocator.settingsRepository.load()
        } else {
            fallback()
        }
    } catch (e: Exception) {
        fallback()
    }

    private fun fallback(): GardenSettings = GardenSettings(
        targetSum = GameConfig.DEFAULT_TARGET,
        sessionSeconds = GameConfig.DEFAULT_SESSION_SECONDS,
        dailyGoal = GameConfig.DEFAULT_DAILY_GOAL,
        reducedMotion = false,
        gardenerName = ""
    )

    private fun applyCurrent(bound: DialogSettingsBinding, settings: GardenSettings) {
        bound.settingsTargetGroup.check(
            when (settings.targetSum) {
                TARGET_MID -> R.id.settings_target_12
                TARGET_HIGH -> R.id.settings_target_15
                else -> R.id.settings_target_10
            }
        )
        bound.settingsLengthGroup.check(
            when (settings.sessionSeconds) {
                LENGTH_SHORT -> R.id.settings_len_45
                LENGTH_LONG -> R.id.settings_len_120
                else -> R.id.settings_len_75
            }
        )
        bound.settingsGoalGroup.check(
            when (settings.dailyGoal) {
                GOAL_SMALL -> R.id.settings_goal_8
                GOAL_LARGE -> R.id.settings_goal_20
                else -> R.id.settings_goal_12
            }
        )
        bound.settingsMotion.isChecked = settings.reducedMotion
        bound.settingsName.setText(settings.gardenerName)
    }

    private fun describeGroups() {
        val bound = _binding ?: return
        try {
            ViewCompat.setStateDescription(
                bound.settingsTargetGroup,
                getString(R.string.a11y_toggle_state, readTarget().toString())
            )
            ViewCompat.setStateDescription(
                bound.settingsLengthGroup,
                getString(R.string.a11y_toggle_state, readLength().toString())
            )
            ViewCompat.setStateDescription(
                bound.settingsGoalGroup,
                getString(R.string.a11y_toggle_state, readGoal().toString())
            )
        } catch (e: Exception) {
        }
    }

    private fun readTarget(): Int {
        val bound = _binding ?: return GameConfig.DEFAULT_TARGET
        return when (bound.settingsTargetGroup.checkedButtonId) {
            R.id.settings_target_12 -> TARGET_MID
            R.id.settings_target_15 -> TARGET_HIGH
            else -> GameConfig.DEFAULT_TARGET
        }
    }

    private fun readLength(): Int {
        val bound = _binding ?: return GameConfig.DEFAULT_SESSION_SECONDS
        return when (bound.settingsLengthGroup.checkedButtonId) {
            R.id.settings_len_45 -> LENGTH_SHORT
            R.id.settings_len_120 -> LENGTH_LONG
            else -> GameConfig.DEFAULT_SESSION_SECONDS
        }
    }

    private fun readGoal(): Int {
        val bound = _binding ?: return GameConfig.DEFAULT_DAILY_GOAL
        return when (bound.settingsGoalGroup.checkedButtonId) {
            R.id.settings_goal_8 -> GOAL_SMALL
            R.id.settings_goal_20 -> GOAL_LARGE
            else -> GameConfig.DEFAULT_DAILY_GOAL
        }
    }

    private fun persist() {
        val bound = _binding ?: return
        try {
            val updated = GardenSettings(
                targetSum = readTarget(),
                sessionSeconds = readLength(),
                dailyGoal = readGoal(),
                reducedMotion = bound.settingsMotion.isChecked,
                gardenerName = bound.settingsName.text.toString().trim()
            )
            if (ServiceLocator.isReady) {
                ServiceLocator.saveSettings.invoke(updated)
            }
            if (isAdded && !parentFragmentManager.isStateSaved) {
                setFragmentResult(REQUEST_KEY, Bundle())
            }
        } catch (e: Exception) {
        }
        try {
            dismissAllowingStateLoss()
        } catch (e: Exception) {
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "settings_dialog"
        const val REQUEST_KEY = "settings_request"
        private const val TARGET_MID = 12
        private const val TARGET_HIGH = 15
        private const val LENGTH_SHORT = 45
        private const val LENGTH_LONG = 120
        private const val GOAL_SMALL = 8
        private const val GOAL_LARGE = 20
    }
}
