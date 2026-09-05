package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class ChambaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApiKey("AIzaSyAt8iuuZe4_xNbRiOMRaSt0MxS7xNI67j8")
                    .setApplicationId("1:982367861690:android:3f8e52c96c4d5389")
                    .setProjectId("chamba-rd-cef51")
                    .setGcmSenderId("982367861690")
                    .setStorageBucket("chamba-rd-cef51.firebasestorage.app")
                    .build()
                FirebaseApp.initializeApp(this, options)
            }
            Log.d("ChambaApp", "Firebase initialized successfully with project chamba-rd-cef51")
        } catch (e: Exception) {
            Log.w("ChambaApp", "Firebase initialization deferred: ${e.message}")
        }
    }
}

