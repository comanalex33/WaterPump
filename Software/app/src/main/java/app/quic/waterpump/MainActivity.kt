package app.quic.waterpump

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import app.quic.waterpump.models.ThingSpeakResponse
import app.quic.waterpump.services.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var updateButton: Button
    private lateinit var textField: EditText
    private lateinit var textView: TextView
    private lateinit var textView2: TextView
    private lateinit var textView3: TextView
    private lateinit var next: Button

    val handler = Handler(Looper.getMainLooper())
    val delay: Long = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        updateButton = findViewById(R.id.button)
        textField = findViewById(R.id.value_field)
        textView = findViewById(R.id.textView)
        textView2 = findViewById(R.id.textView2)
        textView3 = findViewById(R.id.textView3)
        next = findViewById(R.id.next)

        updateButton.setOnClickListener {
            if(textField.text.toString() == "") {
                Toast.makeText(applicationContext, "No value passed", Toast.LENGTH_SHORT).show()
            } else {
                val updateCall: Call<Int> = ApiClient.getService().updateData(resources.getString(R.string.write_api_key), textField.text.toString().toInt())

                updateCall.enqueue(object: Callback<Int> {
                    override fun onResponse(call: Call<Int>, response: Response<Int>) {

                        if(response.isSuccessful) {
                            Toast.makeText(applicationContext, response.body().toString(), Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(applicationContext, response.code().toString(), Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<Int>, t: Throwable) {
                        Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                    }

                })
            }
        }

        next.setOnClickListener {
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(object : Runnable {
            override fun run() {
                val getDataCall: Call<ThingSpeakResponse> = ApiClient.getService().readSensorData(resources.getString(R.string.read_api_key), 1)
                getDataCall.enqueue(object : Callback<ThingSpeakResponse> {
                    @SuppressLint("SimpleDateFormat", "SetTextI18n")
                    override fun onResponse(
                        call: Call<ThingSpeakResponse>,
                        response: Response<ThingSpeakResponse>
                    ) {
                        if (response.isSuccessful) {

                            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.UK)
                            format.timeZone = TimeZone.getTimeZone("UTC")
                            val date = format.parse(response.body()?.feeds!![0].created_at)

                            val calendar = Calendar.getInstance()
                            calendar.time = date!!

                            val outputFormat = SimpleDateFormat("yyyy MMMM dd - HH:mm")
                            textView.text = "Last update: ${outputFormat.format(date)}"

                            textView2.text = "${response.body()?.channel!!.field1}: ${response.body()?.feeds!![0].field1}"
                            textView3.text = "${response.body()?.channel!!.field2}: ${response.body()?.feeds!![0].field2}"
                        } else {
                            Toast.makeText(applicationContext, response.code().toString(), Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<ThingSpeakResponse>, t: Throwable) {
                        Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                    }

                })
                handler.postDelayed(this, delay)
            }
        })
    }
}