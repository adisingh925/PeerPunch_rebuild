package app.adreal.android.peerpunch.network

import android.util.Log
import androidx.lifecycle.MutableLiveData
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Collections
import java.util.Locale


class IPHandler {

    companion object{
        val publicIP = MutableLiveData<String>()
        val publicPort = MutableLiveData<Int>()
        val privateIP = MutableLiveData<String>()
        val receiverIP = MutableLiveData<String>()

        fun getIPAddress(useIPv4: Boolean = true): String {
            try {
                val interfaces: List<NetworkInterface> = Collections.list(NetworkInterface.getNetworkInterfaces())
                for (values in interfaces) {
                    val addressList: List<InetAddress> = Collections.list(values.inetAddresses)
                    for (address in addressList) {
                        if (!address.isLoopbackAddress) {
                            val actualAddress: String = address.hostAddress as String
                            val isIPv4 = actualAddress.indexOf(':') < 0
                            if (useIPv4) {
                                if (isIPv4) return actualAddress
                            } else {
                                if (!isIPv4) {
                                    val delimiter = actualAddress.indexOf('%') // drop ip6 zone suffix
                                    return if (delimiter < 0) actualAddress.uppercase(Locale.getDefault()) else actualAddress.substring(
                                        0,
                                        delimiter
                                    ).uppercase(
                                        Locale.getDefault()
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d("IP address exception", e.message.toString())
            }
            return ""
        }
    }
}