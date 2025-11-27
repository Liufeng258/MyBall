package com.liufeng.ballfight

import android.app.Application

class MyBallApp : Application() {
    override fun onCreate() {
        super.onCreate()
        UserProfile.initialize(this)
    }
}