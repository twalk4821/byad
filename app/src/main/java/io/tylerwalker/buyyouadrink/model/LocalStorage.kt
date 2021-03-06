package io.tylerwalker.buyyouadrink.model

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import io.tylerwalker.buyyouadrink.activity.map.InvitationViewModel.Companion.logTag

class LocalStorage(val context: Context) {
    private val sharedPreferences: SharedPreferences by lazy {
        val prefs = context.getSharedPreferences("io.tylerwalker.buyyouadrink", Context.MODE_PRIVATE)
        prefs
    }

    private val sharedPreferencesCurrentUserKey = "current_user"
    private val sharedPreferencesFirstRunKey = "first_run"
    private val sharedPreferencesRememberMeKey = "remember_me"
    private val sharedPreferencesSavedEmailKey = "saved_email_key"
    private val sharedPreferencesSavedPasswordKey = "saved_password_key"
    private val sharedPreferencesCurrentUserIDKey = "current_uid"

    fun getCurrentUser(): User? {
        return Gson().fromJson(sharedPreferences.getString(sharedPreferencesCurrentUserKey, null), User::class.java)

    }
    fun putCurrentUser(user: User?) {
        Log.d("LocalStorage", "put user: $user")
        val json = Gson().toJson(user)
        sharedPreferences.edit().putString(sharedPreferencesCurrentUserKey, json).apply()
        Log.d("LocalStorage", "put user success: $json")
    }
    fun clearCurrentUser() = sharedPreferences.edit().remove(sharedPreferencesCurrentUserKey)
    fun isFirstRun(): Boolean = sharedPreferences.getBoolean(sharedPreferencesFirstRunKey, false)
    fun setFirstRun(isFirstRun: Boolean = false) = sharedPreferences.edit().putBoolean(sharedPreferencesFirstRunKey, isFirstRun).apply()

    fun shouldRememberMe(rememberMe: Boolean = true) = sharedPreferences.edit().putBoolean(sharedPreferencesRememberMeKey, rememberMe).apply()
    fun rememberMe(): Boolean? = sharedPreferences.getBoolean(sharedPreferencesRememberMeKey, true)

    fun getSavedCredentials(): Credentials? {
        sharedPreferences.getString(sharedPreferencesSavedEmailKey, null)?.let { email ->
            sharedPreferences.getString(sharedPreferencesSavedPasswordKey, null)?.let { password ->
                val creds = Credentials(email, password)
                Log.d(logTag, "getSavedCredentials() $creds")

                return creds
            }
        }

        Log.d(logTag, "getSavedCredentials() null")

        return null
    }

    fun putCredentials(credentials: Credentials) {
        sharedPreferences.edit().putString(sharedPreferencesSavedEmailKey, credentials.email).apply()
        sharedPreferences.edit().putString(sharedPreferencesSavedPasswordKey, credentials.password).apply()
    }

    fun clearCredentials() {
        sharedPreferences.edit().apply {
            remove(sharedPreferencesSavedEmailKey)
            remove(sharedPreferencesSavedPasswordKey)
        }.apply()
    }

    fun getCurrentUid() = sharedPreferences.getString(sharedPreferencesCurrentUserIDKey, null)
    fun putCurrentUid(uid: String) = sharedPreferences.edit().putString(sharedPreferencesCurrentUserIDKey, uid).commit()

    fun putString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }
    fun getString(key: String): String? = sharedPreferences.getString(key, null)
}