package com.topjohnwu.magisk.core

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import com.topjohnwu.magisk.DynAPK
import com.topjohnwu.magisk.Telemetry
import com.topjohnwu.magisk.core.utils.*
import com.topjohnwu.magisk.di.ServiceLocator
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.internal.UiThreadHandler
import com.topjohnwu.superuser.ipc.RootService
import kotlinx.coroutines.Dispatchers
import timber.log.Timber

open class App() : Application() {

    constructor(o: Any) : this() {
        val data = DynAPK.Data(o)
        // Add the root service name mapping
        data.classToComponent[RootRegistry::class.java.name] = data.rootService.name
        // Send back the actual root service class
        data.rootService = RootRegistry::class.java
        Info.stub = data
    }

    init {
        // Always log full stack trace with Timber
        Timber.plant(object : Timber.DebugTree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                super.log(priority, tag, message, t)
                val properties = HashMap<String, String>()
                properties.putAll(Info.properties)
                if (t != null) {
                    Telemetry.trackError(t, properties)
                } else if (priority >= Log.WARN) {
                    properties["message"] = message
                    Telemetry.trackError(RuntimeException(), properties)
                }
            }
        })
    }

    override fun attachBaseContext(context: Context) {
        Shell.setDefaultBuilder(Shell.Builder.create()
            .setFlags(Shell.FLAG_MOUNT_MASTER)
            .setInitializers(ShellInit::class.java)
            .setTimeout(2))
        Shell.EXECUTOR = DispatcherExecutor(Dispatchers.IO)

        // Get the actual ContextImpl
        val app: Application
        val base: Context
        if (context is Application) {
            app = context
            base = context.baseContext
        } else {
            app = this
            base = context
        }
        super.attachBaseContext(base)
        ServiceLocator.context = base

        refreshLocale()
        AppApkPath = if (isRunningAsStub) {
            DynAPK.current(base).path
        } else {
            base.packageResourcePath
        }

        base.resources.patch()
        app.registerActivityLifecycleCallbacks(ActivityTracker)
    }

    override fun onCreate() {
        super.onCreate()
        RootRegistry.bindTask = RootService.createBindTask(
            intent<RootRegistry>(),
            UiThreadHandler.executor,
            RootRegistry.Connection
        )

        Telemetry.start(this,  Info.properties.toString(), "properties")
        Telemetry.trackEvent("App onCreate", Info.properties)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (resources.configuration.diff(newConfig) != 0) {
            resources.setConfig(newConfig)
        }
        if (!isRunningAsStub)
            super.onConfigurationChanged(newConfig)
    }
}

@SuppressLint("StaticFieldLeak")
object ActivityTracker : Application.ActivityLifecycleCallbacks {

    @Volatile
    var foreground: Activity? = null

    val hasForeground get() = foreground != null

    override fun onActivityResumed(activity: Activity) {
        foreground = activity
    }

    override fun onActivityPaused(activity: Activity) {
        foreground = null
    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}
