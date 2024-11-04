package com.burak.project

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.burak.project.R
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.osmdroid.util.BoundingBox
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


class OpenStreetMapActivity1 : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private lateinit var databaseReference: DatabaseReference
    private lateinit var geoPoint: GeoPoint // Sınıf seviyesinde tanımla


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_openstreetmap1)

        // osmdroid kütüphanesini başlat
        Configuration.getInstance().load(applicationContext, getPreferences(MODE_PRIVATE))

        mapView = findViewById(R.id.mapView)
        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)

        databaseReference = FirebaseDatabase.getInstance().reference

        val turkeyCenter = GeoPoint(39.9334, 32.8597)
        mapView.controller.setCenter(turkeyCenter)
        mapView.controller.setZoom(6.0) // İhtiyaca göre zoom seviyesini ayarlayabilirsiniz

        // Gps konum göstergesini eklemek için
        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), mapView)
        locationOverlay.enableMyLocation()
        mapView.overlays.add(locationOverlay)

        // ScaleBar'ı eklemek için
        val scaleBarOverlay = ScaleBarOverlay(mapView)
        mapView.overlays.add(scaleBarOverlay)

        // İki parmakla zoom işlemini sağlamak için GestureDetector oluştur
        scaleGestureDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val scaleFactor = detector.scaleFactor
                val currentZoom = mapView.zoomLevelDouble
                val newZoom = currentZoom * scaleFactor
                mapView.controller.setZoom(newZoom)
                return true
            }
        })


        geoPoint = GeoPoint(0.0, 0.0) // Başlangıçta bir değer ata, istediğiniz bir değer olabilir

        // Firebase'den verileri al ve haritaya marker'lar ekle
        databaseReference.child("veriler")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (veriSnapshot in dataSnapshot.children) {
                        val aktif = veriSnapshot.child("m_aktif").getValue(Boolean::class.java)

                        // "m_id" alanını kontrol et
                        if(veriSnapshot.hasChild("m_id") && aktif == false){
                            val konum = veriSnapshot.child("konum").getValue(String::class.java)
                            Log.d("KonumLog", "Konum: $konum")

                            if (konum != null) {
                                // Konum verisini parçala ve GeoPoint oluştur
                                val latLngArray = konum.split(" ") // "Enlem: 39.9957079 Boylam: 32.7526849"

                                if (latLngArray.size == 4) {
                                    // Arrayin boyutu beklenen değere uygunsa devam et
                                    val latitude = latLngArray[1].toDoubleOrNull()
                                    val longitude = latLngArray[3].toDoubleOrNull()

                                    if (latitude != null && longitude != null) {
                                        val geoPoint = GeoPoint(latitude, longitude)

                                        val intent = intent
                                        val id = veriSnapshot.child("m_id").getValue(String::class.java)
                                        val boyut = veriSnapshot.child("boyut").getValue(String::class.java)
                                        val yon = veriSnapshot.child("yön").getValue(String::class.java)
                                        val levhaAdi = veriSnapshot.child("levhaAdi").getValue(String::class.java)

                                        // Marker'ı oluştur ve bilgileri ekleyerek haritaya ekle
                                        val marker = Marker(mapView)
                                        marker.position = geoPoint
                                        marker.snippet = "ID: $id, Boyut: $boyut\nYön: $yon, Levha Adı: $levhaAdi"
                                        mapView.overlays.add(marker)
                                    } else {
                                        // Latitude veya longitude değeri beklenen formatta değilse hata işleme
                                        Log.e("KonumHata", "Latitude veya longitude değeri beklenen formatta değil.")
                                    }
                                } else {
                                    // Arrayin boyutu beklenen değere uygun değilse hata işleme
                                    Log.e("KonumHata", "Latitude ve longitude değerleri bulunamadı.")
                                }
                            }
                        }
                    }

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Hata durumunu log'a ekle
                }
            })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // ScaleGestureDetector'a dokunma olaylarını iletilir
        scaleGestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

}