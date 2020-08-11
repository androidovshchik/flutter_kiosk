package androidovshchik.flutter_kiosk

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent

class AdminReceiver : DeviceAdminReceiver() {

    override fun onLockTaskModeEntering(context: Context, intent: Intent?, pkg: String?) {
        println("onLockTaskModeEntering")
    }

    override fun onLockTaskModeExiting(context: Context, intent: Intent?) {
        println("onLockTaskModeExiting")
    }
}
