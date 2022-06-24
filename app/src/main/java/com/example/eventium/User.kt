package com.example.eventium

import android.provider.ContactsContract
import java.io.Serializable

class User :Serializable {
    var username: String? = null
    var email : String? = null
    var uid: String? = null
    var favorites : ArrayList<String>? = null
    var stars : Double? = null
    var max_stars : Double? = null

    constructor(){}

    constructor(username: String?, email: String?, uid: String?, favorites : ArrayList<String>?){
        this.username = username
        this.email = email
        this.uid = uid
        this.favorites = favorites
        this.max_stars = 5.0
        this.stars = 0.0
    }

}