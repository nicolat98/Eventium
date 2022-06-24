package com.example.eventium

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {

    private lateinit var et_email: EditText
    private lateinit var et_password: EditText
    private lateinit var btn_login: Button
    private lateinit var btn_signup: Button

    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        et_email = findViewById(R.id.et_email)
        et_password = findViewById(R.id.et_password)
        btn_login = findViewById(R.id.btn_login)
        btn_signup = findViewById(R.id.btn_signup)

        //inizializzo auth
        auth = FirebaseAuth.getInstance()


        btn_signup.setOnClickListener{
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
            finish()
        }

        btn_login.setOnClickListener{
            val email = et_email.text.toString()
            val password = et_password.text.toString()
            if(email.isEmpty() || password.isEmpty()){
                Toast.makeText(baseContext, "Empty text...", Toast.LENGTH_SHORT).show()
            }else{
                login(email, password)
            }
        }
    }

    private fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val intent = Intent(this@Login, MainActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_from_bottom, R.anim.slide_to_up)
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("EventiumError", "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}