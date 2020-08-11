@file:Suppress("unused")

package androidovshchik.flutter_kiosk.extension

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import org.jetbrains.anko.devicePolicyManager
import org.jetbrains.anko.intentFor

val Context.isDeviceOwner: Boolean
    get() = devicePolicyManager.isDeviceOwnerApp(packageName)

tailrec fun Context?.getActivity(): Activity? = when (this) {
    is Activity -> this
    else -> (this as? ContextWrapper)?.baseContext?.getActivity()
}

inline fun <reified T> Context.activityCallback(action: T.() -> Unit) {
    getActivity()?.let {
        if (it is T && !it.isFinishing) {
            action(it)
        }
    }
}

fun Context.areGranted(vararg permissions: String): Boolean {
    for (permission in permissions) {
        if (checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
    }
    return true
}

inline fun <reified T : Activity> Context.pendingActivityFor(
    requestCode: Int = 0,
    flags: Int = PendingIntent.FLAG_UPDATE_CURRENT,
    vararg params: Pair<String, Any?>
): PendingIntent =
    PendingIntent.getActivity(applicationContext, requestCode, intentFor<T>(*params), flags)

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