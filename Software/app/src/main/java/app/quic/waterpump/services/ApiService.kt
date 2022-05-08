package app.quic.waterpump.services

import app.quic.waterpump.models.ThingSpeakResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("update")
    fun updateData(@Query("api_key") writeApiKey: String,
                   @Query("field1") value: Int) : Call<Int>

    @GET("channels/1706158/fields/1.json")
    fun readSensorData(@Query("api_key") readApiKey: String,
                       @Query("results") results: Int): Call<ThingSpeakResponse>
}