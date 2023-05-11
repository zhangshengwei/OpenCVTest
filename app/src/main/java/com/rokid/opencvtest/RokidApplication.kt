package com.rokid.opencvtest

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.hjq.permissions.XXPermissions

class RokidApplication : Application() {

    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
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