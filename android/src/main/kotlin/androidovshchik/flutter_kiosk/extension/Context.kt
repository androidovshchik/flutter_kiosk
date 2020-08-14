@file:Suppress("unused")

package androidovshchik.flutter_kiosk.extension

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import org.jetbrains.anko.alarmManager
import org.jetbrains.anko.devicePolicyManager
import org.jetbrains.anko.intentFor

val Context.isDeviceOwner: Boolean
    get() = devicePolicyManager.isDeviceOwnerApp(packageName)

inline fun <reified T : BroadcastReceiver> Context.pendingReceiverFor(
    requestCode: Int = 0,
    flags: Int = PendingIntent.FLAG_UPDATE_CURRENT,
    vararg params: Pair<String, Any?>
): PendingIntent =
    PendingIntent.getBroadcast(applicationContext, requestCode, intentFor<T>(*params), flags)

fun Context.pendingReceiverFor(
    action: String,
    requestCode: Int = 0,
    flags: Int = PendingIntent.FLAG_UPDATE_CURRENT
): PendingIntent =
    PendingIntent.getBroadcast(applicationContext, requestCode, Intent(action), flags)

inline fun <reified T : BroadcastReceiver> Context.createAlarm(interval: Long, requestCode: Int = 0) {
    when {
        isMarshmallowPlus() -> alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + interval, pendingReceiverFor<T>(requestCode))
        else -> alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + interval, pendingReceiverFor<T>(requestCode))
    }
}

inline fun <reified T : BroadcastReceiver> Context.cancelAlarm(requestCode: Int = 0) {
    alarmManager.cancel(pendingReceiverFor<T>(requestCode))
}