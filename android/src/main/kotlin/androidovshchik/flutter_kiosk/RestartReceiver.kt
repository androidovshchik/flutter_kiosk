package androidovshchik.flutter_kiosk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidovshchik.flutter_kiosk.extension.getBaseActivity
import org.jetbrains.anko.activityManager
import org.jetbrains.anko.newTask
import timber.log.Timber

class RestartReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        with(context) {
            if (activityManager.getBaseActivity(packageName) != null) {
                Timber.d("After restart app is already running")
                return
            }
            packageManager.getLaunchIntentForPackage(packageName)?.let {
                startActivity(it.newTask())
                return
            }
            Timber.w("After restart launch component was not found")
        }
    }
}
