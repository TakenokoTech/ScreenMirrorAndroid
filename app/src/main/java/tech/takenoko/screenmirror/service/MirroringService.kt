package tech.takenoko.screenmirror.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.*
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import tech.takenoko.screenmirror.MainActivity
import tech.takenoko.screenmirror.R
import tech.takenoko.screenmirror.usecase.MirroringUsecase
import tech.takenoko.screenmirror.utils.MLog


class MirroringService : Service() {

    lateinit var mirroringUsecase: MirroringUsecase
    lateinit var remoteViews: RemoteViews

    override fun onBind(intent: Intent?): IBinder? {
        MLog.info(TAG, "onBind")
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MLog.info(TAG, "onStartCommand")

        if(CLOSE_BTN_INTENT == intent?.action) {
            stopSelf()
            return START_NOT_STICKY
        }

        if(OPEN_BTN_INTENT == intent?.action) {
            startActivity(Intent(applicationContext, MainActivity::class.java).setFlags(FLAG_ACTIVITY_SINGLE_TOP or FLAG_ACTIVITY_NEW_TASK))
            return START_NOT_STICKY
        }

        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).also { manager ->
            if (manager.getNotificationChannel(CHANNEL_ID) == null) manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                    description = CHANNEL_DESC
                }
            )
        }

        startForeground(ID, NotificationCompat.Builder(this, CHANNEL_ID).apply {
            remoteViews = this@MirroringService.createNotificationView()
            color = ContextCompat.getColor(applicationContext, R.color.colorNotificationBack)
            setColorized(true)
            setSmallIcon(R.mipmap.ic_launcher)
            setStyle(NotificationCompat.DecoratedCustomViewStyle())
            setCustomContentView(remoteViews)
            setCustomBigContentView(remoteViews)
            setContentTitle(NOTIFY_TITLE)
        }.build())

        return START_NOT_STICKY
    }

    override fun onCreate() {
        MLog.info(TAG, "onCreate")
        super.onCreate()
        mirroringUsecase = MirroringUsecase(this)
        mirroringUsecase.start()
        registerReceiver(configChangeBroadcastReciver, IntentFilter("android.intent.action.CONFIGURATION_CHANGED"))
        registerReceiver(configChangeBroadcastReciver, IntentFilter(RESTART_BTN_INTENT))
    }

    override fun onDestroy() {
        MLog.info(TAG, "onDestroy")
        unregisterReceiver(configChangeBroadcastReciver)
        mirroringUsecase.stop()
        super.onDestroy()
    }

    private fun createNotificationView(): RemoteViews {
        return RemoteViews(packageName, R.layout.notification_layout) .apply {
            setOnClickPendingIntent(R.id.notificationCloseButton, stopPendingIntent(this@MirroringService))
            setOnClickPendingIntent(R.id.notificationLayout, openPendingIntent(this@MirroringService))
        }
    }

    private val configChangeBroadcastReciver = object: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            MLog.info(TAG, "onReceive")
            mirroringUsecase.restart()
        }
    }

    companion object {
        val TAG: String = MirroringService::class.java.simpleName
        const val ID = 1
        const val CHANNEL_ID = "mirrorForeground"
        const val CHANNEL_NAME = "ミラーリング"
        const val CHANNEL_DESC = "録画するよ"
        const val NOTIFY_TITLE = "ScreenMirror"
        const val NOTIFY_TEXT = "ミラーリング中だよ"
        const val CLOSE_BTN_INTENT = "tech.takenoko.screenmirror.service.MirroringService.CLOSE_BTN_INTENT"
        const val OPEN_BTN_INTENT = "tech.takenoko.screenmirror.service.MirroringService.OPEN_BTN_INTENT"
        const val RESTART_BTN_INTENT = "tech.takenoko.screenmirror.service.MirroringService.RESTART_BTN_INTENT"

        val start: (Context) -> Unit = {
            val intent = Intent(it, MirroringService::class.java)
            if (Build.VERSION.SDK_INT >= 26) it.startService(intent)
            else it.startService(intent)
        }

        val stop: (Context) -> Unit = {
            val intent = Intent(it, MirroringService::class.java)
            it.stopService(intent)
        }

        val stopPendingIntent: (Context) ->  PendingIntent = {
            val intent = Intent(it, MirroringService::class.java).apply { action = CLOSE_BTN_INTENT }
            PendingIntent.getService(it, 0, intent, 0)
        }

        val openPendingIntent: (Context) ->  PendingIntent = {
            val intent = Intent(it, MirroringService::class.java).apply { action = OPEN_BTN_INTENT }
            PendingIntent.getService(it, 0, intent, 0)
        }
    }
}