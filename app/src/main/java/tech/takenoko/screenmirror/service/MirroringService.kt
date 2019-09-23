package tech.takenoko.screenmirror.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
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

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) == null) manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                description = CHANNEL_DESC
            })

        remoteViews = this.createNotificationView()
        val customNotification = NotificationCompat.Builder(this, CHANNEL_ID).apply {
            color = ContextCompat.getColor(applicationContext, R.color.colorNotificationBack)
            setColorized(true)
            setSmallIcon(R.mipmap.ic_launcher)
            setStyle(NotificationCompat.DecoratedCustomViewStyle())
            setCustomContentView(remoteViews)
            setCustomBigContentView(remoteViews)
        }.build()
        startForeground(ID,customNotification)

        return START_NOT_STICKY
    }

    override fun onCreate() {
        MLog.info(TAG, "onCreate")

        super.onCreate()
        mirroringUsecase = MirroringUsecase(this)
        mirroringUsecase.start()
    }

    override fun onDestroy() {
        MLog.info(TAG, "onDestroy")

        mirroringUsecase.stop()
        super.onDestroy()
    }

    private fun createNotificationView(): RemoteViews {
        return RemoteViews(packageName, R.layout.notification_layout) .apply {
            setOnClickPendingIntent(R.id.notification_close_button, stopPendingIntent(this@MirroringService))
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
        const val CLOSE_BTN_INTENT = ".service.MirroringService.CLOSE_BTN_INTENT"

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
    }
}