package com.sampro.habesharadios.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class RequestPermissions(private val context: Context, private val activity: AppCompatActivity) {

    fun hasWriteExternalStoragePermission() =
        ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    fun hasReadExternalStoragePermission() =
        ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    fun hasRecordAudioPermission() =
        ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    fun requestWritePermission() {
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 5)
    }

    fun requestPermissions() {
        val permissionToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (!hasWriteExternalStoragePermission()) {
                permissionToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        if (!hasReadExternalStoragePermission()) {
            permissionToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (!hasRecordAudioPermission()) {
            permissionToRequest.add(Manifest.permission.RECORD_AUDIO)
        }

        if (permissionToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, permissionToRequest.toTypedArray(), 0)
        }
    }

}