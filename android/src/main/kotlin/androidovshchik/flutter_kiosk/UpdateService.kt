package androidovshchik.flutter_kiosk

import android.app.IntentService
import android.content.Intent
import android.content.pm.PackageInstaller.SessionParams
import android.os.Bundle
import android.os.ResultReceiver
import androidovshchik.flutter_kiosk.extension.pendingReceiverFor
import org.jetbrains.anko.getStackTraceString
import timber.log.Timber
import java.net.HttpURLConnection
import java.net.URL

class UpdateService : IntentService("UpdateService") {

    override fun onHandleIntent(intent: Intent?) {
        try {
            val url = intent?.getStringExtra("url")
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
            Timber.e(e)
            intent?.getParcelableExtra<ResultReceiver>("callback")
                ?.send(-1, Bundle().apply {
                    putString("message", e.message)
                    putString("details", e.getStackTraceString())
                })
        }
    }
}