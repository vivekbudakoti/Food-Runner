package com.rks369.foodrunner.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.rks369.foodrunner.R
import com.rks369.foodrunner.util.ConnectionManager
import org.json.JSONException
import org.json.JSONObject


class LoginActivity : AppCompatActivity() {

    private lateinit var txtForgot: TextView
    private lateinit var btnLogin: Button
    private lateinit var etPassword: EditText
    private lateinit var etMobileNumber: EditText
    private lateinit var txtRegister: TextView


    private var mobilePattern = "[7-9][0-9]{9}"

    lateinit var sharedpreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sharedpreferences =
            this.getSharedPreferences("FoodApp", Context.MODE_PRIVATE)

        val isLoggedIn = sharedpreferences.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
            val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
            startActivity(intent)
            finish()
        }


        txtForgot = findViewById(R.id.txtForgotPassword)
        btnLogin = findViewById(R.id.btnLogin)
        etPassword = findViewById(R.id.etPassword)
        etMobileNumber = findViewById(R.id.etMobileNumber)
        txtRegister = findViewById(R.id.txtRegisterYourself)

        txtRegister.setOnClickListener {

            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)

        }

        txtForgot.setOnClickListener {

            val intent = Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
            startActivity(intent)

        }

        btnLogin.setOnClickListener {

            if (validations(etMobileNumber.text.toString(), etPassword.text.toString())) {

                if (ConnectionManager().checkConnectivity(this@LoginActivity)) {

                    val queue = Volley.newRequestQueue(this@LoginActivity)
                    val url = "http://13.235.250.119/v2/login/fetch_result/"
                    val jsonParams = JSONObject()
                    jsonParams.put("mobile_number", etMobileNumber.text.toString())
                    jsonParams.put("password", etPassword.text.toString())

                    val jsonObjectRequest =
                        object : JsonObjectRequest(
                            Method.POST,
                            url,
                            jsonParams,
                            Response.Listener {
                                try {
                                    val data = it.getJSONObject("data")
                                    val success = data.getBoolean("success")
                                    if (success) {
                                        btnLogin.isEnabled = false
                                        btnLogin.isClickable = false

                                        val response = data.getJSONObject("data")
                                        sharedpreferences.edit()
                                            .putString("user_id", response.getString("user_id"))
                                            .apply()
                                        sharedpreferences.edit()
                                            .putString("user_name", response.getString("name"))
                                            .apply()
                                        sharedpreferences.edit().putString(
                                            "user_mobile_number",
                                            response.getString("mobile_number")
                                        ).apply()
                                        sharedpreferences.edit()
                                            .putString(
                                                "user_address",
                                                response.getString("address")
                                            )
                                            .apply()
                                        sharedpreferences.edit()
                                            .putString("user_email", response.getString("email"))
                                            .apply()
                                        sharedpreferences.edit()
                                            .putBoolean("isLoggedIn", true)
                                            .apply()

                                        startActivity(
                                            Intent(
                                                this@LoginActivity,
                                                DashboardActivity::class.java
                                            )
                                        )
                                        finish()
                                    } else {
                                        Toast.makeText(
                                            this@LoginActivity,
                                            "Invalid Credentials",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                }
                            },
                            Response.ErrorListener {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Volley error occurred",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }) {
                            override fun getHeaders(): MutableMap<String, String> {
                                val headers = HashMap<String, String>()
                                headers["Content-type"] = "application/json"
                                headers["token"] = "9bf534118365f1"
                                return headers
                            }
                        }
                    queue.add(jsonObjectRequest)
                } else {
                    val dialog = AlertDialog.Builder(this@LoginActivity)
                    dialog.setTitle("Error")
                    dialog.setMessage("Internet Connection Found")
                    dialog.setPositiveButton("Open Settings") { _, _ ->
                        val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                        startActivity(settingsIntent)
                        this.finish()

                    }
                    dialog.setNegativeButton("Exit") { _, _ ->

                        ActivityCompat.finishAffinity(this@LoginActivity)
                    }
                    dialog.create()
                    dialog.show()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()

        val login = sharedpreferences.getBoolean("isLoggedIn", false)
        if (login)
            finish()
    }

    private fun validations(phone: String, password: String): Boolean {

        if (phone.isEmpty()) {
            Toast.makeText(this@LoginActivity, "Enter Mobile Number", Toast.LENGTH_LONG).show()
            return false
        } else {
            return if (password.isEmpty()) {
                Toast.makeText(this@LoginActivity, "Enter Password", Toast.LENGTH_LONG).show()
                false
            } else {
                if (!phone.trim().matches(mobilePattern.toRegex())) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Enter a valid Mobile number",
                        Toast.LENGTH_LONG
                    )
                        .show()
                    false
                } else
                    true
            }
        }
    }
}
