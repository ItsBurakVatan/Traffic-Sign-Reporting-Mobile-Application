// DataDisplay1Activity.kt
package com.burak.project

import android.app.AlertDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.widget.LinearLayout
import android.widget.CheckBox
import com.google.firebase.database.DatabaseReference
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Button

class DataDisplay1Activity : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_display1)

        databaseReference = FirebaseDatabase.getInstance().reference

        // formattedVeriList'i intent'ten al
        val formattedVeriList = intent.getStringArrayListExtra("formattedVeriList")

        val linearLayout: LinearLayout = findViewById(R.id.linearLayout)



        formattedVeriList?.forEach { veri ->
            val checkBox = CheckBox(this)
            checkBox.text = veri

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                // CheckBox durumu değiştiğinde burada işlem yapabilirsiniz
                if (isChecked) {
                    // Seçildiyse
                    showConfirmationDialog(veri)
                }
            }
            // CheckBox'ı LinearLayout'a ekle
            linearLayout.addView(checkBox)
        }

        val aktifVerileriGorButton: Button = findViewById(R.id.aktifVerileriListele)
        aktifVerileriGorButton.setOnClickListener {
            val intent = Intent(this, AktifVeriler1Activity::class.java)
            startActivity(intent)
        }



    }
    private fun showToast(message: String) {
        Toast.makeText(this@DataDisplay1Activity, message, Toast.LENGTH_SHORT).show()
    }


    private fun updateAktifStatus(veri: String, yeniDurum: Boolean) {
        val idValue = veri.substringAfter("ID:").substringBefore("\n").trim()

        if (idValue.isNotEmpty()) {
            val veriReference = databaseReference.child("veriler")

            val query = veriReference.orderByChild("m_id").equalTo(idValue)

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Log.e("DataDisplay1Activity", "Veri bulundu: $veri")
                        for (veriSnapshot in snapshot.children) {
                            val key = veriSnapshot.key
                            if (key != null) {
                                val updateData = hashMapOf<String, Any>("m_aktif" to yeniDurum)
                                val veriToUpdateReference = veriReference.child(key)
                                veriToUpdateReference.updateChildren(updateData)
                            }
                        }
                    } else {
                        Log.e("DataDisplay1Activity", "Veri bulunamadı: $veri")
                        showToast("Veri bulunamadı: $veri")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("DataDisplay1Activity", "Veritabanına erişilemiyor: ${error.message}")
                    showToast("Veritabanına erişilemiyor: ${error.message}")
                }


            })
        } else {
            showToast("Geçersiz ID değeri: $veri")
        }
    }

    private fun showConfirmationDialog(veri: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Onay")
        builder.setMessage("Bu verinin aktifliğini sona erdirmek istediğinize emin misiniz?")

        // Evet butonu
        builder.setPositiveButton("Evet") { _, _ ->
            // Verinin aktifliğini sona erdirme işlemini yap
            updateAktifStatus(veri, true)
            showSuccessDialog()
        }

        // Hayır butonu
        builder.setNegativeButton("Hayır") { _, _ ->
            // Hiçbir işlem yapma, pop-up'ı kapat
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun showSuccessDialog() {
        val successBuilder = AlertDialog.Builder(this)
        successBuilder.setTitle("İşlem Başarılı!")
        successBuilder.setMessage("Verinin aktifliği başarıyla sona erdirildi.")

        successBuilder.setPositiveButton("Tamam") { _, _ ->
            // Kullanıcı tamam dediğinde hiçbir işlem yapma, pop-up'ı kapat
        }

        val successDialog = successBuilder.create()
        successDialog.show()
    }

}
