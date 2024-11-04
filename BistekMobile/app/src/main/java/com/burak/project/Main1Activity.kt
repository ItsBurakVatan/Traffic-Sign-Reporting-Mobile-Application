package com.burak.project

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.*
import android.os.Build
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.view.View
import android.os.Handler
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.util.Log
import androidx.appcompat.app.AlertDialog
import android.content.Intent
import android.net.ConnectivityManager
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.Marker


class Main1Activity : AppCompatActivity() {

    private lateinit var locationText: TextView
    private lateinit var levhaAdiEditText: EditText
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var databaseReference: DatabaseReference
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    private var offlineDataQueue: MutableList<Map<String, Any>> = mutableListOf()
    private val locationUpdateInterval = 2000L // 2 seconds
    private val handler = Handler()
    private var nextId: Int = 1

    private fun initializeNextId() {
        databaseReference.child("veriler")
            .orderByChild("m_id")
            .limitToLast(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val maxId1 = dataSnapshot.children.first().child("m_id").getValue(String::class.java)
                        val maxId = maxId1?.toInt()
                        nextId = maxId?.plus(1) ?: 1
                    } else {
                        nextId = 1
                    }
                    syncOfflineDataWithFirebase()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Firebase", "Maksimum id alınamadı: ${databaseError.message}")
                }
            })
    }

    private lateinit var locationCallback: LocationCallback


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main1)

        locationText = findViewById(R.id.locationText)
        levhaAdiEditText = findViewById(R.id.levhaAdiEditText)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        databaseReference = FirebaseDatabase.getInstance().reference
        initializeNextId()


        // Konum izni kontrolü
        if (checkLocationPermission()) {
            startLocationUpdates() // Start location updates
            handler.post(locationRunnable) // Başlangıçta bir kere çağrılsın
        } else {
            // Kullanıcıya izin isteği göster
            requestLocationPermission()
        }

        val boyutSpinner: Spinner = findViewById(R.id.boyutSpinner)
        val yonSpinner: Spinner = findViewById(R.id.yönSpinner)

        val boyutList = listOf("30x120 Tek Satırlı", "45x120 Çift Satırlı", "60x120 Çift Satırlı")
        val yonList = listOf("Yönlü-Sağ", "Yönlü-Sol", "Yönsüz")

        val boyutAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, boyutList)
        boyutAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        boyutSpinner.adapter = boyutAdapter

        val yonAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, yonList)
        yonAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        yonSpinner.adapter = yonAdapter

        val addToDatabaseButton: Button = findViewById(R.id.addToDatabaseButton)
        addToDatabaseButton.text = "Ekle"

        addToDatabaseButton.setOnClickListener {
            if (checkLocationPermission()) {
                val levhaAdi = levhaAdiEditText.text.toString()
                // Verileri Firebase Realtime Database'e ekle
                if (levhaAdi.isNotEmpty()) {
                    // Verileri Firebase Realtime Database'e ekle
                    addLocationToDatabase(
                        locationText.text.toString(),
                        boyutSpinner.selectedItem.toString(),
                        yonSpinner.selectedItem.toString(),
                        levhaAdi
                    )
                } else {
                    showToast("Lütfen levha adını girin.")
                }
            } else {
                // Kullanıcı izin vermediğinde uyarı göster
                showToast("Konum izni verilmedi. İşlem tamamlanamaz.")
            }
        }

        val verileriGorButton: Button = findViewById(R.id.verileriGorButton)
        verileriGorButton.text = "Verileri Gör"

        verileriGorButton.setOnClickListener {
            // Levhaları Firebase Realtime Database'den al ve ekrana göster
            getAllVerilerAndDisplay()
        }

        val syncButton: Button = findViewById(R.id.syncButton)
        syncButton.text = "Yerel Verileri Ekle"

        syncButton.setOnClickListener {
            syncOfflineDataWithFirebase()
        }

        val showOnMapButton: Button = findViewById(R.id.showOnMapButton)
        showOnMapButton.text = "Verileri Haritada Göster"

        showOnMapButton.setOnClickListener {
            val intent = Intent(this, OpenStreetMapActivity1::class.java)

            // Pass the required data as extras
            intent.putExtra("id", nextId.toString())
            intent.putExtra("boyut", boyutSpinner.selectedItem.toString())
            intent.putExtra("yon", yonSpinner.selectedItem.toString())
            intent.putExtra("levhaAdi", levhaAdiEditText.text.toString())
            val currentLocation = locationText.text.toString()
            intent.putExtra("konum", currentLocation)
            // Add the 'aktif' value to the intent
            intent.putExtra("aktif", false)

            startActivity(intent)
        }
    }

    private fun getAllVerilerAndDisplay() {
        if (isInternetAvailable()) {
            databaseReference.child("veriler")
                .orderByChild("m_aktif")
                .equalTo(false)
                .get()
                .addOnSuccessListener { dataSnapshot ->
                    val veriList = mutableListOf<String>()

                    dataSnapshot.children.forEach { child ->
                        val konum = child.child("konum").getValue(String::class.java)
                        val boyut = child.child("boyut").getValue(String::class.java)
                        val yon = child.child("yön").getValue(String::class.java)
                        val id = child.child("m_id").getValue(String::class.java)
                        val levhaAdi = child.child("levhaAdi").getValue(String::class.java)
                        if (konum != null && boyut != null && yon != null && id != null && levhaAdi != null) {
                            val veriInfo =
                                "ID: $id, Konum: $konum, Boyut: $boyut, Yön: $yon, Levha Adı: $levhaAdi\n"
                            veriList.add(veriInfo)
                        }
                    }

                    // Verileri ekrana göster
                    showVeriList(veriList)
                }
                .addOnFailureListener { e ->
                    showToast("Veritabanından veriler alınamadı: ${e.message}")
                }
        } else{
            showLocalVeriler()
        }
    }

    private fun showLocalVeriler() {
        // Yerel verileri al ve ekrana göster
        val veriList = mutableListOf<String>()

        for (offlineData in offlineDataQueue) {
            val id = offlineData["m_id"].toString()
            val konum = offlineData["konum"].toString()
            val boyut = offlineData["boyut"].toString()
            val yon = offlineData["yön"].toString()
            val levhaAdi = offlineData["levhaAdi"].toString()

            val veriInfo = "ID: $id, Konum: $konum, Boyut: $boyut, Yön: $yon, Levha Adı: $levhaAdi\n"
            veriList.add(veriInfo)
        }
        // Verileri ekrana göster
        showVeriList(veriList)
    }


    private fun updateLocationOnScreen(location: String) {
        locationText.text = location
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest().apply {
            interval = locationUpdateInterval
            fastestInterval = locationUpdateInterval
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val locationInfo = "Enlem: $latitude Boylam: $longitude"
                    updateLocationOnScreen(locationInfo)
                }
            }
        }

        if (checkLocationPermission()) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        }
    }

    private val locationRunnable: Runnable = object : Runnable {
        override fun run() {
            try {
                // Konum izni kontrolü
                if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    // Konum izni varsa devam et
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { location ->
                            if (location != null) {
                                val latitude = location.latitude
                                val longitude = location.longitude
                                val locationInfo = "Enlem: $latitude Boylam: $longitude"
                                updateLocationOnScreen(locationInfo) // Update location on screen
                            } else {
                                locationText.text = "Konum bilgisi bulunamadı."
                            }
                        }
                        .addOnFailureListener { e ->
                            locationText.text = "Konum alınamadı: ${e.message}"
                        }
                } else {
                    // Konum izni yoksa izin talep et
                    requestLocationPermission()
                    Log.w("Permission", "Konum izni yok.")
                }
            } catch (se: SecurityException) {
                locationText.text = "Konum izni verilmedi."
            } finally {
                // Belirli aralıklarla tekrar çalışması için postDelayed kullanılır
                handler.postDelayed(this, locationUpdateInterval)
            }
        }
    }

    private fun checkLocationPermission(): Boolean {
        return (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun showVeriList(veriList: List<String>) {
        val formattedVeriList = mutableListOf<String>()

        for (veri in veriList) {
            val (id, konum, boyut, yon, levhaAdi) = veri.split(", ")



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


        // Oluşturulan düzenlenmiş verileri yeni sayfada göstermek için Intent oluştur
        val intent = Intent(this, DataDisplay1Activity::class.java)

        // formattedVeriList'i yeni sayfaya gönder
        intent.putStringArrayListExtra("formattedVeriList", ArrayList(formattedVeriList))

        // Yeni sayfayı başlat
        startActivity(intent)
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults.all { it == PackageManager.PERMISSION_GRANTED }
                ) {
                    startLocationUpdates()
                } else {
                    showToast("Konum izni verilmedi.")
                }
            }
        }
    }

    private fun syncOfflineDataWithFirebase(){
        if (offlineDataQueue.isNotEmpty() && isInternetAvailable()){
            for (offlineData in offlineDataQueue){
                val id = offlineData["m_id"].toString()
                val konum = offlineData["konum"].toString()
                val boyut = offlineData["boyut"].toString()
                val yon = offlineData["yön"].toString()
                val levhaAdi = offlineData["levhaAdi"].toString()

                // Firebase'e veriyi ekle
                addLocationToFirebaseOffline(id, konum, boyut, yon, levhaAdi)
            }

            // Yerel kuyruğu temizle
            offlineDataQueue.clear()

            // Kullanıcıya mesaj göster
            showToast("Yereldeki veriler Firebase'e eklendi.")
        }
    }

    private fun storeOfflineData(location: String, boyut: String, yon: String, levhaAdi: String){
        // Veriyi yerelde bir kuyrukta sakla
        val newData = hashMapOf<String, Any>()
        val id = nextId.toString()

        newData["m_id"] = id
        newData["konum"] = location
        newData["boyut"] = boyut
        newData["yön"] = yon
        newData["levhaAdi"] = levhaAdi
        newData["m_aktif"] = false

        offlineDataQueue.add(newData)
        nextId++

        showToast("İnternet bağlantısı yok. Veri yerelde saklandı.")
    }

    private fun isInternetAvailable(): Boolean {
        // Cihazın aktif bir internet bağlantısı olup olmadığını kontrol et
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun addLocationToDatabase(location: String, boyut: String, yon: String, levhaAdi: String) {
        if (isInternetAvailable()) {
            // İnternet varsa, veriyi Firebase'e ekle
            addLocationToFirebase(location, boyut, yon, levhaAdi)
        } else {
            // İnternet yoksa, veriyi yerelde depola
            storeOfflineData(location, boyut, yon, levhaAdi)
        }
    }


        private fun addLocationToFirebaseOffline(id: String,location: String, boyut: String, yon: String,levhaAdi: String) {
        // "veriler" düğümü altında yeni bir alt düğüm (child) oluştur
        val newChildRef = databaseReference.child("veriler").push()

        val newData = hashMapOf<String, Any>()
        newData["m_id"] = id
        newData["konum"] = location
        newData["boyut"] = boyut
        newData["yön"] = yon
        newData["levhaAdi"] = levhaAdi
        newData["m_aktif"] = false

        // Yeni oluşturulan alt düğüme veriyi ekle
        newChildRef.setValue(newData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Ekleme işlemi başarılı oldu, kullanıcıya mesaj göster
                    showToast("Ekleme işlemi başarılı!")
                } else {
                    // Ekleme işlemi başarısız oldu, hata mesajını log'a ekle
                    Log.e("Firebase", "Ekleme işlemi başarısız: ${task.exception}")
                    // Hata durumunu kullanıcıya mesaj olarak göster
                    showToast("Ekleme işlemi başarısız oldu. Hata: ${task.exception?.message}")
                }
            }
    }

    private fun addLocationToFirebase(location: String, boyut: String, yon: String, levhaAdi: String) {
        // "veriler" düğümü altında yeni bir alt düğüm (child) oluştur
        val newChildRef = databaseReference.child("veriler").push()

        val id = nextId.toString()


        val newData = hashMapOf<String, Any>()
        newData["m_id"] = id
        newData["konum"] = location
        newData["boyut"] = boyut
        newData["yön"] = yon
        newData["levhaAdi"] = levhaAdi
        newData["m_aktif"] = false


        // Yeni oluşturulan alt düğüme veriyi ekle
        newChildRef.setValue(newData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Ekleme işlemi başarılı oldu, kullanıcıya mesaj göster
                    showToast("Ekleme işlemi başarılı!")
                    nextId++
                } else {
                    // Ekleme işlemi başarısız oldu, hata mesajını log'a ekle
                    Log.e("Firebase", "Ekleme işlemi başarısız: ${task.exception}")
                    // Hata durumunu kullanıcıya mesaj olarak göster
                    showToast("Ekleme işlemi başarısız oldu. Hata: ${task.exception?.message}")
                }
            }
    }


    private fun showToast(message: String) {
        // Kısa süreli bir Toast mesajı gösterme
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(locationRunnable) // Aktivite sona erdiğinde çalışmayı durdur
    }


}

