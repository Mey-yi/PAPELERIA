package com.example.papeleria

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.papeleria.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnHuella.setOnClickListener { verificarYAutenticar() }

        // Pide la huella automáticamente al abrir la app
        verificarYAutenticar()
    }

    private fun verificarYAutenticar() {
        val manager = BiometricManager.from(this)
        when (manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> mostrarPrompt()
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                binding.tvEstado.text = getString(R.string.error_sin_sensor)
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                binding.tvEstado.text = getString(R.string.error_sin_huella)
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                binding.tvEstado.text = getString(R.string.error_sensor_no_disponible)
            else ->
                binding.tvEstado.text = getString(R.string.error_sensor_no_disponible)
        }
    }

    private fun mostrarPrompt() {
        val executor = ContextCompat.getMainExecutor(this)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish() // saca el login de la pila: el botón atrás ya no regresa aquí
            }

            override fun onAuthenticationFailed() {
                binding.tvEstado.text = getString(R.string.error_huella_no_reconocida)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                binding.tvEstado.text = when (errorCode) {
                    BiometricPrompt.ERROR_USER_CANCELED,
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON -> getString(R.string.error_cancelado)
                    BiometricPrompt.ERROR_LOCKOUT,
                    BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> getString(R.string.error_bloqueo)
                    else -> errString.toString()
                }
            }
        }

        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.titulo_login))
            .setSubtitle(getString(R.string.subtitulo_login))
            .setNegativeButtonText(getString(R.string.cancelar))
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        BiometricPrompt(this, executor, callback).authenticate(info)
    }
}