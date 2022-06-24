package com.example.eventium

class Partecipation {
    var eid : String? = null
    var uid : String? = null

    constructor(){}

    constructor(uid: String?, eid: String?){
        this.eid = eid
        this.uid = uid
    }

}
