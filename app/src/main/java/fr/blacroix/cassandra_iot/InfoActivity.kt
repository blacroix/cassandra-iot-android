package fr.blacroix.cassandra_iot

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import kotlinx.android.synthetic.main.activity_info.*

class InfoActivity : AppCompatActivity() {

    companion object {
        val KEY_URL = "KEY_URL"
        val KEY_FREQUENCY = "KEY_FREQUENCY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        url.setText(PreferenceManager.getDefaultSharedPreferences(this).getString(KEY_URL, MainActivity.URL))
        frequency.setText(PreferenceManager.getDefaultSharedPreferences(this).getLong(KEY_FREQUENCY, MainActivity.FREQUENCY).toString())

        save.setOnClickListener {
            if (!TextUtils.isEmpty(frequency.text)) {
                val preferences = PreferenceManager.getDefaultSharedPreferences(this)
                val editor = preferences.edit()
                editor.putString(KEY_URL, url.text.toString())
                editor.putLong(KEY_FREQUENCY, frequency.text.toString().toLong())
                editor.apply()
                setResult(24)
                finish()
            }
        }
    }

}
