package com.Kelzuni2.Vorqet

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.Kelzuni2.Vorqet.core.di.ServiceLocator
import com.Kelzuni2.Vorqet.core.navigation.Navigator
import com.Kelzuni2.Vorqet.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    lateinit var navigator: Navigator
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ServiceLocator.init(applicationContext)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navigator = Navigator(supportFragmentManager, R.id.fragment_container)
        if (savedInstanceState == null) {
            navigator.showSplash()
        }
    }
}
