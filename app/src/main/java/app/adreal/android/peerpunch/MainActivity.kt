package app.adreal.android.peerpunch

import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import app.adreal.android.peerpunch.databinding.ActivityMainBinding
import app.adreal.android.peerpunch.storage.SharedPreferences
import app.adreal.android.peerpunch.viewmodel.MainActivityViewModel


class MainActivity : AppCompatActivity() {

    private val binding by lazy{
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mainActivityViewModel by lazy{
        ViewModelProvider(this)[MainActivityViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        SharedPreferences.init(this)
        mainActivityViewModel.intiStartupClasses(this)
        mainActivityViewModel.generateKeyPair()
    }

    fun updateStatusBarColor(color: String?, navigationBarColor : String) {
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.parseColor(color)
        window.navigationBarColor = Color.parseColor(navigationBarColor)
    }
}