package tech.takenoko.screenmirror.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import tech.takenoko.screenmirror.R
import tech.takenoko.screenmirror.usecase.MirroringUsecase
import tech.takenoko.screenmirror.utils.MLog

class MirroringService : Service() {

    lateinit var mirroringUsecase: MirroringUsecase

    override fun onBind(intent: Intent?): IBinder? {
        MLog.info(TAG, "onBind")
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MLog.info(TAG, "onStartCommand")

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) == null) manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                description = CHANNEL_DESC
            })

        startForeground(ID,
            Notification.Builder(this, CHANNEL_ID).apply {
                setContentTitle(NOTIFY_TITLE)
                setContentText(NOTIFY_TEXT)
                setSmallIcon(R.mipmap.ic_launcher)
            }.build())

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

    companion object {
        val TAG: String = MirroringService::class.java.simpleName
        const val ID = 1
        const val CHANNEL_ID = "mirrorForeground"
        const val CHANNEL_NAME = "ミラーリング"
        const val CHANNEL_DESC = "録画するよ"
        const val NOTIFY_TITLE = "ScreenMirror"
        const val NOTIFY_TEXT = "ミラーリング中だよ"

        val start: (Activity) -> Unit = { activity ->
            val intent = Intent(activity, MirroringService::class.java)
            if (Build.VERSION.SDK_INT >= 26) activity.startService(intent)
            else activity.startService(intent)
        }

        val stop: (Activity) -> Unit = { activity ->
            val intent = Intent(activity, MirroringService::class.java)
            activity.stopService(intent)
        }
    }
}