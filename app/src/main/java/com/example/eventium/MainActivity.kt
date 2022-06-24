package com.example.eventium

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.eventium.nav_fragments.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(), Communicator {

    //private lateinit var currentFragment : Fragment
    private lateinit var bottom_nav : BottomNavigationView
    //private lateinit var btn_logout : Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //supportFragmentManager.beginTransaction().replace(R.id.nav_container, HomeFragment()).commit()
        bottom_nav = findViewById(R.id.bottom_navigation_bar)

        //bottom_nav.setOnItemSelectedListener(navListener)

        //btn_logout = findViewById(R.id.btn_logout)
        auth = FirebaseAuth.getInstance()

        val homeFragment =HomeFragment()
        val personalFragment = PersonalFragment()
        val createFragment = CreateFragment()
        val chatFragment = ChatFragment()
        val settingsFragment = SettingsFragment()

        //inizializzo
        makeCurrentFragment(homeFragment)

        //cambio il fragment
        bottom_nav.setOnItemSelectedListener {
            when(it.itemId){
                R.id.home -> {
                    makeCurrentFragment(homeFragment)
                }
                R.id.personal -> {
                    makeCurrentFragment(personalFragment)
                }

                R.id.create -> {
                    val intent = Intent(this, CreateActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_from_bottom, R.anim.slide_to_up)
                    finish()
                }

                R.id.chat -> {
                    makeCurrentFragment(chatFragment)
                }
                R.id.settings -> {
                    makeCurrentFragment(settingsFragment)
                }
            }
            true
        }
    }


    private fun makeCurrentFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.nav_container, fragment)
            addToBackStack(null)
            commit()
        }
    }

    override fun passEvent(event: Event) {
        val bundle = Bundle()

        bundle.putSerializable("event", event)
        bundle.putString("event_creator_uid", event.uid)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.slide_from_right, R.anim.slide_to_left, R.anim.slide_from_left, R.anim.slide_to_right)
        val newFragment = EventDetailsFragment()
        newFragment.arguments = bundle
        transaction.replace(R.id.nav_container, newFragment).addToBackStack(null).commit()
    }

    override fun passUser(user: User) {
        val bundle = Bundle()

        bundle.putSerializable("creator", user)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.slide_from_right, R.anim.slide_to_left, R.anim.slide_from_left, R.anim.slide_to_right)
        val newFragment = CreatorDetailsFragment()
        newFragment.arguments = bundle
        transaction.replace(R.id.nav_container, newFragment).addToBackStack(null).commit()
    }

}
