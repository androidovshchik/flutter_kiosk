package androidovshchik.flutter_kiosk

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.UiThread
import timber.log.Timber

class AdminReceiver : DeviceAdminReceiver() {

    @UiThread
    override fun onLockTaskModeEntering(context: Context, intent: Intent?, pkg: String?) {
        Timber.d("onLockTaskModeEntering")
        lockTaskLiveData.value = true
    }

    @UiThread
    override fun onLockTaskModeExiting(context: Context, intent: Intent?) {
        Timber.d("onLockTaskModeExiting")
        lockTaskLiveData.value = false
    }
}
