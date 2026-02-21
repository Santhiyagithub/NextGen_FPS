
# 🚀 NextGen Team Pixel – High-Speed FPS Camera & Video Temporal Error Detection

![License](https://img.shields.io/badge/License-MIT-green.svg)
![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20Python-blue.svg)
![Status](https://img.shields.io/badge/Status-Active-success.svg)

## 📌 Project Overview
![logo](https://github.com/user-attachments/assets/7692cfef-b090-4fa8-badf-a206fbdd5ee9)

This repository contains two advanced video processing systems developed by NextGen Team Pixel:

1. High-Speed FPS Camera System (Android App)  
2. Video Temporal Error Detection System (Web-Based Tool)
Project Link: ![Project link](https://huggingface.co/spaces/yoga28v28/Video_temporal_error_detection)

These systems together provide high-frame-rate video capture and automatic detection of frame drops and merges.

---

## 🎯 Problem Statement

Modern video systems often suffer from:

- Frame drops  
- Frame merging  
- Inconsistent FPS  
- Motion blur  

This project solves these issues by:

- True 120/240 FPS recording  
- Manual camera control  
- Automatic anomaly detection  

---

## 🧩 System Components

### 📱 High-Speed FPS Camera App

Features:

- 120 / 240 FPS Recording  
- Manual ISO & Shutter  
- HEVC Encoding  
- Resolution Control  
- Metadata Display  

---

### 🌐 Temporal Error Detection System

Features:

- Frame analysis  
- Timestamp validation  
- Motion similarity detection  
- CSV report generation  
- Cloud support  

---

## 🏗️ System Architecture

### High-Speed Camera

UI → Camera2 API → High-Speed Session → Encoder → Storage → Playback

### Error Detection

User → Gradio → Python → OpenCV → Output

---

## 🛠️ Technology Stack

### Android

- Kotlin
- Camera2 API
- MediaRecorder
- ExoPlayer
- MediaStore

### Python

- Python 3
- Gradio
- OpenCV
- NumPy
- FFmpeg

---

## 📚 Libraries

### Android

implementation "com.google.android.exoplayer:exoplayer:2.19.1"

### Python

gradio  
opencv-python  
numpy  

---

## ⚙️ Installation

### Android App

git clone https://github.com/yogaV28/NextGen_Team_Pixel.git

Open in Android Studio and Run.

### Detection System

pip install gradio opencv-python numpy

python app.py

Open: http://localhost:7860

---

## 📊 Output Example
![WhatsApp Image 2026-02-21 at 1 11 46 PM](https://github.com/user-attachments/assets/9ef037ed-24b2-4010-b887-2436bbf316a4)
![WhatsApp Image 2026-02-21 at 1 11 46 PM (2)](https://github.com/user-attachments/assets/d86f3272-afda-462d-9268-210d4e55dd0d)
![WhatsApp Image 2026-02-21 at 1 11 49 PM](https://github.com/user-attachments/assets/329d721e-5847-4ce1-823e-1a622e0356f7)
![WhatsApp Image 2026-02-21 at 1 11 50 PM](https://github.com/user-attachments/assets/ea0ee957-cdcc-48e3-aa2c-4dcdf4b9f9eb)
![WhatsApp Image 2026-02-21 at 1 11 46 PM (1)](https://github.com/user-attachments/assets/9492d2f0-a67f-4ea4-bfa6-0275f0907785)
![WhatsApp Image 2026-02-21 at 1 11 51 PM (1)](https://github.com/user-attachments/assets/189d2b45-eff3-4c64-8780-364a012cadbf)
![WhatsApp Image 2026-02-21 at 1 11 51 PM](https://github.com/user-attachments/assets/9e8db198-ac08-44fe-9eac-2b185d04e992)
![WhatsApp Image 2026-02-21 at 1 11 47 PM](https://github.com/user-attachments/assets/3f7f10a8-cafd-4927-8198-7477d39e4806)
https://github.com/user-attachments/assets/5d3c7018-48c2-4ebe-93a2-a6dc19c0dbc0


FPS: 240  
Encoder: HEVC  
Resolution: 1920x1080  
Bitrate: 80 Mbps  

CSV:
![WhatsApp Image 2026-02-21 at 1 11 46 PM](https://github.com/user-attachments/assets/d3b4dd35-f8c2-4a66-b62f-64c39b117f18)
![WhatsApp Image 2026-02-21 at 1 11 46 PM (2)](https://github.com/user-attachments/assets/18ea86fb-4135-4654-9ce0-cba6eea6e703)
![WhatsApp Image 2026-02-21 at 1 11 47 PM (1)](https://github.com/user-attachments/assets/0f435853-935e-4ec7-95f6-0ac602b3a391)
![WhatsApp Image 2026-02-21 at 1 11 48 PM](https://github.com/user-attachments/assets/14092d41-7daf-4fd3-ac4a-a9b24d7b0ae1)

Frame,Label  
1,Normal  
2,Frame Drop  

---

## 📈 Performance

- Fixed FPS Lock  
- AE Disabled  
- Hardware Encoding  
- Background Processing  

---

## 📌 Applications

- Video Quality Testing  
- Surveillance  
- Broadcasting  
- Autonomous Vehicles  
- Research  

---

## 👨‍💻 Developer

Yoga Vignesh V  
GitHub: https://github.com/yogaV28

---

## 📄 License

MIT License

---

## ⭐ Support

Please star ⭐ this repository if you like this project!
