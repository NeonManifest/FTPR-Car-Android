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

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize views
        phoneNumberEditText = findViewById(R.id.phone_number_edittext)
        sendOtpButton = findViewById(R.id.send_otp_button)
        verificationCodeEditText = findViewById(R.id.verification_code_edittext)
        verifyButton = findViewById(R.id.verify_button)

        // Set up click listeners
        sendOtpButton.setOnClickListener {
            val phoneNumber = phoneNumberEditText.text.toString().trim()
            if (phoneNumber.isNotEmpty()) {
                sendVerificationCode(phoneNumber)
            } else {
                Toast.makeText(this, "Please enter phone number", Toast.LENGTH_SHORT).show()
            }
        }

        verifyButton.setOnClickListener {
            val code = verificationCodeEditText.text.toString().trim()
            if (code.isNotEmpty() && storedVerificationId != null) {
                verifyVerificationCode(code)
            } else {
                Toast.makeText(this, "Please enter verification code", Toast.LENGTH_SHORT).show()
            }
        }

        // Check if user is already logged in
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is already logged in, redirect to main activity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun sendVerificationCode(phoneNumber: String) {
        // Format phone number with country code if needed
        val formattedPhoneNumber = if (phoneNumber.startsWith("+")) phoneNumber else "+91$phoneNumber"

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(formattedPhoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Auto verification (sometimes happens instantly)
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Toast.makeText(this@Login, "Verification failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("LoginActivity", "Verification failed", e)
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    storedVerificationId = verificationId
                    resendToken = token
                    Toast.makeText(this@Login, "OTP sent successfully", Toast.LENGTH_SHORT).show()

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
                    // Sign in success
                    val user = task.result?.user
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()

                    // Navigate to main activity
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // Sign in failed
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    Log.e("LoginActivity", "Authentication failed", task.exception)
                }
            }
    }
}