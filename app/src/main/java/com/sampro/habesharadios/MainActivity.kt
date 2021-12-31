package com.sampro.habesharadios

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {

    private lateinit var cvAfroFM: CardView
    private lateinit var cvShegerFM: CardView
    private lateinit var cvAhaduFM: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cvAfroFM = findViewById(R.id.cvAfroFM)
        cvShegerFM = findViewById(R.id.cvShegerFM)
        cvAhaduFM = findViewById(R.id.cvAhaduFM)

        cvAfroFM.setOnClickListener {
            val intent = Intent(this@MainActivity, PlayerActivity::class.java)
            intent.putExtra("station","Afro FM")
            startActivity(intent)
//            Toast.makeText(this,"Afro FM Clicked!",Toast.LENGTH_SHORT).show()
        }

        cvShegerFM.setOnClickListener {
            val intent = Intent(this@MainActivity, PlayerActivity::class.java)
            intent.putExtra("station","Sheger FM")
            startActivity(intent)
        }

        cvAhaduFM.setOnClickListener {
            val intent = Intent(this@MainActivity, PlayerActivity::class.java)
            intent.putExtra("station","Ahadu FM")
            startActivity(intent)
        }

    }
}