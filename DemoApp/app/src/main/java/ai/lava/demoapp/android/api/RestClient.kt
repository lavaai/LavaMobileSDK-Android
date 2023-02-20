package ai.lava.demoapp.android.api

import com.lava.lavasdk.SecureMemberTokenExpiryListener
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiResultListener {
    fun onResult(success: Boolean, authResponse: AuthResponse?, errorMessage: String? = null)
}

internal interface AppApiInterface {
    @POST("/login")
    fun login(@Body body: LoginRequest): Call<AuthResponse>

    @POST("/refresh_token")
    fun refreshToken(@Body body: RefreshTokenRequest): Call<AuthResponse>
}

internal object RestClient {
    // This should be replaced by another testing URL
    private const val baseUrl: String = "https://gcp2dev-sdk-sample-app-backend.test.lava.ai/"

    private fun clientBuilder(): OkHttpClient.Builder {
        return OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor()
                    .setLevel(HttpLoggingInterceptor.Level.BODY)
            )
    }

    private val retrofitBuilder: Retrofit.Builder by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
    }

    val client: AppApiInterface by lazy {
        val client = clientBuilder().build()
        val retrofit = retrofitBuilder.client(client).build()

        retrofit.create(AppApiInterface::class.java)
    }

    fun login(body: LoginRequest, listener: ApiResultListener) {
        val call = client.login(body)

        call.enqueue(object: Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                listener.onResult(response.isSuccessful, response.body())
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                t.printStackTrace()
                listener.onResult(false, null, t.message ?: "")
            }
        })
    }

    fun refreshToken(body: RefreshTokenRequest, listener: ApiResultListener) {
        val call = client.refreshToken(body)

        call.enqueue(object: Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                listener.onResult(response.isSuccessful, response.body())
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                t.printStackTrace()
                listener.onResult(false, null, t.message ?: "")
            }
        })
    }
}