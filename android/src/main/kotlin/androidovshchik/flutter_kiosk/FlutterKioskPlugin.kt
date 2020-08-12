package androidovshchik.flutter_kiosk

import android.content.Context
import android.os.Bundle
import android.os.ResultReceiver
import androidovshchik.flutter_kiosk.extension.isDeviceOwner
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.startService

@Suppress("unused")
class FlutterKioskPlugin : FlutterPlugin, MethodCallHandler {

    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel

    private lateinit var context: Context

    @Suppress("DEPRECATION")
    override fun onAttachedToEngine(pluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(pluginBinding.flutterEngine.dartExecutor, PLUGIN_NAME)
        channel.setMethodCallHandler(this)
        context = pluginBinding.applicationContext
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "isDeviceOwner" -> result.success(context.isDeviceOwner)
            "installUpdate" -> {
                context.startService<UpdateService>(
                    "url" to call.argument("url"),
                    "callback" to object : ResultReceiver(null) {

                        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                            context.runOnUiThread {
                                if (resultCode == 0) {
                                    result.success(null)
                                } else {
                                    result.error(
                                        resultData?.getString("code") ?: "unknown",
                                        resultData?.getString("message"),
                                        resultData?.getString("details")
                                    )
                                }
                            }
                        }
                    }
                )
            }
            else -> result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    companion object {

        const val PLUGIN_NAME = "flutter_kiosk"
    }
}
