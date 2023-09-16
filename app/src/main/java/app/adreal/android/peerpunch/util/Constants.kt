package app.adreal.android.peerpunch.util

class Constants {
    companion object {
        private const val CONNECTION_ESTABLISH_STRING = "#$%*#)$%*#)%#%#"
        private const val EXIT_CHAT = "EXIT_CHAT"
        private const val UDP_PORT = 50001
        private const val LOOPBACK_ADDRESS = "127.0.0.1"
        private const val CONNECTING = 0
        private const val CONNECTED = 1
        private const val DISCONNECTED = 2

        fun getConnecting(): Int {
            return CONNECTING
        }

        fun getConnected(): Int {
            return CONNECTED
        }

        fun getDisconnected(): Int {
            return DISCONNECTED
        }

        fun getConnectionEstablishString(): String {
            return CONNECTION_ESTABLISH_STRING
        }

        fun getExitChatString(): String {
            return EXIT_CHAT
        }

        fun getUdpPort(): Int {
            return UDP_PORT
        }

        fun getLoopbackAddress(): String {
            return LOOPBACK_ADDRESS
        }
    }
}