package app.quic.waterpump

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import app.quic.waterpump.Utility.NetworkChangeListener
import app.quic.waterpump.dialogs.CommandsDialog
import app.quic.waterpump.models.ThingSpeakResponse
import app.quic.waterpump.services.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var commandsButton: Button
    private lateinit var updateDateText: TextView
    private lateinit var lightStatusText: TextView
    private lateinit var humidityStatusText: TextView
    private lateinit var next: Button
    var networkChangeListener = NetworkChangeListener()
    val handler = Handler(Looper.getMainLooper())
    val delay: Long = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        commandsButton = findViewById(R.id.command_button)
        updateDateText = findViewById(R.id.update_text)
        lightStatusText = findViewById(R.id.field1_value)
        humidityStatusText = findViewById(R.id.field2_value)
        next = findViewById(R.id.graphics_button)

        commandsButton.setOnClickListener {
            val dialog = CommandsDialog()
            dialog.show(this.supportFragmentManager, "Commands dialog")
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
                            updateDateText.text = "Last update: ${outputFormat.format(date)}"

                            if(response.body()?.feeds!![0].field1 == "1") {
                                lightStatusText.text = "NO"
                                lightStatusText.setTextColor(resources.getColor(R.color.red))
                            } else {
                                lightStatusText.text = "YES"
                                lightStatusText.setTextColor(resources.getColor(R.color.green))
                            }

                            val humidity = response.body()?.feeds!![0].field2.toInt()
                            if(humidity > 800) {
                                humidityStatusText.text = "LOW"
                                humidityStatusText.setTextColor(resources.getColor(R.color.red))
                            } else if(humidity > 600) {
                                humidityStatusText.text = "MEDIUM"
                                humidityStatusText.setTextColor(resources.getColor(R.color.yellow))
                            } else {
                                humidityStatusText.text = "PROPERLY"
                                humidityStatusText.setTextColor(resources.getColor(R.color.green))
                            }
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
    override fun onStart() {
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeListener, filter)
        super.onStart()
    }
    override fun onStop() {
        unregisterReceiver(networkChangeListener)
        super.onStop()
    }
}