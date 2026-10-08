package com.ahmedpasha.smartfarm.balegh

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import java.util.Locale

class AndroidBaleghSpeechProvider(context: Context) : BaleghSpeechProvider {
    private val appContext = context.applicationContext
    private val recognizer: SpeechRecognizer? =
        if (SpeechRecognizer.isRecognitionAvailable(appContext)) SpeechRecognizer.createSpeechRecognizer(appContext) else null
    private val tts = TextToSpeech(appContext, null).apply {
        language = Locale("ar", "EG")
    }

    private var onResult: ((String) -> Unit)? = null
    private var onError: ((String) -> Unit)? = null

    init {
        recognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) = Unit
            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit
            override fun onPartialResults(partialResults: Bundle?) = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit
            override fun onError(error: Int) {
                onError?.invoke(errorMessage(error))
                clearCallbacks()
            }
            override fun onResults(results: Bundle?) {
                val values = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = values?.firstOrNull()?.trim().orEmpty()
                if (text.isBlank()) onError?.invoke("لم أسمع كلاماً واضحاً.")
                else onResult?.invoke(text)
                clearCallbacks()
            }
        })
    }

    override fun isAvailable(): Boolean = recognizer != null

    override fun startListening(onResult: (String) -> Unit, onError: (String) -> Unit) {
        val current = recognizer ?: run {
            onError("التعرف الصوتي غير متاح على هذا الجهاز.")
            return
        }
        this.onResult = onResult
        this.onError = onError
        current.cancel()
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-EG")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ar-EG")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        current.startListening(intent)
    }

    override fun stopListening() {
        recognizer?.stopListening()
        clearCallbacks()
    }

    override fun cancel() {
        recognizer?.cancel()
        clearCallbacks()
    }

    override fun speak(text: String) {
        tts.language = Locale("ar", "EG")
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "balegh-response")
    }

    fun release() {
        recognizer?.destroy()
        tts.stop()
        tts.shutdown()
        clearCallbacks()
    }

    private fun clearCallbacks() {
        onResult = null
        onError = null
    }

    private fun errorMessage(error: Int): String = when (error) {
        SpeechRecognizer.ERROR_AUDIO -> "تعذر الوصول إلى الميكروفون."
        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "تعذر الاتصال بخدمة التعرف الصوتي."
        SpeechRecognizer.ERROR_NO_MATCH -> "لم أجد كلاماً واضحاً."
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "انتهى وقت الاستماع قبل أن تبدأ."
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "صلاحية الميكروفون غير متاحة."
        else -> "حدث خطأ أثناء الاستماع."
    }
}
