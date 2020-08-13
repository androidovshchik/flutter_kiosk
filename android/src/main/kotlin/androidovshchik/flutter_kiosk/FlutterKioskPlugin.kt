package androidovshchik.flutter_kiosk

import android.app.Activity
import android.content.*
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import androidovshchik.flutter_kiosk.extension.isConnected
import androidovshchik.flutter_kiosk.extension.isDeviceOwner
import androidx.annotation.UiThread
import androidx.lifecycle.*
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.embedding.engine.plugins.lifecycle.HiddenLifecycleReference
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import org.jetbrains.anko.connectivityManager
import org.jetbrains.anko.devicePolicyManager
import org.jetbrains.anko.startService
import timber.log.Timber

@Suppress("unused")
class FlutterKioskPlugin : FlutterPlugin, MethodCallHandler, ActivityAware, LifecycleObserver, Observer<Boolean> {

    private lateinit var channel: MethodChannel

    private lateinit var context: Context

    private lateinit var preferences: SharedPreferences

    private var activity: Activity? = null

    private var lifecycle: Lifecycle? = null

    @UiThread
    @Suppress("DEPRECATION")
    override fun onAttachedToEngine(flutterBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterBinding.flutterEngine.dartExecutor, PLUGIN_NAME).also {
            it.setMethodCallHandler(this)
        }
        context = flutterBinding.applicationContext
        preferences = context.getSharedPreferences(PLUGIN_NAME, Context.MODE_PRIVATE)
        val isDebug = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        if (isDebug && Timber.treeCount() <= 0) {
            Timber.plant(Timber.DebugTree())
        }
    }

    @UiThread
    override fun onAttachedToActivity(activityBinding: ActivityPluginBinding) {
        activity = activityBinding.activity
        lifecycle = (activityBinding.lifecycle as? HiddenLifecycleReference)?.lifecycle?.also {
            it.addObserver(this)
        }
        lockTaskLiveData.observeForeverFreshly(this)
    }

    override fun onReattachedToActivityForConfigChanges(activityBinding: ActivityPluginBinding) {
        onAttachedToActivity(activityBinding)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onActivityResumed() {
        if (hasLockTask) {
            activity?.startLockTask()
        }
    }

    override fun onChanged(value: Boolean) {
        if (!value && hasLockTask && lifecycle?.currentState == Lifecycle.State.RESUMED) {
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
        val dpm = context.devicePolicyManager
        val packageName = context.packageName
        val component = ComponentName(context, AdminReceiver::class.java)
        when (call.method) {
            "installUpdate" -> {
                if (!context.connectivityManager.isConnected) {
                    result.error(EMPTY_CODE, "No internet connection", null)
                    return
                }
                context.startService<UpdateService>(
                    "url" to call.argument("url"),
                    "callback" to object : ResultReceiver(Handler(Looper.getMainLooper())) {

                        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                            result.error(EMPTY_CODE, resultData?.getString("message"), resultData?.getString("details"))
                        }
                    }
                )
                return
            }
            "startLockTask" -> {
                hasLockTask = true
                if (lifecycle?.currentState == Lifecycle.State.RESUMED) {
                    activity?.startLockTask()
                }
                preferences.edit()
                    .putBoolean(KEY_HAS_LOCK_TASK, true)
                    .apply()
            }
            "stopLockTask" -> {
                hasLockTask = false
                activity?.stopLockTask()
                preferences.edit()
                    .putBoolean(KEY_HAS_LOCK_TASK, false)
                    .apply()
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
                val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
                if (launchIntent?.component == null) {
                    result.error(EMPTY_CODE, "Launch component was not found", null)
                    return
                }
                if (!launchIntent.categories.contains(Intent.CATEGORY_HOME) ||
                    !launchIntent.categories.contains(Intent.CATEGORY_DEFAULT)) {
                    result.error(EMPTY_CODE, "Incomplete categories for launch component", null)
                    return
                }
                val intentFilter = IntentFilter(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    addCategory(Intent.CATEGORY_DEFAULT)
                }
                dpm.addPersistentPreferredActivity(component, intentFilter, launchIntent.component!!)
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

    @UiThread
    override fun onDetachedFromActivity() {
        lockTaskLiveData.removeFreshObserver(this)
        lifecycle?.removeObserver(this)
        lifecycle = null
        activity = null
    }

    @UiThread
    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
}
