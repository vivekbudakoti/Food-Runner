package com.rks369.foodrunner.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.rks369.foodrunner.R

class SplashActivity : AppCompatActivity() {

    /*Life-cycle method of the activity*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*Setting up the view for the activity*/
        setContentView(R.layout.activity_splash)

        /*This is how we change the activity with a delay of 2000 milliseconds.
        */
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            finish()
        }, 2000)
    }
}
