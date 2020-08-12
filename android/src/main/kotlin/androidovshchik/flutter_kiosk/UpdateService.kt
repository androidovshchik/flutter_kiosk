package androidovshchik.flutter_kiosk

import android.app.IntentService
import android.content.Intent
import android.content.pm.PackageInstaller.SessionParams
import android.os.ResultReceiver
import androidovshchik.flutter_kiosk.extension.isConnected
import androidovshchik.flutter_kiosk.extension.isDeviceOwner
import androidovshchik.flutter_kiosk.extension.pendingReceiverFor
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.connectivityManager
import java.net.HttpURLConnection
import java.net.URL

class UpdateService : IntentService("UpdateService") {

    override fun onHandleIntent(intent: Intent?) {
        if (!isDeviceOwner) {
            sendResult(intent, -1, "code" to "no_rights")
            return
        }
        if (!connectivityManager.isConnected) {
            sendResult(intent, -1, "code" to "no_internet")
            return
        }
        try {
            val url = intent?.getStringExtra("url")
            check(!url.isNullOrBlank())
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.connect()
            if (connection.contentType?.startsWith("application") != true) {
                throw Exception("Invalid mime type of apk \"${connection.contentType}\"")
            }
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
            sendResult(intent, 0)
        } catch (e: Throwable) {
            e.printStackTrace()
            sendResult(intent, -1, "code" to "exception", "message" to e.message, "details" to e.toString())
        }
    }

    @Suppress("DEPRECATION")
    private fun sendResult(intent: Intent?, resultCode: Int, vararg params: Pair<String, Any?>) {
        if (intent?.hasExtra("callback") == true) {
            intent.getParcelableExtra<ResultReceiver>("callback")
                .send(resultCode, bundleOf(*params))
        }
    }
}