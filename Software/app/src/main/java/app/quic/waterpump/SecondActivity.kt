package app.quic.waterpump

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import app.quic.waterpump.models.ThingSpeakResponse
import app.quic.waterpump.services.ApiClient
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SecondActivity : AppCompatActivity() {

    lateinit var linelist:ArrayList<Entry>
    lateinit var lineDataset: LineDataSet
    lateinit var line_chart: LineChart
    lateinit var lineData: LineData
    val handler = Handler(Looper.getMainLooper())
    private lateinit var backButton: Button
    private lateinit var averageText: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        backButton = findViewById(R.id.back_button)
        averageText = findViewById(R.id.textView2)

        handler.post(object : Runnable {
            override fun run() {
                var average = 0
                val getDataCall: Call<ThingSpeakResponse> = ApiClient.getService().readSensorData(resources.getString(R.string.read_api_key), 100)
                getDataCall.enqueue(object : Callback<ThingSpeakResponse> {
                    @SuppressLint("SimpleDateFormat", "SetTextI18n")
                    override fun onResponse(
                        call: Call<ThingSpeakResponse>,
                        response: Response<ThingSpeakResponse>
                    ) {
                        if (response.isSuccessful) {
                            linelist= ArrayList()
                            for(i in 0..99){
                                linelist.add(Entry(i.toFloat(),response.body()?.feeds!![i].field2.toFloat()))
                                average += response.body()?.feeds!![i].field2.toInt()
                            }
                            averageText.text = (average/100).toString()
                            lineDataset = LineDataSet(linelist, "Humidity")
                            lineData= LineData(lineDataset)
                            line_chart = findViewById(R.id.line_chart)
                            line_chart.data = lineData
                            lineDataset.setColors(*ColorTemplate.JOYFUL_COLORS)
                            lineDataset.valueTextColor= Color.BLUE
                            lineDataset.valueTextSize= 20f


                        } else {
                            Toast.makeText(applicationContext, response.code().toString(), Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<ThingSpeakResponse>, t: Throwable) {
                        Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                    }

                })
            }
        })
        backButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

    }
}