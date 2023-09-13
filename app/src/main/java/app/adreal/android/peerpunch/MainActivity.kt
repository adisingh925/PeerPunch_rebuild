package app.adreal.android.peerpunch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import app.adreal.android.peerpunch.databinding.ActivityMainBinding
import app.adreal.android.peerpunch.network.SocketHandler
import app.adreal.android.peerpunch.network.UDPReceiver
import app.adreal.android.peerpunch.network.UDPStun

class MainActivity : AppCompatActivity() {

    private val binding by lazy{
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        UDPReceiver.startUDPReceiver()
        UDPStun().sendUDPBindingRequest()
        SocketHandler.initUDPSocket()
    }
}