
# 🚀 NextGen Team Pixel – High-Speed FPS Camera & Video Temporal Error Detection

![License](https://img.shields.io/badge/License-MIT-green.svg)
![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20Python-blue.svg)
![Status](https://img.shields.io/badge/Status-Active-success.svg)

## 📌 Project Overview

This repository contains two advanced video processing systems developed by NextGen Team Pixel:

1. High-Speed FPS Camera System (Android App)  
2. Video Temporal Error Detection System (Web-Based Tool)

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

FPS: 240  
Encoder: HEVC  
Resolution: 1920x1080  
Bitrate: 80 Mbps  

CSV:

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
