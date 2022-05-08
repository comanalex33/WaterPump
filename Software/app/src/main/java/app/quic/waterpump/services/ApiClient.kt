package app.quic.waterpump.services

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiClient {
    companion object {
        private fun getRetrofit(): Retrofit {
            return Retrofit.Builder()
                .baseUrl("https://api.thingspeak.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        fun getService(): ApiService {
            return getRetrofit().create(ApiService::class.java)
        }
    }
}