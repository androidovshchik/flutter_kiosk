package androidx.lifecycle

import java.lang.ref.WeakReference

fun <T> LiveData<T>.observeFreshly(owner: LifecycleOwner, observer: Observer<in T>) {
    observe(owner, FreshObserver<T>(observer, WeakReference(this), version))
}

fun <T> LiveData<T>.observeForeverFreshly(observer: Observer<in T>) {
    observeForever(FreshObserver<T>(observer, WeakReference(this), version))
}

fun <T> LiveData<T>.removeFreshObserver(observer: Observer<in T>) {
    removeObserver(FreshObserver<T>(observer, WeakReference(this), 0))
}

class FreshObserver<T>(
    private val delegate: Observer<in T>,
    private val liveData: WeakReference<LiveData<*>>,
    private val sinceVersion: Int
) : Observer<T> {

    override fun onChanged(t: T) {
        liveData.get()?.let {
            if (it.version > sinceVersion) {
                delegate.onChanged(t)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (delegate != (other as FreshObserver<*>).delegate) return false
        return true
    }

    override fun hashCode() = delegate.hashCode()
}