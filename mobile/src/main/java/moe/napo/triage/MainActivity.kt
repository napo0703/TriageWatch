package moe.napo.triage

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.wearable.DataApi
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

public class MainActivity : AppCompatActivity() {

    var mRateButton1: TextView? = null
    var mRateButton2: TextView? = null
    var mRateButton3: TextView? = null
    var mRateButton4: TextView? = null
    var mRateButton5: TextView? = null
    var mHealthPoint = 0
    var mNextButton: Button? = null
    var mGoogleApiClient: GoogleApiClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mRateButton1 = findViewById(R.id.activity_main_star_1) as TextView?
        mRateButton1!!.setOnClickListener{ onItemClick(mRateButton1!!) }
        mRateButton2 = findViewById(R.id.activity_main_star_2) as TextView?
        mRateButton2!!.setOnClickListener{ onItemClick(mRateButton2!!) }
        mRateButton3 = findViewById(R.id.activity_main_star_3) as TextView?
        mRateButton3!!.setOnClickListener{ onItemClick(mRateButton3!!) }
        mRateButton4 = findViewById(R.id.activity_main_star_4) as TextView?
        mRateButton4!!.setOnClickListener{ onItemClick(mRateButton4!!) }
        mRateButton5 = findViewById(R.id.activity_main_star_5) as TextView?
        mRateButton5!!.setOnClickListener{ onItemClick(mRateButton5!!) }
        mNextButton = findViewById(R.id.activity_main_next_button) as Button?
        mNextButton!!.setOnClickListener { sendNotification(mHealthPoint) }

        mGoogleApiClient = GoogleApiClient.Builder(this).addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
            override fun onConnected(connectionHint: Bundle?) {
                Log.d(TAG, "onConnected")
            }
            override fun onConnectionSuspended(cause: Int) {
                Log.d(TAG, "onConnectionSuspended")
            }
        }).addOnConnectionFailedListener(object : GoogleApiClient.OnConnectionFailedListener {
            override fun onConnectionFailed(result: ConnectionResult) {
                Log.d(TAG, "onConnectionFailed")
            }
        }).addApi(Wearable.API).build()
    }

    override fun onStart() {
        super.onStart()
        mGoogleApiClient!!.connect()
    }

    override fun onStop() {
        super.onStop()
        mGoogleApiClient!!.disconnect()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    fun onItemClick(view: TextView) {
        mHealthPoint =  when (view.tag) {
            "failure" -> 0
            "bad" -> 25
            "fair" -> 50
            "good" -> 75
            "excellent" -> 100
            else -> 0
        }
        when (mHealthPoint) {
            0 -> {
                mRateButton1!!.text = "★"
                mRateButton2!!.text = "☆"
                mRateButton3!!.text = "☆"
                mRateButton4!!.text = "☆"
                mRateButton5!!.text = "☆"
            }
            25 -> {
                mRateButton1!!.text = "★"
                mRateButton2!!.text = "★"
                mRateButton3!!.text = "☆"
                mRateButton4!!.text = "☆"
                mRateButton5!!.text = "☆"
            }
            50 -> {
                mRateButton1!!.text = "★"
                mRateButton2!!.text = "★"
                mRateButton3!!.text = "★"
                mRateButton4!!.text = "☆"
                mRateButton5!!.text = "☆"
            }
            75 -> {
                mRateButton1!!.text = "★"
                mRateButton2!!.text = "★"
                mRateButton3!!.text = "★"
                mRateButton4!!.text = "★"
                mRateButton5!!.text = "☆"
            }
            100 -> {
                mRateButton1!!.text = "★"
                mRateButton2!!.text = "★"
                mRateButton3!!.text = "★"
                mRateButton4!!.text = "★"
                mRateButton5!!.text = "★"
            }
        }
        mNextButton!!.visibility = View.VISIBLE
    }

    private fun sendNotification(score: Int){
        if (mGoogleApiClient!!.isConnected) {
            val dataMapRequest = PutDataMapRequest.create(Constants.NOTIFICATION_PATH)
            dataMapRequest.dataMap.putString(Constants.NOTIFICATION_TITLE, "Triage")
            dataMapRequest.dataMap.putString(Constants.NOTIFICATION_CONTENT, "今日の体調を測定しましょう！")
            dataMapRequest.dataMap.putInt(Constants.NOTIFICATION_SCORE, score)
            // Set timestamp so that it always trigger onDataChanged event
            dataMapRequest.dataMap.putLong(Constants.NOTIFICATION_TIME, System.currentTimeMillis())
            val putDataRequest = dataMapRequest.asPutDataRequest()
            val pendingResult = Wearable.DataApi.putDataItem(mGoogleApiClient, putDataRequest)
            pendingResult.setResultCallback(object : ResultCallback<DataApi.DataItemResult> {
                override fun onResult(dataItemResult: DataApi.DataItemResult) {
                    Log.d(TAG, "Send result:" + dataItemResult.status.isSuccess)
                }
            })
        } else {
            Log.e(TAG, "No connection to wearable available!")
        }
    }

    companion object {
        private val TAG = "MainActivity"
    }

    public object Constants {
        public val NOTIFICATION_PATH: String = "/notification"
        public val NOTIFICATION_TITLE: String = "title"
        public val NOTIFICATION_CONTENT: String = "content"
        public var NOTIFICATION_SCORE: String = "score"
        public val NOTIFICATION_TIME: String = "time"
    }
}