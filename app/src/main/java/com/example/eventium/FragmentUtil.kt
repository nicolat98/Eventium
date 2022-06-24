package com.example.eventium

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

object FragmentUtil {
    fun refreshFragment(context : Context?){
        context?.let{
            val fragmentManager = (context as? AppCompatActivity)?.supportFragmentManager
            fragmentManager?.let {
                val currentFragment = fragmentManager.findFragmentById(R.id.nav_container)
                currentFragment?.let {
                    val fragmentTransaction = fragmentManager.beginTransaction()
                    fragmentTransaction.detach(it)
                    fragmentTransaction.attach(it)
                    fragmentTransaction.commit()
                }
            }
        }
    }
}