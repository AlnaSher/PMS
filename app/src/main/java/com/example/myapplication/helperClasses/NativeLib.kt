package com.example.myapplication.helperClasses

class NativeLib {
    init {
        System.loadLibrary("native-lib")
    }

    external fun addUser(username: String, password: String): Boolean
    external fun isUserValid(username: String, password: String): Boolean
    external fun freeUsers()
}