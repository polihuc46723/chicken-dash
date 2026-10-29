package com.Kelzuni2.Vorqet.core.navigation

import androidx.fragment.app.FragmentManager
import com.Kelzuni2.Vorqet.R
import com.Kelzuni2.Vorqet.domain.model.SessionResult
import com.Kelzuni2.Vorqet.presentation.game.GameFragment
import com.Kelzuni2.Vorqet.presentation.gameover.GameOverFragment
import com.Kelzuni2.Vorqet.presentation.menu.MenuFragment
import com.Kelzuni2.Vorqet.presentation.splash.SplashFragment

class Navigator(
    private val fragmentManager: FragmentManager,
    private val containerId: Int
) {

    fun showSplash(): Boolean {
        if (fragmentManager.isStateSaved) {
            return false
        }
        fragmentManager.beginTransaction()
            .replace(containerId, SplashFragment(), TAG_SPLASH)
            .commit()
        return true
    }

    fun showMenu(): Boolean {
        if (fragmentManager.isStateSaved) {
            return false
        }
        fragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            .replace(containerId, MenuFragment(), TAG_MENU)
            .commit()
        return true
    }

    fun showGame(): Boolean {
        if (fragmentManager.isStateSaved) {
            return false
        }
        fragmentManager.popBackStack(TAG_RESULT, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        fragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.fade_in,
                R.anim.fade_out
            )
            .replace(containerId, GameFragment(), TAG_GAME)
            .addToBackStack(TAG_GAME)
            .commit()
        return true
    }

    fun showResult(result: SessionResult): Boolean {
        if (fragmentManager.isStateSaved) {
            return false
        }
        fragmentManager.popBackStack(TAG_GAME, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        fragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.fade_in,
                R.anim.slide_out_left,
                R.anim.fade_in,
                R.anim.fade_out
            )
            .replace(containerId, GameOverFragment.newInstance(result), TAG_RESULT)
            .addToBackStack(TAG_RESULT)
            .commit()
        return true
    }

    fun backToMenu(): Boolean {
        if (fragmentManager.isStateSaved) {
            return false
        }
        fragmentManager.popBackStack(TAG_RESULT, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        fragmentManager.popBackStack(TAG_GAME, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        return true
    }

    companion object {
        const val TAG_SPLASH = "splash"
        const val TAG_MENU = "menu"
        const val TAG_GAME = "game"
        const val TAG_RESULT = "result"
    }
}
