package moe.napo.triage

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.support.wearable.view.WatchViewStub
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView

import java.util.ArrayList

public class MainWearActivity : WearableActivity(), SensorEventListener {
    private var mTextViewHeart: TextView? = null
    private var mTextViewMeasuring: TextView? = null
    private var mApplyButton: Button? = null
    private var mHealthScore: Int = 0
    private val mHeartRates = ArrayList<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Keep the Wear screen always on (for testing only!)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super<WearableActivity>.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_wear)
        val stub = findViewById(R.id.watch_view_stub) as WatchViewStub
        stub.setOnLayoutInflatedListener(object : WatchViewStub.OnLayoutInflatedListener {
            override fun onLayoutInflated(stub: WatchViewStub) {
                mTextViewMeasuring = findViewById(R.id.activity_main_wear_measuring_text) as TextView
                mApplyButton = findViewById(R.id.activity_main_wear_apply_button) as Button
                mApplyButton!!.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View) {
                        onNextButtonClicked()
                    }
                })
                mTextViewHeart = stub.findViewById(R.id.activity_main_wear_heartrate_text) as TextView
                getHeartRate()
            }
        })
        val intent = getIntent()
        mHealthScore = intent.getIntExtra("score", 0)
    }

    private fun onNextButtonClicked() {
        val heartPoint = calculateAverage(mHeartRates).toInt()
        Log.d("average", "" + heartPoint)
        val intent = Intent()
        intent.putExtra("score", mHealthScore + heartPoint)
    }

    private fun calculateAverage(marks: List<Int>): Double {
        var sum: Int? = 0
        if (!marks.isEmpty()) {
            for (mark in marks) {
                sum = sum!!.toInt() + mark.toInt()
            }
            return sum!!.toDouble() / marks.size().toDouble()
        }
        return sum!!.toDouble()
    }

    private fun getHeartRate() {
        val mSensorManager = (getSystemService(Context.SENSOR_SERVICE) as SensorManager)
        val mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE && event.values[0].toInt() != 0) {
            val heartRate = event.values[0].toInt()
            val msg = "" + heartRate
            mTextViewHeart!!.setVisibility(View.VISIBLE)
            mTextViewHeart!!.setText(msg)
            mTextViewMeasuring!!.setVisibility(View.VISIBLE)
            Log.d(TAG, msg)
            mHeartRates.add(heartRate)
        } else if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            val msg = "" + event.values[0].toInt()
            mTextViewMeasuring!!.setVisibility(View.INVISIBLE)
            mTextViewHeart!!.setVisibility(View.INVISIBLE)
            Log.d(TAG, msg)
        } else {
            mTextViewMeasuring!!.setVisibility(View.INVISIBLE)
            mTextViewHeart!!.setVisibility(View.INVISIBLE)
            Log.d(TAG, "Unknown sensor type")
        }
        if (mHeartRates.size() > 10) {
            mApplyButton!!.setVisibility(View.VISIBLE)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
    }

    companion object {
        private val TAG = "MainWearActivity"
    }
}