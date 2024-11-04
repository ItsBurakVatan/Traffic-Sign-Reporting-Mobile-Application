package com.burak.project

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class AnaMenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ana_menu)

        val trafikLevhasiEkleButton: Button = findViewById(R.id.trafikLevhasiEkleButton)
        val meskenMahalLevhasiEkleButton: Button = findViewById(R.id.meskenMahalLevhasiEkleButton)

        trafikLevhasiEkleButton.setOnClickListener {
            // Trafik levhası ekle butonuna tıklandığında MainActivity'yi başlat
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        meskenMahalLevhasiEkleButton.setOnClickListener {
            // Mesken mahal levhası ekle butonuna tıklandığında Main1Activity'yi başlat
            val intent = Intent(this, Main1Activity::class.java)
            startActivity(intent)
        }
    }
}
