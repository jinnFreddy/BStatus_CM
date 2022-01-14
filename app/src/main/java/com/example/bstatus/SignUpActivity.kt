package com.example.bstatus

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {

    private lateinit var edtUser : EditText
    private lateinit var edtTextEmail : EditText
    private lateinit var edtTextPassword : EditText
    private lateinit var btnSignUp : Button
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        supportActionBar?.hide()
        mAuth = FirebaseAuth.getInstance()

        edtUser = findViewById(R.id.edt_user)
        edtTextEmail = findViewById(R.id.edt_email)
        edtTextPassword = findViewById(R.id.edt_password)
        btnSignUp = findViewById(R.id.btnSignUp)

        btnSignUp.setOnClickListener {
            val name = edtUser.text.toString()
            val email = edtTextEmail.text.toString()
            val password = edtTextPassword.text.toString()

            signUp(name, email, password)
        }
    }

    private fun signUp(name: String, email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    addUserToDatabase(name, email, mAuth.currentUser?.uid!!)
                    val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                    finish()
                    startActivity(intent)
                } else {
                    Toast.makeText(this@SignUpActivity, "Some error occured", Toast.LENGTH_SHORT).show()

                }
        }
    }

    private fun addUserToDatabase(name: String, email: String, uid: String) {
        mDbRef = FirebaseDatabase.getInstance().getReference()
        mDbRef.child("user").child(uid).setValue(User(name, email, uid))

    }
}

