package com.burak.project

import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*


class AktifVeriler1Activity : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aktif_veriler1)

        databaseReference = FirebaseDatabase.getInstance().reference

        val linearLayout: LinearLayout = findViewById(R.id.aktifVerilerLinearLayout)

        getAktifVerilerAndDisplay(linearLayout)
    }

    private fun getAktifVerilerAndDisplay(linearLayout: LinearLayout) {
        databaseReference.child("veriler")
            .orderByChild("m_aktif")
            .equalTo(true)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val veriList = mutableListOf<String>()

                    dataSnapshot.children.forEach { child ->
                        val konum = child.child("konum").getValue(String::class.java)
                        val boyut = child.child("boyut").getValue(String::class.java)
                        val yon = child.child("yön").getValue(String::class.java)
                        val id = child.child("m_id").getValue(String::class.java)
                        val levhaAdi = child.child("levhaAdi").getValue(String::class.java)
                        if (konum != null && boyut != null && yon != null && id != null && levhaAdi != null) {
                            val veriInfo = "ID: $id, Konum: $konum, Boyut: $boyut, Yön: $yon, Levha Adı: $levhaAdi\n"
                            veriList.add(veriInfo)
                        }
                    }

                    // Verileri ekrana göster
                    showAktifVeriList(linearLayout, veriList)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Hata durumunu işle
                    showToast("Veriler alınamadı: ${databaseError.message}")
                }
            })
    }

    private fun showAktifVeriList(linearLayout: LinearLayout, veriList: List<String>) {
        val formattedVeriList = mutableListOf<String>()

        for (veri in veriList) {
            val (id, konum, boyut, yon, levhaAdi) = veri.split(",")



            // Veriyi düzenle ve \n ekleyerek yeni bir string oluştur
            val formattedVeri = buildString {
                append("\n\n$id")
                append("\n\n$konum")
                append("\n\n$boyut\n\n")
                append("$yon\n\n")
                append("$levhaAdi\n\n")
            }

            formattedVeriList.add(formattedVeri)
        }

        // TextView'ları ekleyerek verileri göster
        for (formattedVeri in formattedVeriList) {
            val textView = TextView(this)
            textView.text = formattedVeri
            linearLayout.addView(textView)
        }
    }

    private fun showToast(message: String) {
        // Kısa süreli bir Toast mesajı gösterme
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
