package tech.takenoko.screenmirror.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import tech.takenoko.screenmirror.R
import tech.takenoko.screenmirror.usecase.MirroringUsecase


class MirroringService : Service() {

    lateinit var mirroringUsecase: MirroringUsecase

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?).also { manager ->
            kotlin.runCatching {
                if (manager?.getNotificationChannel(CHANNEL_ID) == null) manager?.createNotificationChannel(
                    NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                        description = CHANNEL_DESC
                    }
                )
            }
        }

        startForeground(ID, NotificationCompat.Builder(this, CHANNEL_ID).apply {
            color = ContextCompat.getColor(applicationContext, R.color.colorNotificationBack)
            setColorized(true)
            setSmallIcon(R.mipmap.ic_launcher)
            setStyle(NotificationCompat.DecoratedCustomViewStyle())
            setContentTitle(NOTIFY_TITLE)
            setContentText(NOTIFY_TEXT)
        }.build())

        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        mirroringUsecase = MirroringUsecase(this)
        mirroringUsecase.start()
        registerReceiver(configChangeBroadcastReciver, IntentFilter("android.intent.action.CONFIGURATION_CHANGED"))
    }

    override fun onDestroy() {
        unregisterReceiver(configChangeBroadcastReciver)
        mirroringUsecase.stop()
        super.onDestroy()
    }

    private val configChangeBroadcastReciver = object: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            mirroringUsecase.restart()
        }
    }

    companion object {
        const val ID = 1
        const val CHANNEL_ID = "mirrorForeground"
        const val CHANNEL_NAME = "ミラーリング"
        const val CHANNEL_DESC = "録画するよ"
        const val NOTIFY_TITLE = "ScreenMirror"
        const val NOTIFY_TEXT = "ミラーリング中だよ"

        val start: (Context) -> Unit = {
            val intent = Intent(it, MirroringService::class.java)
            if (Build.VERSION.SDK_INT >= 26) it.startService(intent)
            else it.startService(intent)
        }

        val stop: (Context) -> Unit = {
            val intent = Intent(it, MirroringService::class.java)
            it.stopService(intent)
        }
    }
}