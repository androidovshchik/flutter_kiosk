package androidovshchik.flutter_kiosk

import androidx.lifecycle.MutableLiveData

const val PLUGIN_NAME = "flutter_kiosk"

const val KEY_HAS_LOCK_TASK = "has_lock_task"

const val EMPTY_CODE = ""

var hasExceptionHandler = false

var hasLockTask = false

val lockTaskLiveData = MutableLiveData<Boolean>()