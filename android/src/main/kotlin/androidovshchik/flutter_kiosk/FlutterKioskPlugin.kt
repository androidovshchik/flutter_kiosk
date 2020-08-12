package androidovshchik.flutter_kiosk

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
        val pm = context.packageManager
        val dpm = context.devicePolicyManager
        val packageName = context.packageName
        val component = ComponentName(context, AdminReceiver::class.java)
        when (call.method) {
            "installUpdate" -> {
                context.startService<UpdateService>(
                    "url" to call.argument("url"),
                    "callback" to object : ResultReceiver(null) {

                        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                            activity?.runOnUiThread {
                                result.error(
                                    resultData?.getString("code") ?: "unknown",
                                    resultData?.getString("message"),
                                    resultData?.getString("details")
                                )
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
                dpm.setLockTaskPackages(component, if (call.argument("enable")!!) {
                    arrayOf(packageName)
                } else {
                    emptyArray()
                })
            }
            "setGlobalSetting" -> {
                dpm.setGlobalSetting(
                    component,
                    call.argument("setting"),
                    call.argument("value")
                )
            }
            "setKeyguardDisabled" -> {
                dpm.setKeyguardDisabled(component, call.argument("disabled")!!)
            }
            "setStatusBarDisabled" -> {
                dpm.setStatusBarDisabled(component, call.argument("disabled")!!)
            }
            "setPersistentPreferredActivity" -> {
                val intentFilter = IntentFilter(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    addCategory(Intent.CATEGORY_DEFAULT)
                }
                val launchIntent = pm.getLaunchIntentForPackage(packageName)
                dpm.addPersistentPreferredActivity(component, intentFilter, launchIntent.component)
            }
            "clearPersistentPreferredActivities" -> {
                dpm.clearPackagePersistentPreferredActivities(component, packageName)
            }
            "addUserRestrictions" -> {
                call.argument<List<String>>("keys")!!.forEach {
                    dpm.addUserRestriction(component, it)
                }
            }
            "clearUserRestrictions" -> {
                call.argument<List<String>>("keys")!!.forEach {
                    dpm.clearUserRestriction(component, it)
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
