package androidovshchik.flutter_kiosk

import android.app.Activity
import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import androidovshchik.flutter_kiosk.extension.isDeviceOwner
import androidx.annotation.UiThread
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import org.jetbrains.anko.activityManager
import org.jetbrains.anko.devicePolicyManager
import org.jetbrains.anko.startService

@Suppress("unused")
class FlutterKioskPlugin : FlutterPlugin, MethodCallHandler, ActivityAware, LifecycleObserver {

    private lateinit var channel: MethodChannel

    private lateinit var context: Context

    private var activity: Activity? = null

    private var lifecycle: Lifecycle? = null

    private var hasLockTask = false

    @Suppress("DEPRECATION")
    override fun onAttachedToEngine(flutterBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterBinding.flutterEngine.dartExecutor, PLUGIN_NAME).also {
            it.setMethodCallHandler(this)
        }
        context = flutterBinding.applicationContext
    }

    override fun onAttachedToActivity(activityBinding: ActivityPluginBinding) {
        activity = activityBinding.activity
        lifecycle = (activityBinding.lifecycle as Lifecycle).also {
            it.addObserver(this)
        }
    }

    override fun onReattachedToActivityForConfigChanges(activityBinding: ActivityPluginBinding) {
        onAttachedToActivity(activityBinding)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onActivityResumed() {
        if (hasLockTask) {
            if (activity?.activityManager.lockTaskModeState == ActivityManager.LOCK_TASK_MODE_NONE) {
                startLockTask()
            }
            activity?.startLockTask()
        }
    }

    @UiThread
    override fun onMethodCall(call: MethodCall, result: Result) {
        val isDeviceOwner = context.isDeviceOwner
        if (call.method == "isDeviceOwner") {
            result.success(isDeviceOwner)
            return
        } else if (!isDeviceOwner) {
            result.error(EMPTY_CODE, "This app is not a device owner", null)
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
                    "callback" to object : ResultReceiver(Handler(Looper.getMainLooper())) {

                        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                            result.error(EMPTY_CODE, resultData?.getString("message"), resultData?.getString("details"))
                        }
                    }
                )
            }
            "startLockTask" -> {
                hasLockTask = true
                if (lifecycle?.currentState == Lifecycle.State.RESUMED) {
                    activity?.startLockTask()
                }
            }
            "stopLockTask" -> {
                hasLockTask = false
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
                dpm.setGlobalSetting(component, call.argument("setting"), call.argument("value"))
            }
            "setKeyguardDisabled" -> {
                dpm.setKeyguardDisabled(component, call.argument("disabled")!!)
            }
            "setStatusBarDisabled" -> {
                dpm.setStatusBarDisabled(component, call.argument("disabled")!!)
            }
            "setPersistentPreferredActivity" -> {
                pm.getLaunchIntentForPackage(packageName)?.component?.let {
                    pm.getLaunchIntentForPackage(packageName)?.action
                    val intentFilter = IntentFilter(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_HOME)
                        addCategory(Intent.CATEGORY_DEFAULT)
                    }
                    dpm.addPersistentPreferredActivity(component, intentFilter, it)
                }
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
        lifecycle?.removeObserver(this)
        lifecycle = null
        activity = null
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    companion object {

        const val PLUGIN_NAME = "flutter_kiosk"

        const val EMPTY_CODE = ""
    }
}
