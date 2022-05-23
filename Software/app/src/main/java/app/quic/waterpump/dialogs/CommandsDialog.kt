package app.quic.waterpump.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import app.quic.waterpump.R
import app.quic.waterpump.services.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CommandsDialog: DialogFragment() {

    private lateinit var smallPeriodButton: Button
    private lateinit var continuousButton: Button
    private lateinit var stopButton: Button
    private lateinit var hardwareButton: Button
    private lateinit var messageText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_commands, container, false)

        //set background transparent
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        smallPeriodButton = view.findViewById(R.id.small_period_button)
        continuousButton = view.findViewById(R.id.continuous_button)
        stopButton = view.findViewById(R.id.stop_button)
        hardwareButton = view.findViewById(R.id.hardware_decisions_button)
        messageText = view.findViewById(R.id.message_text)

        smallPeriodButton.setOnClickListener {
            updateServer(1)
        }

        continuousButton.setOnClickListener {
            updateServer(2)
        }

        stopButton.setOnClickListener {
            updateServer(3)
        }

        hardwareButton.setOnClickListener {
            updateServer(4)
        }

        return view
    }

    private fun updateServer(value: Int) {
        val updateCall: Call<Int> = ApiClient.getService().updateData(resources.getString(R.string.write_api_key), value)

        updateCall.enqueue(object: Callback<Int> {
            override fun onResponse(call: Call<Int>, response: Response<Int>) {

                if(response.isSuccessful) {
                    if(response.body().toString().toInt() == 0) {
                        messageText.text = "Cannot update data yet, try again"
                        messageText.setTextColor(resources.getColor(R.color.red))
                        Handler().postDelayed({
                            messageText.text = ""
                        }, 3000)
                    } else {
                        messageText.text = "Updated successfully"
                        messageText.setTextColor(resources.getColor(R.color.green))
                        Handler().postDelayed({
                            messageText.text = ""
                        }, 3000)
                    }
                } else {
                    messageText.text = response.code().toString()
                    messageText.setTextColor(resources.getColor(R.color.red))
                    Handler().postDelayed({
                        messageText.text = ""
                    }, 3000)
                }
            }

            override fun onFailure(call: Call<Int>, t: Throwable) {
                Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
            }

        })
    }
}