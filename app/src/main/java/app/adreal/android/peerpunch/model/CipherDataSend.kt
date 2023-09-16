package app.adreal.android.peerpunch.model

data class CipherDataSend (
    val cipherText: String,
    val iv: String,
    val hash : String
)