package androidovshchik.flutter_kiosk

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidovshchik.flutter_kiosk.extension.getBaseActivity
import org.jetbrains.anko.activityManager
import org.jetbrains.anko.newTask
import timber.log.Timber

class RebootReceiver : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent?) {
        with(context) {
            if (activityManager.getBaseActivity(packageName) != null) {
                Timber.d("After reboot app is already running")
                return
            }
            val preferences = getSharedPreferences(PLUGIN_NAME, Context.MODE_PRIVATE)
            if (preferences.getBoolean(KEY_HAS_LOCK_TASK, false)) {
                hasLockTask = true
                packageManager.getLaunchIntentForPackage(packageName)?.let {
                    startActivity(it.newTask())
                    return
                }
                Timber.w("After reboot launch component was not found")
            }
        }
    }
}