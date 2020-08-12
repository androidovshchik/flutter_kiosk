package androidovshchik.flutter_kiosk

import android.app.IntentService
import android.content.Intent
import android.content.pm.PackageInstaller.SessionParams
import android.os.ResultReceiver
import androidovshchik.flutter_kiosk.extension.isConnected
import androidovshchik.flutter_kiosk.extension.pendingReceiverFor
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.connectivityManager
import org.jetbrains.anko.getStackTraceString
import java.net.HttpURLConnection
import java.net.URL

class UpdateService : IntentService("UpdateService") {

    override fun onHandleIntent(intent: Intent?) {
        val url = intent?.getStringExtra("url")
        if (!url.isNullOrBlank()) {
            reportError(intent, "message" to "Invalid url")
            return
        }
        if (!connectivityManager.isConnected) {
            reportError(intent, "message" to "No internet connection")
            return
        }
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            val packageInstaller = packageManager.packageInstaller
            val params = SessionParams(SessionParams.MODE_FULL_INSTALL)
            params.setAppPackageName(packageName)
            val sessionId = packageInstaller.createSession(params)
            packageInstaller.openSession(sessionId).use { session ->
                session.openWrite(packageName, 0, -1).use { output ->
                    connection.inputStream.use { input ->
                        input.copyTo(output)
                    }
                    session.fsync(output)
                }
                session.commit(
                    pendingReceiverFor(Intent.ACTION_PACKAGE_ADDED, sessionId)
                        .intentSender
                )
            }
            // normally app will be terminated here
        } catch (e: Throwable) {
            e.printStackTrace()
            reportError(intent, "message" to e.message, "details" to e.getStackTraceString())
        }
    }

    @Suppress("DEPRECATION")
    private fun reportError(intent: Intent?, vararg params: Pair<String, Any?>) {
        intent?.getParcelableExtra<ResultReceiver>("callback")
            ?.send(-1, bundleOf(*params))
    }
}