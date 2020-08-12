package androidovshchik.flutter_kiosk

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.os.ResultReceiver
import android.os.UserManager
import androidovshchik.flutter_kiosk.extension.isDeviceOwner
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import org.jetbrains.anko.devicePolicyManager
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.startService

@Suppress("unused")
class FlutterKioskPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {

    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel

    private lateinit var context: Context

    @Suppress("DEPRECATION")
    override fun onAttachedToEngine(flutterBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterBinding.flutterEngine.dartExecutor, PLUGIN_NAME)
        channel.setMethodCallHandler(this)
        context = flutterBinding.applicationContext
    }

    override fun onAttachedToActivity(activityBinding: ActivityPluginBinding) {
    }

    override fun onReattachedToActivityForConfigChanges(activityBinding: ActivityPluginBinding) {
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        val isDeviceOwner = context.isDeviceOwner
        if (call.method == "isDeviceOwner") {
            result.success(isDeviceOwner)
            return
        } else if (!isDeviceOwner) {
            result.error("no_rights", null, null)
            return
        }
        val dpm = context.devicePolicyManager
        val adminComponent = ComponentName(context, AdminReceiver::class.java)
        when (call.method) {
            "enableLockTask" -> {
                dpm.setLockTaskPackages(adminComponent, if (call.argument("enable")!!) {
                    arrayOf(context.packageName)
                } else {
                    emptyArray()
                })
            }
            "setKeyguardDisabled" -> {
                dpm.setKeyguardDisabled(adminComponent, call.argument("disabled")!!)
            }
            "addUserRestrictions" -> {
                call.argument<List<String>>("keys")!!.forEach {
                    dpm.addUserRestriction(adminComponent, it)
                }
            }
            "clearUserRestrictions" -> {
                call.argument<List<String>>("keys")!!.forEach {
                    dpm.clearUserRestriction(adminComponent, it)
                }
            }
            "startLock" -> {
                if (isDeviceOwner) {
                    setRestrictions(enable)
                    deviceManager.setKeyguardDisabled(adminComponent, enable)
                    setLockTask(enable)
                    return true
                }

                if (enable) {
                    context.devicePolicyManager.setLockTaskPackages(component, arrayOf(packageName))
                } else {
                    setLockTaskPackages(component, arrayOf())
                }
            }
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

    override fun onDetachedFromActivityForConfigChanges() {
    }

    override fun onDetachedFromActivity() {
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    companion object {

        const val PLUGIN_NAME = "flutter_kiosk"
    }
}
