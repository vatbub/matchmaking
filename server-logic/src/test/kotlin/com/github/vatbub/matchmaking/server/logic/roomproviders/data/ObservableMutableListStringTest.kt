package com.github.vatbub.matchmaking.server.logic.roomproviders.data

class ObservableMutableListStringTest : ObservableMutableListTestSuperclass<String>() {
    private var callCount = -1
    override fun getNewTestElement(): String {
        callCount++
        return "dummyValue$callCount"
    }
}