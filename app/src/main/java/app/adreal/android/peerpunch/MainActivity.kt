package app.adreal.android.peerpunch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import app.adreal.android.peerpunch.databinding.ActivityMainBinding
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

        mainActivityViewModel.intiStartupClasses(this)
    }
}