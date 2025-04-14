package com.example.testupanh.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object RetrofitInstance {
    val api: CloudinaryApi by lazy {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        Retrofit.Builder()
            .baseUrl("https://api.cloudinary.com/")  // Đảm bảo URL này đúng
            .addConverterFactory(GsonConverterFactory.create())  // Gson Converter
            .client(client)
            .build()
            .create(CloudinaryApi::class.java)
    }
}


