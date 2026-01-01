# 📺 TV Screensaver & Dashboard

A modern, feature-rich screensaver application for Android TV, designed to transform your idle screen into a beautiful and informative dashboard. This project integrates a Native Android App, a Node.js Backend, and ESP32 Firmware for IoT connectivity.

![Banner](banner.png)

## ✨ Features

- **Dynamic Weather**: Real-time weather updates with distinct visual themes (Sunny, Rainy, Cloudy, Night, etc.).
- **Smart Dashboard**: Displays time, date, and essential information at a glance.
- **Customizable**: Settings to toggle units, update intervals, and more.
- **IoT Integration**: Connects with ESP32 devices for room temperature and humidity monitoring (optional).
- **Backend API**: Node.js backend to proxy weather data and manage device connections.

## 🛠️ Tech Stack

- **Android App**: Kotlin, Jetpack Compose / XML Views, Retrofit, Coroutines.
- **Backend**: Node.js, Express.
- **Firmware**: C++ (Arduino Framework) for ESP32.

## 🚀 Installation

### Android TV App

1. Download the latest APK from the [Releases](releases/) page.
2. Sideload the APK onto your Android TV using `ADB` or a file manager.
3. Launch the app and grant necessary permissions.

### Backend Setup (Optional)

Required if you want to host your own weather proxy or use the IoT features.

1. Navigate to the `backend/` directory.
2. Install dependencies:

   ```bash
   npm install
   ```

3. Create a `.env` file with your API keys (see `.env.example`).
4. Start the server:

   ```bash
   node server.js
   ```

## 📦 Project Structure

- `android_app/`: Source code for the Android TV application.
- `backend/`: Node.js server code.
- `esp32_firmware/`: Firmware for ESP32 sensors.

## 🤝 Contributing

Contributions are welcome! Please fork the repository and submit a pull request.

## 📄 License

This project is open-source and available for personal use.
