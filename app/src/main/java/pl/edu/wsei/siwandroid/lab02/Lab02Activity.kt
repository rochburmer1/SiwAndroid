package pl.edu.wsei.siwandroid.lab02

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pl.edu.wsei.siwandroid.R
import pl.edu.wsei.siwandroid.lab03.Lab03Activity

class Lab02Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lab02)
    }

    fun onPickSize(v: View) {
        val tag = v.tag as String
        val tokens = tag.split(" ")
        val rows = tokens[0].toInt()
        val cols = tokens[1].toInt()

        val intent = Intent(this, Lab03Activity::class.java)
        intent.putExtra("rows", rows)
        intent.putExtra("cols", cols)
        startActivity(intent)
    }
}