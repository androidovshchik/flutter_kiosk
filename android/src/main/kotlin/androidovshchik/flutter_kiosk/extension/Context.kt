@file:Suppress("unused")

package androidovshchik.flutter_kiosk.extension

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import org.jetbrains.anko.devicePolicyManager

val Context.isDeviceOwner: Boolean
    get() = devicePolicyManager.isDeviceOwnerApp(packageName)

fun Context.pendingReceiverFor(
    action: String,
    requestCode: Int = 0,
    flags: Int = PendingIntent.FLAG_UPDATE_CURRENT
): PendingIntent =
    PendingIntent.getBroadcast(applicationContext, requestCode, Intent(action), flags)