package com.ahmedpasha.smartfarm.balegh
interface BaleghSpeechProvider { fun isAvailable(): Boolean; fun startListening(onResult: (String) -> Unit, onError: (String) -> Unit); fun stopListening(); fun cancel() }
class UnconfiguredSpeechProvider : BaleghSpeechProvider {
    override fun isAvailable() = false
    override fun startListening(onResult: (String) -> Unit, onError: (String) -> Unit) { onError("التعرف الصوتي غير مفعّل في هذه النسخة.") }
    override fun stopListening() {}
    override fun cancel() {}
}
