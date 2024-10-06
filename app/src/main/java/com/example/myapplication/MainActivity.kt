package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.activities.LoginActivity
import com.example.myapplication.activities.ProfileActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_login)

        val intent = Intent(this, LoginActivity::class.java)//login
        startActivity(intent)
    }
}
