package androidovshchik.flutter_kiosk

import android.app.IntentService
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.ResultReceiver
import androidovshchik.flutter_kiosk.extension.isConnected
import org.jetbrains.anko.connectivityManager
import java.net.HttpURLConnection
import java.net.URL

class UpdateService : IntentService("UpdateService") {

    override fun onHandleIntent(intent: Intent?) {
        if (!connectivityManager.isConnected) {
            sendResult(intent, -1)
            return
        }
        val url = URL(intent?.getStringExtra("url"))
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "HEAD"
            connection.connect()
            val contentType = connection.contentType
            val data = connection.inputStream.bufferedReader().use { it.readText() }
            if (body.contentType()?.type != "application") {
                Timber.e("Invalid mime type of apk ${body.contentType()}")
                return Result.failure()
            }
            if (adminManager.isDeviceOwner) {
                val packageInstaller = packageManager.packageInstaller
                val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
                params.setAppPackageName(packageName)
                val sessionId = packageInstaller.createSession(params)
                packageInstaller.openSession(sessionId).use {
                    it.openWrite(packageName, 0, -1).use { output ->
                        body.byteStream().copyTo(output)
                        it.fsync(output)
                    }
                    it.commit(
                        pendingReceiverFor(Intent.ACTION_PACKAGE_ADDED, sessionId)
                            .intentSender
                    )
                }
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun sendResult(intent: Intent, resultCode: Int = 0) {
        if (intent.hasExtra("callback")) {
            intent.getParcelableExtra<ResultReceiver>("callback")
                .send(resultCode, null)
        }
    }
}