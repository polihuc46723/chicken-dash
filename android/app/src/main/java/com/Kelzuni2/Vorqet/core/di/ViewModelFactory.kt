package com.Kelzuni2.Vorqet.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.Kelzuni2.Vorqet.presentation.game.GameViewModel
import com.Kelzuni2.Vorqet.presentation.gameover.GameOverViewModel
import com.Kelzuni2.Vorqet.presentation.menu.MenuViewModel
import com.Kelzuni2.Vorqet.presentation.splash.SplashViewModel

class ViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val created: ViewModel = when {
            modelClass.isAssignableFrom(SplashViewModel::class.java) ->
                SplashViewModel()

            modelClass.isAssignableFrom(MenuViewModel::class.java) ->
                MenuViewModel(
                    ServiceLocator.dailyProgress,
                    ServiceLocator.sessionRepository,
                    ServiceLocator.settingsRepository
                )

            modelClass.isAssignableFrom(GameViewModel::class.java) ->
                GameViewModel(
                    ServiceLocator.startSession,
                    ServiceLocator.resolvePair,
                    ServiceLocator.refillGrid,
                    ServiceLocator.finishSession,
                    ServiceLocator.settingsRepository
                )

            modelClass.isAssignableFrom(GameOverViewModel::class.java) ->
                GameOverViewModel(
                    ServiceLocator.dailyProgress,
                    ServiceLocator.settingsRepository
                )

            else -> throw IllegalArgumentException(modelClass.name)
        }
        return created as T
    }
}
