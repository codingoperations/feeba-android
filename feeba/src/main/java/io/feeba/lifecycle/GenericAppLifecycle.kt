package io.feeba.lifecycle

interface GenericAppLifecycle {
    var windowLifecycleListener: OnWindowLifecycleListener?
    var appLifecycleListener: AppLifecycleListener?
}
interface OnWindowLifecycleListener {
    fun onWindow(state: WindowState)
}
enum class WindowState {
    CREATED, OPENED, CLOSED, DESTROYED
}

interface AppLifecycleListener {
    fun onLifecycleEvent(state: AppState)
}
enum class AppState {
    CREATED, FOREGROUND, BACKGROUND
}