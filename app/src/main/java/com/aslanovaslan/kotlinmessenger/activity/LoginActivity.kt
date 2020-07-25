package com.aslanovaslan.kotlinmessenger.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aslanovaslan.kotlinmessenger.R
import com.aslanovaslan.kotlinmessenger.internal.ProgressFragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity:AppCompatActivity() {
    private lateinit var progressBar: ProgressFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login_button_login.setOnClickListener{
            createProgressBar()
            if (signInWithEmailFireBase())return@setOnClickListener
        }
        idont_have_account_text_view.setOnClickListener{
           finish() /*val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)*/
        }
    }
    private fun createProgressBar() {
        progressBar = ProgressFragment()
        progressBar.show(supportFragmentManager, "mainDialog")
        progressBar.isCancelable = false
    }
    private fun cancelProgressBar() {
        progressBar.dismiss()
    }

    private fun signInWithEmailFireBase(): Boolean {
        val email = editTextTextPersonNameLogin.text.toString()
        val password = editTextTextPasswordLogin.text.toString()
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email/password ", Toast.LENGTH_SHORT).show()
            return true
        }
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                val intent = Intent(this, LatestMessageActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK))
                startActivity(intent)
                finish()
                Log.d(TAG, "onCreate: user found: ${it.result?.user?.uid}")
                cancelProgressBar()
            }.addOnFailureListener {
                Toast.makeText(this, "User not found ${it.message}", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "onCreate: user not found")
                cancelProgressBar()
            }
        return false
    }
    companion object{
        private const val TAG = "LoginActivity"
    }
}