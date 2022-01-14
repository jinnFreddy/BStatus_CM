package com.example.bstatus

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var edtTextEmail : EditText
    private lateinit var edtTextPassword : EditText
    private lateinit var btnLogin : Button
    private lateinit var btnSignUp : Button
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()

        edtTextEmail = findViewById(R.id.edt_email)
        edtTextPassword = findViewById(R.id.edt_password)
        btnLogin = findViewById(R.id.btnLogin)
        btnSignUp = findViewById(R.id.btnSignUp)

        btnSignUp.setOnClickListener{
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener{
            val email = edtTextEmail.text.toString()
            val password = edtTextPassword.text.toString()

            login(email,password)
        }
    }

    private fun login(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    finish()
                    startActivity(intent)
                } else {
                    Toast.makeText(this@LoginActivity, "User does not exist", Toast.LENGTH_SHORT).show()
                }
            }
    }

}