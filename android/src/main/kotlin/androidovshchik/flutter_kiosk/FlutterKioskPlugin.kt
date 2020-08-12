package androidovshchik.flutter_kiosk

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.os.ResultReceiver
import androidovshchik.flutter_kiosk.extension.isDeviceOwner
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import org.jetbrains.anko.devicePolicyManager
import org.jetbrains.anko.startService

@Suppress("unused")
class FlutterKioskPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {

    private lateinit var channel: MethodChannel

    private var activity: Activity? = null

    private lateinit var context: Context

    @Suppress("DEPRECATION")
    override fun onAttachedToEngine(flutterBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterBinding.flutterEngine.dartExecutor, PLUGIN_NAME)
        channel.setMethodCallHandler(this)
        context = flutterBinding.applicationContext
    }

    override fun onAttachedToActivity(activityBinding: ActivityPluginBinding) {
        activity = activityBinding.activity
    }

    override fun onReattachedToActivityForConfigChanges(activityBinding: ActivityPluginBinding) {
        onAttachedToActivity(activityBinding)
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
            "installUpdate" -> {
                context.startService<UpdateService>(
                    "url" to call.argument("url"),
                    "callback" to object : ResultReceiver(null) {

                        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                            activity?.runOnUiThread {
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
            "startLockTask" -> {
                activity?.startLockTask()
            }
            "stopLockTask" -> {
                activity?.stopLockTask()
            }
            "toggleLockTask" -> {
                dpm.setLockTaskPackages(adminComponent, if (call.argument("enable")!!) {
                    arrayOf(context.packageName)
                } else {
                    emptyArray()
                })
            }
            "setGlobalSetting" -> {
                dpm.setGlobalSetting(adminComponent, call.argument("setting"), call.argument("value"))
            }
            "setKeyguardDisabled" -> {
                dpm.setKeyguardDisabled(adminComponent, call.argument("disabled")!!)
            }
            "setStatusBarDisabled" -> {
                dpm.setStatusBarDisabled(adminComponent, call.argument("disabled")!!)
            }
            "addPersistentPreferredActivity" -> {
                dpm.addPersistentPreferredActivity(adminComponent, ,)
            }
            "clearPersistentPreferredActivities" -> {
                dpm.clearPackagePersistentPreferredActivities(adminComponent, context.packageName)
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
            else -> {
                result.notImplemented()
                return
            }
        }
        result.success(null)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity()
    }

    override fun onDetachedFromActivity() {
        activity = null
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    companion object {

        const val PLUGIN_NAME = "flutter_kiosk"
    }
}
