package moe.napo.triage

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.wearable.DataApi
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService

import com.google.android.gms.wearable.PutDataRequest.WEAR_URI_SCHEME

public class NotificationUpdateService : WearableListenerService() {
    private var notificationId = 1
    private val mGoogleApiClient: GoogleApiClient? = null
    private var mHealthScore = 0
    private val TAG = "DismissNotification"

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer?) {
        Log.d(TAG, "onDataChanged")
        for (dataEvent in dataEvents!!) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                if (Constants.NOTIFICATION_PATH == dataEvent.getDataItem().getUri().getPath()) {
                    val dataMapItem = DataMapItem.fromDataItem(dataEvent.getDataItem())
                    val title = dataMapItem.getDataMap().getString(Constants.NOTIFICATION_TITLE)
                    val content = dataMapItem.getDataMap().getString(Constants.NOTIFICATION_CONTENT)
                    mHealthScore = dataMapItem.getDataMap().getInt(Constants.NOTIFICATION_SCORE)
                    sendNotification(title, content)
                }
            }
        }
    }

    private fun sendNotification(title: String, content: String) {

        Log.d(TAG, "sendNotification")
        // this intent will open the activity when the user taps the "open" action on the notification
        val viewIntent = Intent(this, javaClass<MainWearActivity>())
        viewIntent.putExtra("health_score", mHealthScore)
        Log.d("NUS_HealthScore", "" + mHealthScore)
        Log.d("intent", "" + viewIntent.getIntExtra("health_score", 0))
        val pendingViewIntent = PendingIntent.getActivity(this, 0, viewIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        // this intent will be sent when the user swipes the notification to dismiss it
        val dismissIntent = Intent(Constants.ACTION_DISMISS)
        val pendingDeleteIntent = PendingIntent.getService(this, 0, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val builder = NotificationCompat.Builder(this).setSmallIcon(R.mipmap.ic_launcher).setContentTitle(title).setContentText(content).setDefaults(Notification.DEFAULT_VIBRATE).setDeleteIntent(pendingDeleteIntent).setContentIntent(pendingViewIntent).addAction(R.mipmap.ic_launcher, "Open", pendingViewIntent).setLocalOnly(true).extend(NotificationCompat.WearableExtender().setContentAction(0).setHintHideIcon(true))
        val notification = builder.build()
        val notificationManagerCompat = NotificationManagerCompat.from(this)
        notificationManagerCompat.notify(notificationId++, notification)
    }

    private fun dismissNotification() {
        DismissNotificationCommand(this).execute()
    }


    private inner class DismissNotificationCommand(context: Context) : GoogleApiClient.ConnectionCallbacks, ResultCallback<DataApi.DeleteDataItemsResult>, GoogleApiClient.OnConnectionFailedListener {

        private val mGoogleApiClient: GoogleApiClient
        private val TAG = "DismissNotification"

        init {
            mGoogleApiClient = GoogleApiClient.Builder(context).addApi(Wearable.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build()
        }

        public fun execute() {
            mGoogleApiClient.connect()
        }

        override fun onConnected(bundle: Bundle) {
            val dataItemUri = Uri.Builder().scheme(WEAR_URI_SCHEME).path(Constants.NOTIFICATION_PATH).build()
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Deleting Uri: " + dataItemUri.toString())
            }
            Wearable.DataApi.deleteDataItems(mGoogleApiClient, dataItemUri).setResultCallback(this)
        }

        override fun onConnectionSuspended(i: Int) {
            Log.d(TAG, "onConnectionSuspended")
        }

        override fun onResult(deleteDataItemsResult: DataApi.DeleteDataItemsResult) {
            if (!deleteDataItemsResult.getStatus().isSuccess()) {
                Log.e(TAG, "dismissWearableNotification(): failed to delete DataItem")
            }
            mGoogleApiClient.disconnect()
        }

        override fun onConnectionFailed(connectionResult: ConnectionResult) {
            Log.d(TAG, "onConnectionFailed")
        }
    }

    public object Constants {
        public val NOTIFICATION_PATH: String = "/notification"
        public val NOTIFICATION_TITLE: String = "title"
        public val NOTIFICATION_CONTENT: String = "content"
        public val NOTIFICATION_SCORE: String = "score"
        public val ACTION_DISMISS: String = "moe.napo.DISMISS"
    }

    companion object {
        private val TAG = "TEST"
    }
}
