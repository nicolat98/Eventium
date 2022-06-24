package com.example.eventium

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUp : AppCompatActivity() {
    private lateinit var et_email: EditText
    private lateinit var et_password: EditText
    private lateinit var et_username: EditText
    private lateinit var et_password2: EditText
    private lateinit var btn_signup: Button
    private lateinit var btn_back: Button


    //Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        et_email = findViewById(R.id.et_email)
        et_password = findViewById(R.id.et_password)
        et_password2 = findViewById(R.id.et_password2)
        et_username = findViewById(R.id.et_username)
        btn_signup = findViewById(R.id.btn_signup)
        btn_back = findViewById(R.id.btn_back)

        //inizializzo auth
        auth = FirebaseAuth.getInstance()

        btn_signup.setOnClickListener {
            val username = et_username.text.toString()
            val email = et_email.text.toString()
            val password = et_password.text.toString()
            val password2 = et_password2.text.toString()


            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || password2.isEmpty()) {
                Toast.makeText(baseContext, "Empty text...", Toast.LENGTH_SHORT).show()
            } else {
                if (password.equals(password2)) {
                    signup(username, email, password)
                } else {
                    Toast.makeText(baseContext, "Password does not match...", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btn_back.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
            finish()
        }
    }

    private fun signup(username: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    //aggiugo l'utente al database
                    addUserToDatabase(username, email, auth.currentUser?.uid!!)
                    val intent = Intent(this@SignUp, MainActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_from_bottom, R.anim.slide_to_up)
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("EventiumError", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun addUserToDatabase(username: String, email: String, uid: String) {
        db = FirebaseDatabase.getInstance().reference
        val favorites = ArrayList<String>()
        favorites.add("null")
        db.child("user").child(uid).setValue(User(username, email, uid, favorites)) //lo aggiungo al db
    }
}