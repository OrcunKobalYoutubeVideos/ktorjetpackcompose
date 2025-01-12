package com.ktoryoutube

import com.google.gson.GsonBuilder
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

interface ApiEndpoints{
    @GET("products")
    suspend fun getProducts() : Response<List<ProductItem>>
}

class ApiClient(
    private val apiEndpoints: ApiEndpoints
) {

    suspend fun getProducts(): ApiResponse<List<ProductItem>> {
        return apiRequest {
            apiEndpoints.getProducts()
        }
    }

    private inline fun <T> apiRequest(callBack:() -> Response<T>) : ApiResponse<T>{
        return try {
            val response = callBack()
            if( response.isSuccessful ){
                val body = response.body()
                if(body != null ){
                    ApiResponse.Success(data = body)
                }else{
                    ApiResponse.Error(error = Exception("Empty List"))
                }
            }else{
                ApiResponse.Error(error = Exception("Unknown Error"))
            }
        }catch (e:Exception){
            ApiResponse.Error(error = e)
        }
    }

    sealed interface ApiResponse<T> {
        data class Success<T>(val data: T) : ApiResponse<T>
        data class Error<T>(val error: Exception) : ApiResponse<T>

        fun onSuccess(block:(T) -> Unit): ApiResponse<T> {
            if( this is Success ) block(data)
            return this
        }

        fun onError(block:(Exception)->Unit): ApiResponse<T> {
            if( this is Error ) block(error)
            return this
        }
    }

    companion object{
        operator fun invoke():ApiClient{

            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(10,TimeUnit.SECONDS)
                .writeTimeout(10,TimeUnit.SECONDS)
                .readTimeout(10,TimeUnit.SECONDS)
                .addInterceptor(httpLoggingInterceptor)
                .build()

            val gson = GsonBuilder()
                .setLenient()
                .setPrettyPrinting()
                .create()

            val httpUrl = HttpUrl.Builder()
                .host("fakestoreapi.com")
                .scheme("https")
                .build()

            val retrofit = Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(httpUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

            return ApiClient(
                apiEndpoints = retrofit.create(ApiEndpoints::class.java)
            )
        }
    }

}