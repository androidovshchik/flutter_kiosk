@file:Suppress("DEPRECATION")

package androidovshchik.flutter_kiosk.extension

import android.app.ActivityManager

fun ActivityManager.getBaseActivity(packageName: String): String? {
    for (task in getRunningTasks(Int.MAX_VALUE)) {
        task.baseActivity?.let {
            if (it.packageName == packageName) {
                return it.className
            }
        }
    }
    return null
}