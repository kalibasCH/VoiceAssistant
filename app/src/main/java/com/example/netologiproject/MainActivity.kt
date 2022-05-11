package com.example.netologiproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    val TAG: String = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "start of onCreate function")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val name: String = "Ivan"
        val surname: String = "Ivanov"
        val age: Int = 37
        val height: Double = 172.2

        val stringOfText = "name: $name, surname: $surname, age: $age, height: $height"

        val output: TextView = findViewById(R.id.output)
        output.text = stringOfText

        Log.d(TAG, "end of onCreate function")
    }
}



