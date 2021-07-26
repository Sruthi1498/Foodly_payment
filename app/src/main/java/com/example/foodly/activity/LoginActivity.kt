package com.example.foodly.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.foodly.R
import com.example.foodly.util.ConnectionManager
import com.muddzdev.styleabletoast.StyleableToast
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject


class LoginActivity : AppCompatActivity() {

    lateinit var etMobileNumber: EditText
    lateinit var etPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        etMobileNumber = findViewById(R.id.login_mobile_no)
        etPassword = findViewById(R.id.login_password)


        login_btn.setOnClickListener {

            if (etMobileNumber.text.isBlank() || etMobileNumber.text.length != 10) {
                etMobileNumber.error = "Enter valid Mobile Number"
            } else {
                if (etPassword.text.isBlank() || (etPassword.text.length <= 4)) {
                    etPassword.error = "Enter Valid Password"
                } else {
                    loginUserFun()
                }

            }

        }
        login_sign_up.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

    }
    private fun loginUserFun() {
        val loginUser = JSONObject()
        loginUser.put("mobile_number", etMobileNumber.text)
        loginUser.put("password", etPassword.text)

        val queue = Volley.newRequestQueue(this)
        val url = "http://13.235.250.119/v2/login/fetch_result/"

        if (ConnectionManager().checkConnectivity(this)) {

            val sharedPreferences = this.getSharedPreferences(
                getString(R.string.shared_preferences),
                Context.MODE_PRIVATE
            )

            try {
                val jsonRequest =
                    object :
                        JsonObjectRequest(Request.Method.POST, url, loginUser, Response.Listener
                        {

                            val response = it.getJSONObject("data")
                            val success = response.getBoolean("success")

                            if (success) {
                                val data = response.getJSONObject("data")
                                //TODO sharedPreferences.edit() creates a new Editor instance for every method call.
                                // This can be called once, the  returned editor instance can be stored in a variable and re-used.
                                // same goes to the apply method. apply method initiates a write operation. So instead of calling it multiple times,
                                // it should be called once to save this batch of values
                                // We should always reuse an Object especially a resource, reduce the no of expensive method calls whenever possible
                                sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()
                                sharedPreferences.edit()
                                    .putString("user_id", data.getString("user_id")).apply()
                                sharedPreferences.edit().putString("name", data.getString("name"))
                                    .apply()
                                sharedPreferences.edit().putString("email", data.getString("email"))
                                    .apply()
                                sharedPreferences.edit()
                                    .putString("mobile_number", data.getString("mobile_number"))
                                    .apply()
                                sharedPreferences.edit()
                                    .putString("address", data.getString("address")).apply()

                                StyleableToast.Builder(this)
                                    .text("Welcome " + data.getString("name"))
                                    .backgroundColor(Color.RED)
                                    .textColor(Color.WHITE).show()

                                userSuccessfullyLoggedIn()
                            } else {
                                Toast.makeText(
                                    this,
                                    " Incorrect Mobile No Or Password",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }


                        }, Response.ErrorListener {
                            Toast.makeText(this, "Volley Error", Toast.LENGTH_SHORT).show()
                        }) {
                        override fun getHeaders(): MutableMap<String, String> {
                            val headers = HashMap<String, String>()
                            headers["Content-type"] = "application/json"
                            headers["token"] = "9bf534118365f1"
                            return headers
                        }
                    }
                queue.add(jsonRequest)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } else {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("ERROR")
            dialog.setMessage("Internet Connection  Not Found")
            dialog.setPositiveButton("Open Settings") { text, listener ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                finish()


            }

            dialog.setNegativeButton("Exit") { text, listener ->
                ActivityCompat.finishAffinity(this)
            }
            dialog.create()
            dialog.show()
        }
    }

    private fun userSuccessfullyLoggedIn() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }


}