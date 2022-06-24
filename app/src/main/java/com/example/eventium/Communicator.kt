package com.example.eventium

interface Communicator {
    fun passEvent(event: Event)

    fun passUser(user: User)
}