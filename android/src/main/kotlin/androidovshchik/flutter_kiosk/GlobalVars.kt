package androidovshchik.flutter_kiosk

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData

const val PLUGIN_NAME = "flutter_kiosk"

const val KEY_HAS_LOCK_TASK = "has_lock_task"

const val EMPTY_CODE = ""

var hasLockTask = false

val mainHandler = Handler(Looper.getMainLooper())

val lockTaskLiveData = MutableLiveData<Boolean>()