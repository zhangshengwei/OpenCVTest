package com.rokid.opencvtest

import android.app.Application
import android.content.Context
import com.hjq.permissions.XXPermissions

class RokidApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        //KOOM.init(this);
        XXPermissions.setScopedStorage(true);
    }

    companion object {
        var appContext: Context? = null
            private set

        fun getInstance(): Context? {
            return appContext
        }
    }


}