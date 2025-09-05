package com.example.myapitest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var phoneNumberEditText: EditText
    private lateinit var sendOtpButton: Button
    private lateinit var verificationCodeEditText: EditText
    private lateinit var verifyButton: Button

    private var storedVerificationId: String? = null
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Instanciar o firebase auth
        auth = FirebaseAuth.getInstance()

        // Inicializar as views
        phoneNumberEditText = findViewById(R.id.phone_number_edittext)
        sendOtpButton = findViewById(R.id.send_otp_button)
        verificationCodeEditText = findViewById(R.id.verification_code_edittext)
        verifyButton = findViewById(R.id.verify_button)

        // Set up dos ouvintes
        sendOtpButton.setOnClickListener {
            val phoneNumber = phoneNumberEditText.text.toString().trim()
            if (phoneNumber.isNotEmpty()) {
                sendVerificationCode(phoneNumber)
            } else {
                Toast.makeText(this, R.string.please_phone, Toast.LENGTH_SHORT).show()
            }
        }

        verifyButton.setOnClickListener {
            val code = verificationCodeEditText.text.toString().trim()
            if (code.isNotEmpty() && storedVerificationId != null) {
                verifyVerificationCode(code)
            } else {
                Toast.makeText(this, R.string.please_otp, Toast.LENGTH_SHORT).show()
            }
        }

        // Verificar se o usuário já está logado
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        // Usar o auth para ver se já tem o usuário logado
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Direcionar o usuário à atividade principal caso ele já esteja logado
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun sendVerificationCode(phoneNumber: String) {
        // Formatar número de telefone se necessário, adicionando +55 se já não tiver
        val formattedPhoneNumber = if (phoneNumber.startsWith("+")) phoneNumber else "+55$phoneNumber"

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(formattedPhoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Toast.makeText(this@Login, getString(R.string.verification_failed, e.message), Toast.LENGTH_SHORT).show()
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    storedVerificationId = verificationId
                    resendToken = token
                    Toast.makeText(this@Login, R.string.otp_success, Toast.LENGTH_SHORT).show()

                    // Enable verify button and code field
                    verificationCodeEditText.isEnabled = true
                    verifyButton.isEnabled = true
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyVerificationCode(code: String) {
        storedVerificationId?.let { verificationId ->
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            signInWithPhoneAuthCredential(credential)
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // Sign in failed
                    Toast.makeText(this, getString(R.string.auth_failed, task.exception?.message), Toast.LENGTH_SHORT).show()
                }
            }
    }
}