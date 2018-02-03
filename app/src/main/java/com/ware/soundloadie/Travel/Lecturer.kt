package com.ware.soundloadie.Travel

import android.content.Intent
import android.os.Bundle
import com.github.paolorotolo.appintro.AppIntro
import android.support.annotation.Nullable
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import com.github.paolorotolo.appintro.AppIntroFragment
import com.ware.soundloadie.MainActivity
import com.ware.soundloadie.R


class Lecturer : AppIntro() {
    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val colores =  ContextCompat.getColor(this@Lecturer, R.color.colorPrimary)
        addSlide(AppIntroFragment.newInstance(getString(R.string.title1),getString(R.string.intro1), R.drawable.a1, colores ))
        addSlide(AppIntroFragment.newInstance(getString(R.string.title2),getString(R.string.intro2), R.drawable.a2, colores ))
        addSlide(AppIntroFragment.newInstance(getString(R.string.title3),getString(R.string.intro3), R.drawable.a3, colores ))





        askForPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),3)




        // Hide Skip/Done button.
        showSkipButton(true)
        setSwipeLock(false)
        isProgressButtonEnabled = true



    }

    override fun onSkipPressed(currentFragment: Fragment) {
        super.onSkipPressed(currentFragment)
        startActivity(Intent(this@Lecturer, MainActivity::class.java))
    }

    override fun onDonePressed(currentFragment: Fragment) {
        super.onDonePressed(currentFragment)
        startActivity(Intent(this@Lecturer,MainActivity::class.java))

    }


}