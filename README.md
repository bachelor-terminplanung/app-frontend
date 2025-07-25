# 📅 Terminplaner App mit OCR

Diese Android-App ermöglicht es Benutzer:innen, Termine manuell einzugeben oder mithilfe von Texterkennung (OCR) automatisch aus handschriftlichen oder gedruckten Texten zu erkennen und zu speichern.

## 🔧 Technologien

- **Frontend**: Android (Java)
- **Backend**: Node.js mit Express
- **Texterkennung**: Google Vision API
- **Datenbank**: MySQL
- **Kommunikation**: HTTP (REST API)

## 🧠 Hauptfunktionen

- Termine anlegen (manuell oder per OCR)
- Wiederholende Termine mit verschiedenen Wiederholungsintervallen
- Visuelle Oberfläche zur Eingabe von Datum, Uhrzeit, Dauer usw.
- Speicherung der Termine auf dem Server

## 📷 OCR-Funktion

- Bildaufnahme aus Galerie
- Texterkennung mit Google Vision API
- Extraktion von Datum und Uhrzeit aus erkannten Texten
- Automatischer Vorschlag für Termineintrag basierend auf OCR-Text

## ▶️ Installation & Ausführen

### 📱 Android-App

1. Projekt in Android Studio öffnen
2. Kamera- und Internetberechtigungen setzen
3. API-Key für Google Vision API in `CloudOCR.java` einfügen
4. IP-Adresse setzen im VisionApiHelper
5. Emulator oder echtes Gerät starten
6. App ausführen

