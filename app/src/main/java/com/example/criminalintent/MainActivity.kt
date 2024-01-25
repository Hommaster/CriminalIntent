package com.example.criminalintent

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.criminalintent.databinding.ActivityMainBinding
import java.util.UUID

@SuppressLint("StaticFieldLeak")
private lateinit var bindingClass: ActivityMainBinding

class MainActivity : AppCompatActivity(), CrimeListFragment.Callbacks {
    override fun onCreate(savedInstanceState: Bundle?) {
        bindingClass = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        if(currentFragment == null) {
            val fragment = CrimeListFragment.newInstance()
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit()
        }

        setContentView(bindingClass.root)
    }

    override fun onCrimeSelected(crimeId: UUID) {
        val fragment = CrimeFragment()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}