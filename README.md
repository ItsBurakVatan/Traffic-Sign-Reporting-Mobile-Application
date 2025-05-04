# 🚧 Traffic Sign Reporting Mobile Application

![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)
![OpenStreetMap](https://img.shields.io/badge/OpenStreetMap-7EBC6F?style=for-the-badge&logo=openstreetmap&logoColor=white)
![Mobile](https://img.shields.io/badge/Platform-Android-green?style=for-the-badge)

---

## 📱 About

This is a Kotlin-based mobile application that enables users to report, visualize, and manage traffic signs and residential plates with location data. It uses Firebase Realtime Database for data storage and OpenStreetMap for displaying reported sign locations on a map.

---

## ✨ Features

- 📍 Real-time location detection with permission handling
- 🛑 Report traffic or residential signs with metadata (size, direction, pole height, etc.)
- 🌐 View signs on OpenStreetMap with custom markers
- 📋 List active/inactive reports with filtering
- 🔄 Offline data saving and sync when internet is available
- ✅ Checkbox-based confirmation to activate reports
- 🔐 Permissions handling and error messages

---

## 🧱 Technologies

- **Kotlin**
- **Firebase Realtime Database**
- **Google Location Services API**
- **OSMDroid (OpenStreetMap for Android)**
- **Android SDK**

---

## 📂 Project Structure

BistekMobile/
├── app/
│ └── src/
│ └── main/
│ ├── java/com/burak/project/
│ │ ├── MainActivity.kt
│ │ ├── Main1Activity.kt
│ │ ├── DataDisplayActivity.kt
│ │ ├── DataDisplay1Activity.kt
│ │ ├── AktifVerilerActivity.kt
│ │ ├── AktifVeriler1Activity.kt
│ │ ├── AnaMenuActivity.kt
│ │ ├── OpenStreetMapActivity.kt
│ │ └── OpenStreetMapActivity1.kt
│ └── AndroidManifest.xml


---

## 🧭 Usage

1. **Start the app** and allow location permissions.
2. **Select traffic or residential plate entry** from the main menu.
3. **Enter metadata and get real-time location.**
4. **Submit the report** to Firebase (or store offline if not connected).
5. **View submitted reports** on a map or in a list format.
6. **Mark reports as active** using checkboxes and confirmation dialogs.

---

## 🌐 Map Integration

- Uses OSMDroid to display markers based on reported coordinates.
- Marker info includes: ID, location, size/pole, direction, and sign name.
- Zoom and scale bar overlays enabled.
- Only non-active reports are shown.

---

## 📝 Notes

- Offline mode is supported. Data is queued and synchronized when back online.
- All activities are declared in `AndroidManifest.xml`.
- Dual report types are handled separately: `MainActivity` for traffic signs and `Main1Activity` for residential ones.

---

## 🔗 Firebase Schema

```json
veriler: {
  id / m_id: string,
  konum: string,
  levha / levhaAdi: string,
  direk / boyut: string,
  yön: string,
  aktif / m_aktif: boolean
}
