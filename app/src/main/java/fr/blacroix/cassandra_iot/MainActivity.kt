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
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), SensorEventListener {

    companion object {
        val FREQUENCY: Long = 10000
        val URL: String = "http://private-anon-ce4f5ccfd-mnantern.apiary-mock.com/data"
    }

    val JSON: MediaType = MediaType.parse("application/json; charset=utf-8")
    var x: Float = 0f
    var y: Float = 0f
    var z: Float = 0f
    var lum: Float = 0f
    val handler: Handler = Handler()
    val recursiveSubmit = { submitSensorData() }

    val anim: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.bounce_fade)
    }

    val sensorManager: SensorManager by lazy {
        getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fab = findViewById(R.id.fab) as FloatingActionButton

        fab.setOnClickListener {
            startActivityForResult(Intent(this, InfoActivity::class.java), 42)
        }

        registerSensorListener()
        submitSensorData()
    }

    fun registerSensorListener() {
        sensorManager.unregisterListener(this)

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

    fun submitSensorData() {
        imageCenter.startAnimation(anim)
        val preferences = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
        val urls = preferences.getString(InfoActivity.KEY_URL, URL)
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.format(Date())
        for (url in urls.split(";")) {
            val thread = Thread() {
                val client = OkHttpClient()
                val body = RequestBody.create(JSON, "[{\"smartphoneId\":\"${preferences.getString(App.DEVICE_ID_KEY, "")}\",\"type\":\"BRIGHTNESS\",\"eventTime\":\"$date\",\"value\":\"$lum\"},{\"smartphoneId\":\"${preferences.getString(App.DEVICE_ID_KEY, "")}\",\"type\":\"ACCELEROMETER\",\"eventTime\":\"$date\",\"value\":\"$x;$y;$z\"}]")
                try {
                    val request = Request.Builder()
                            .url(url)
                            .post(body)
                            .build()
                    val response = client.newCall(request).execute()
                    if (response.code() == 201) {
                        // Do nothing
                    } else {
                        Snackbar.make(coodinatorLayout, "An error occurred: ${response.code()}", Snackbar.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Snackbar.make(coodinatorLayout, "An error occurred: ${e.message}", Snackbar.LENGTH_SHORT).show()
                }
            }
            thread.start()
        }
        handler.postDelayed(recursiveSubmit, preferences.getLong(InfoActivity.KEY_FREQUENCY, FREQUENCY))
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

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }

}