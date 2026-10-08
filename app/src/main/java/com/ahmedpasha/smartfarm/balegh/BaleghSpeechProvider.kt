package com.ahmedpasha.smartfarm.balegh

interface BaleghSpeechProvider {
    fun isAvailable(): Boolean
    fun startListening(onResult: (String) -> Unit, onError: (String) -> Unit)
    fun stopListening()
    fun cancel()
    fun speak(text: String)
}

class UnconfiguredSpeechProvider : BaleghSpeechProvider {
    override fun isAvailable() = false
    override fun startListening(onResult: (String) -> Unit, onError: (String) -> Unit) {
        onError("التعرف الصوتي غير مفعّل.")
    }
    override fun stopListening() = Unit
    override fun cancel() = Unit
    override fun speak(text: String) = Unit
}
