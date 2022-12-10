package com.rrat.stopwatch.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import com.rrat.stopwatch.util.Constants.ACTION_SERVICE_CANCEL
import com.rrat.stopwatch.util.Constants.ACTION_SERVICE_START
import com.rrat.stopwatch.util.Constants.ACTION_SERVICE_STOP
import com.rrat.stopwatch.util.Constants.NOTIFICATION_CHANNEL_ID
import com.rrat.stopwatch.util.Constants.NOTIFICATION_CHANNEL_NAME
import com.rrat.stopwatch.util.Constants.NOTIFICATION_ID
import com.rrat.stopwatch.util.Constants.STOPWATCH_STATE
import com.rrat.stopwatch.util.formatTime
import com.rrat.stopwatch.util.pad
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class StopWatchService : Service() {

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var notificationBuilder: NotificationCompat.Builder

    private val binder = StopWatchBinder()


    private var duration: Duration = Duration.ZERO
    private lateinit var timer: Timer

    var seconds = mutableStateOf("00")
        private set

    var minutes = mutableStateOf("00")
        private set

    var hours = mutableStateOf("00")
        private set

    var currentState = mutableStateOf(StopWatchState.Idle)
        private set

    override fun onBind(intent: Intent?): IBinder = binder


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when(intent?.getStringExtra(STOPWATCH_STATE)){
            StopWatchState.Started.name->{
                setStopButton()
                startForegroundService()
                startStopWatch { h, m, s -> updateNotification(h, m, s)}
            }

            StopWatchState.Stopped.name->{
                stopStopWatch()
                setResumeButton()
            }

            StopWatchState.Canceled.name->{
                stopStopWatch()
                cancelStopWatch()
                stopForegroundService()
            }
        }

        intent?.action.let {
            when(it){
                ACTION_SERVICE_START->{
                    setStopButton()
                    startForegroundService()
                    startStopWatch { h, m, s -> updateNotification(h, m, s)}
                }
                ACTION_SERVICE_STOP->{
                    stopStopWatch()
                    setResumeButton()
                }
                ACTION_SERVICE_CANCEL->{
                    stopStopWatch()
                    cancelStopWatch()
                    stopForegroundService()
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }


    private fun startStopWatch(
        onTick: (h: String, m: String, s: String)->Unit
    ){
        currentState.value = StopWatchState.Started
        timer = fixedRateTimer(initialDelay = 1000L, period = 1000L){
            duration = duration.plus(1.seconds)
            updateTimeUnits()
            onTick(hours.value, minutes.value, seconds.value)
        }
    }

    private fun stopStopWatch(){
        if(this::timer.isInitialized){
            timer.cancel()
        }
        currentState.value = StopWatchState.Stopped
    }

    private fun cancelStopWatch(){
        duration = Duration.ZERO
        currentState.value = StopWatchState.Idle
        updateTimeUnits()
    }

    private fun updateTimeUnits(){
        duration.toComponents{ hours, minutes, seconds, _->
            this@StopWatchService.hours.value = hours.toInt().pad()
            this@StopWatchService.minutes.value = minutes.pad()
            this@StopWatchService.seconds.value = seconds.pad()
        }
    }

    private fun startForegroundService(){
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun stopForegroundService(){
        notificationManager.cancel(NOTIFICATION_ID)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

    }

    private fun setStopButton(){
        notificationBuilder.mActions.removeAt(0)
        notificationBuilder.mActions.add(
            0,
            NotificationCompat.Action(
                0,
                "Stop",
                ServiceHelper.stopPendingIntent(this)
            )
        )
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun setResumeButton(){
        notificationBuilder.mActions.removeAt(0)
        notificationBuilder.mActions.add(
            0,
            NotificationCompat.Action(
                0,
                "Resume",
                ServiceHelper.resumePendingIntent(this)
            )
        )
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun updateNotification(hours: String, minutes:String, seconds: String){
        notificationManager.notify(
            NOTIFICATION_ID,
            notificationBuilder.setContentText(
                formatTime(hours = hours, minutes = minutes, seconds = seconds)
            ).build()
        )
    }

    inner class StopWatchBinder : Binder(){
        fun getService(): StopWatchService = this@StopWatchService
    }
}

enum class StopWatchState{
    Idle,
    Started,
    Stopped,
    Canceled
}