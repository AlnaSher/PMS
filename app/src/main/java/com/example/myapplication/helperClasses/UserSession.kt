package com.example.myapplication.helperClasses

object UserSession {
    var currentUserId: Int? = null
        private set

    fun setUserId(userId: Int) {
        currentUserId = userId
    }

    fun clearUserId() {
        currentUserId = null
    }

    fun isUserLoggedIn(): Boolean {
        return currentUserId != null
    }
}