package com.example.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // URL del Backend de FastAPI (Local / Render)
    // Cambiar por "https://chamba-rd.onrender.com/api/v1/" en producción
    private const val BASE_URL = "https://ais-dev-wmprgtn55odgjal2pwbjxh-856541202184.us-east1.run.app/api/v1/"

    var authToken: String? = null

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val httpClient = OkHttpClient.Builder().apply {
        addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("Accept", "application/json")
            
            authToken?.let {
                requestBuilder.header("Authorization", "Bearer $it")
            }
            
            chain.proceed(requestBuilder.build())
        }
        connectTimeout(30, TimeUnit.SECONDS)
        readTimeout(30, TimeUnit.SECONDS)
    }.build()

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val chambaApi: ChambaApi by lazy { retrofit.create(ChambaApi::class.java) }
    val postulacionApi: PostulacionApi by lazy { retrofit.create(PostulacionApi::class.java) }
    val reportesApi: ReportesApi by lazy { retrofit.create(ReportesApi::class.java) }
}
