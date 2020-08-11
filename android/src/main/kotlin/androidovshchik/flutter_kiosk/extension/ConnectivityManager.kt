@file:Suppress("unused")

package androidovshchik.flutter_kiosk.extension

import android.net.ConnectivityManager

val ConnectivityManager.isConnected: Boolean
    get() = activeNetworkInfo?.isConnected == true