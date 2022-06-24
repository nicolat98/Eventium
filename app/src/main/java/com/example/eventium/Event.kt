package com.example.eventium

import java.io.Serializable

class Event : Serializable{
    var name: String? = null
    var type : String? = null
    var date : String? = null
    var time : String? = null
    var location : String? = null
    var max_part : Int? = null
    var num_part : Int? = null
    var eid: String? = null
    var uid: String? = null
    var description : String? = null

    constructor(){}

    constructor(name: String?, type: String?, date: String?, time: String?, location: String?, max_part: Int?, num_part: Int?, description : String?,  eid: String?, uid:String?){
        this.name = name
        this.type = type
        this.date = date
        this.time = time
        this.location = location
        this.max_part = max_part
        this.num_part = num_part
        this.description = description
        this.eid = eid
        this.uid = uid
    }

}