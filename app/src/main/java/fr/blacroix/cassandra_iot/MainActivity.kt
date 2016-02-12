package fr.blacroix.cassandra_iot

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

class MainActivity : AppCompatActivity(), SensorEventListener {

    companion object {
        val FREQUENCY: Long = 10000
    }

    private val JSON: MediaType = MediaType.parse("application/json; charset=utf-8")
    private var x: Float = 0f
    private var y: Float = 0f
    private var z: Float = 0f
    private var lum: Float = 0f
    private val handler: Handler = Handler()
    private val recursiveSubmit = { submitSensorData() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fab = findViewById(R.id.fab) as FloatingActionButton

        fab.setOnClickListener {
            startActivityForResult(Intent(this, InfoActivity::class.java), 42)
        }

        imageCenter.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bounce_fade));

        registerSensorListener()
        submitSensorData()
    }

    private fun registerSensorListener() {
        val sensorManager: SensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer: Sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val light: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        if (light != null) {
            sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 42 && resultCode == 24) {
            handler.removeCallbacksAndMessages(null)
            submitSensorData()
        }
    }

    private fun submitSensorData() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
        val url = preferences.getString(InfoActivity.KEY_URL, null)
        if (!TextUtils.isEmpty(url)) {
            val thread = Thread() {
                val client = OkHttpClient()
                val body = RequestBody.create(JSON, "{\"x\":$x,\"y\":$y,\"z\":$z,\"lum\":$lum}")
                val request = Request.Builder()
                        .url(url)
                        .post(body)
                        .build()
                val response = client.newCall(request).execute()
                if (response.code() == 200) {
                    // Do nothing
                } else {
                    Snackbar.make(coodinatorLayout, "An error occurred: ${response.code()}", Snackbar.LENGTH_SHORT).show()
                }
            }
            thread.start()
            handler.postDelayed(recursiveSubmit, preferences.getLong(InfoActivity.KEY_FREQUENCY, FREQUENCY))
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        // Not needed
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    x = event.values[0];
                    y = event.values[1];
                    z = event.values[2];
                    sensorX.text = "x: $x"
                    sensorY.text = "y: $y"
                    sensorZ.text = "z: $z"
                }
                Sensor.TYPE_LIGHT -> {
                    lum = event.values[0]
                    sensorLight.text = "${event.values[0]}lx"
                }
            }
        }
    }

}