package tv.remo.android.controller.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import tv.remo.android.controller.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        settingsButton.setOnClickListener {
            startActivity(SettingsActivity.getIntent(this))
        }
    }

    companion object{
        fun getIntent(context: Context) : Intent {
            return Intent(context, MainActivity::class.java)
        }
    }
}
