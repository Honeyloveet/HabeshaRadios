package com.sampro.habesharadios.utils

import android.app.ActivityManager
import android.content.Context

fun Context.isServiceRunning(serviceClassName: String): Boolean {
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager

    return activityManager?.getRunningServices(Integer.MAX_VALUE)?.any { it.service.className == serviceClassName } ?: false
}