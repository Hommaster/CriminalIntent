package com.example.criminalintent

import android.app.Application

class CrimeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        CrimeRepository.initialize(this)
    }
}