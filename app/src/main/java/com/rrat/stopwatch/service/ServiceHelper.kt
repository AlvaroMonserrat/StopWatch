package com.rrat.stopwatch.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.rrat.stopwatch.MainActivity
import com.rrat.stopwatch.util.Constants.CANCEL_REQUEST_CODE
import com.rrat.stopwatch.util.Constants.CLICK_REQUEST_CODE
import com.rrat.stopwatch.util.Constants.RESUME_REQUEST_CODE
import com.rrat.stopwatch.util.Constants.STOPWATCH_STATE
import com.rrat.stopwatch.util.Constants.STOP_REQUEST_CODE


object ServiceHelper {
    private val flag =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0

    fun clickPendingIntent(context: Context): PendingIntent{
        val clickIntent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(context, CLICK_REQUEST_CODE, clickIntent, flag)
    }

    fun stopPendingIntent(context: Context): PendingIntent{
        return servicePendingIntent(context, StopWatchState.Stopped.name, STOP_REQUEST_CODE)
    }

    fun resumePendingIntent(context: Context): PendingIntent{
        return servicePendingIntent(context, StopWatchState.Started.name, RESUME_REQUEST_CODE)
    }

    fun cancelPendingIntent(context: Context): PendingIntent{
        return servicePendingIntent(context, StopWatchState.Canceled.name, CANCEL_REQUEST_CODE)
    }

    private fun servicePendingIntent(context: Context, serviceName: String, requestCode: Int): PendingIntent{
        val intent = Intent(context, StopWatchService::class.java)
        intent.putExtra(STOPWATCH_STATE, serviceName)
        return PendingIntent.getService(context, requestCode, intent, flag)
    }

    fun triggerForegroundService(context: Context, action: String){
        val intent = Intent(context, StopWatchService::class.java)
        intent.action = action
        context.startService(intent)
    }

}